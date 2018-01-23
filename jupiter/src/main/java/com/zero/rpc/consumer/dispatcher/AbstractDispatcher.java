package com.zero.rpc.consumer.dispatcher;


import com.zero.rpc.RPCClient;
import com.zero.rpc.Request;
import com.zero.rpc.Response;
import com.zero.rpc.consumer.future.DefaultInvokeFuture;
import com.zero.rpc.consumer.loadbalance.LoadBalancer;
import com.zero.rpc.model.MethodSpecialConfig;
import com.zero.rpc.model.ResultWrapper;
import com.zero.rpc.model.ServiceMetaData;
import com.zero.serialization.api.Serializer;
import com.zero.transport.Status;
import com.zero.transport.api.RequestBytes;
import com.zero.transport.api.channel.Channel;
import com.zero.transport.api.channel.ChannelGroup;
import com.zero.transport.api.channel.CopyOnWriteGroupList;
import com.zero.transport.api.channel.FutureListener;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.zero.common.util.Maps;
import org.zero.common.util.SystemClock;
import com.zero.rpc.ConsumerHook;
import org.zero.rpc.exception.JupiterRemoteException;

import java.util.List;
import java.util.Map;

import static org.zero.common.util.StackTraceUtil.stackTrace;

public abstract class AbstractDispatcher implements Dispatcher {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractDispatcher.class);

    private final RPCClient client;
    private final LoadBalancer loadBalancer;                    // 软负载均衡
    private final Serializer serializerImpl;                    // 序列化/反序列化impl
    private ConsumerHook[] hooks = ConsumerHook.EMPTY_HOOKS;    // 消费者端钩子函数
    private long timeoutMillis = 3000;                          // 调用超时时间设置

    // 针对指定方法单独设置的超时时间, 方法名为key
    private Map<String, Long> methodSpecialTimeoutMapping = Maps.newHashMap();

    public AbstractDispatcher(RPCClient client, Serializer serializer) {
        this(client, null, serializer);
    }

    public AbstractDispatcher(RPCClient client, LoadBalancer loadBalancer, Serializer serializer) {
        this.client = client;
        this.loadBalancer = loadBalancer;
        this.serializerImpl = serializer;
    }

    public Serializer serializer() {
        return serializerImpl;
    }

    public ConsumerHook[] hooks() {
        return hooks;
    }

    @Override
    public Dispatcher hooks(List<ConsumerHook> hooks) {
        if (hooks != null && !hooks.isEmpty()) {
            this.hooks = hooks.toArray(new ConsumerHook[hooks.size()]);
        }
        return this;
    }

    @Override
    public Dispatcher timeoutMillis(long timeoutMillis) {
        if (timeoutMillis > 0) {
            this.timeoutMillis = timeoutMillis;
        }
        return this;
    }

    @Override
    public Dispatcher methodSpecialConfigs(List<MethodSpecialConfig> methodSpecialConfigs) {
        if (!methodSpecialConfigs.isEmpty()) {
            for (MethodSpecialConfig config : methodSpecialConfigs) {
                long timeoutMillis = config.getTimeoutMillis();
                if (timeoutMillis > 0) {
                    methodSpecialTimeoutMapping.put(config.getMethodName(), timeoutMillis);
                }
            }
        }
        return this;
    }

    public long getMethodSpecialTimeoutMillis(String methodName) {
        Long methodTimeoutMillis = methodSpecialTimeoutMapping.get(methodName);
        if (methodTimeoutMillis != null && methodTimeoutMillis > 0) {
            return methodTimeoutMillis;
        }
        return timeoutMillis;
    }

    protected Channel select(ServiceMetaData metadata) {
        CopyOnWriteGroupList groups = client.connector().directory(metadata);

        ChannelGroup group = loadBalancer.select(groups, metadata);

        if (group != null) {
            if (group.isAvailable()) {
                return group.next();
            }

            // to the deadline (no available channel), the time exceeded the predetermined limit
            long deadline = group.deadlineMillis();
            if (deadline > 0 && SystemClock.millisClock().now() > deadline) {
                boolean removed = groups.remove(group);
                if (removed) {
                    logger.warn("Removed channel group: {} in directory: {} on [select].", group, metadata.directory());
                }
            }
        } else {
            // for 3 seconds, expired not wait
            if (!client.awaitConnections(metadata, 3000)) {
                throw new IllegalStateException("no connections");
            }
        }

        ChannelGroup[] snapshot = groups.snapshot();
        for (ChannelGroup g : snapshot) {
            if (g.isAvailable()) {
                return g.next();
            }
        }

        throw new IllegalStateException("no channel");
    }

    protected ChannelGroup[] groups(ServiceMetaData metadata) {
        return client.connector().directory(metadata)
                .snapshot();
    }

    /**
     * 这里只关心向Channel里面写数据，
     * DefaultInvokeFuture的静态变量中已经保存了该future
     * 结果的处理在DefaultConsumerProcessor的MessageTask中，会调用DefaultInvokeFuture的静态方法，将该future设置值
     * @return
     */
    protected <T> DefaultInvokeFuture<T> write(
            Channel channel, final Request request, final DefaultInvokeFuture<T> future) {

        final RequestBytes requestBytes = request.requestBytes();
        final ConsumerHook[] hooks = future.hooks();

        channel.write(requestBytes, new FutureListener<Channel>() {

            @SuppressWarnings("all")
            @Override
            public void onSuccess(Channel channel) throws Exception {
                // 标记已发送
                future.markSent();
                requestBytes.nullBytes();

                // hook.before()
                for (int i = 0; i < hooks.length; i++) {
                    hooks[i].before(request, channel);
                }
            }

            @Override
            public void onFailure(Channel channel, Throwable cause) throws Exception {
                requestBytes.nullBytes();

                if (logger.isWarnEnabled()) {
                    logger.warn("Writes {} fail on {}, {}.", request, channel, stackTrace(cause));
                }

                ResultWrapper result = new ResultWrapper();
                result.setError(new JupiterRemoteException(cause));

                Response response = new Response(requestBytes.invokeId());
                response.status(Status.CLIENT_ERROR);
                response.result(result);

                DefaultInvokeFuture.received(channel, response);
            }
        });

        return future;
    }
}
