package com.zero.easyrpc.registry.base;

import com.zero.easyrpc.common.exception.RemotingSendRequestException;
import com.zero.easyrpc.common.exception.RemotingTimeoutException;
import com.zero.easyrpc.common.loadbalance.LoadBalanceStrategy;
import com.zero.easyrpc.common.protocal.Protocol;
import com.zero.easyrpc.common.rpc.RegisterMeta;
import com.zero.easyrpc.common.transport.body.AckCustomBody;
import com.zero.easyrpc.common.transport.body.SubcribeResultCustomBody;
import com.zero.easyrpc.netty4.Transporter;
import com.zero.easyrpc.registry.DefaultRegistry;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.ConcurrentSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 注册中心模块消费端的管理
 * Created by jianjia1 on 17/12/07.
 */
public class RegistryConsumerManager {
    private static final Logger logger = LoggerFactory.getLogger(RegistryConsumerManager.class);

    private DefaultRegistry defaultRegistry;

    private static final AttributeKey<ConcurrentSet<String>> S_SUBSCRIBE_KEY = AttributeKey.valueOf("server.subscribed");

    private volatile ChannelGroup subscriberChannels = new DefaultChannelGroup("subscribers", GlobalEventExecutor.INSTANCE);

    private final ConcurrentSet<MessageNonAck> messagesNonAcks = new ConcurrentSet<>();

    public RegistryConsumerManager(DefaultRegistry defaultRegistry) {
        this.defaultRegistry = defaultRegistry;
    }

    public ChannelGroup getSubscriberChannels() {
        return subscriberChannels;
    }

    /**
     * 通知相关的订阅者服务的信息
     * @param meta
     * @param loadBalanceStrategy
     * @throws InterruptedException
     * @throws RemotingTimeoutException
     * @throws RemotingSendRequestException
     */
    public void notifyMacthedSubscriber(final RegisterMeta meta, LoadBalanceStrategy loadBalanceStrategy) throws RemotingSendRequestException, RemotingTimeoutException, InterruptedException {

        // 构建订阅通知的主体传输对象
        SubcribeResultCustomBody subcribeResultCustomBody = new SubcribeResultCustomBody();
        buildSubcribeResultCustomBody(meta, subcribeResultCustomBody, loadBalanceStrategy);

        // 传送给consumer对象的RemotingTransporter
        Transporter sendConsumerRemotingTrasnporter = Transporter.createRequestTransporter(Protocol.SUBCRIBE_RESULT,
                subcribeResultCustomBody);

        pushMessageToConsumer(sendConsumerRemotingTrasnporter, meta.getServiceName());
    }

    /**
     * 通知订阅者某个服务取消
     * @param meta
     * @throws RemotingSendRequestException
     * @throws RemotingTimeoutException
     * @throws InterruptedException
     */
    public void notifyMacthedSubscriberCancel(final RegisterMeta meta) throws RemotingSendRequestException, RemotingTimeoutException, InterruptedException {

        // 构建订阅通知的主体传输对象
        SubcribeResultCustomBody subcribeResultCustomBody = new SubcribeResultCustomBody();
        buildSubcribeResultCustomBody(meta, subcribeResultCustomBody, null);

        Transporter sendConsumerRemotingTrasnporter = Transporter.createRequestTransporter(Protocol.SUBCRIBE_SERVICE_CANCEL,
                subcribeResultCustomBody);

        pushMessageToConsumer(sendConsumerRemotingTrasnporter, meta.getServiceName());

    }


    /**
     * 检查messagesNonAcks中是否有发送失败的信息，然后再次发送
     */
    public void checkSendFailedMessage() {

        ConcurrentSet<MessageNonAck> nonAcks = messagesNonAcks;
        messagesNonAcks.clear();
        if (nonAcks != null) {
            for (MessageNonAck messageNonAck : nonAcks) {
                try {
                    pushMessageToConsumer(messageNonAck.getMsg(), messageNonAck.getServiceName());
                } catch (Exception e) {
                    logger.error("send message failed");
                }
            }
        }
    }


    /**
     * 因为在consumer订阅服务的时候，就会在其channel上绑定其订阅的信息
     * @param channel
     * @return
     */
    private boolean isChannelSubscribeOnServiceMeta(String serviceName, Channel channel) {
        ConcurrentSet<String> serviceMetaSet = channel.attr(S_SUBSCRIBE_KEY).get();

        return serviceMetaSet != null && serviceMetaSet.contains(serviceName);
    }

    /**
     * 构建返回给consumer的返回主体对象
     * @param meta
     * @param subcribeResultCustomBody
     * @param loadBalanceStrategy
     */
    private void buildSubcribeResultCustomBody(RegisterMeta meta, SubcribeResultCustomBody subcribeResultCustomBody, LoadBalanceStrategy loadBalanceStrategy) {

        LoadBalanceStrategy defaultBalanceStrategy = defaultRegistry.getRegistryConfig().getDefaultLoadBalanceStrategy();
        List<RegisterMeta> registerMetas = new ArrayList<>();

        registerMetas.add(meta);
        subcribeResultCustomBody.setLoadBalanceStrategy(loadBalanceStrategy == null ? defaultBalanceStrategy : loadBalanceStrategy);
        subcribeResultCustomBody.setRegisterMeta(registerMetas);
    }

    private void pushMessageToConsumer(Transporter sendConsumerRemotingTrasnporter, String serviceName)
            throws RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        // 所有的订阅者的channel集合
        if (!subscriberChannels.isEmpty()) {
            for (Channel channel : subscriberChannels) {
                if (isChannelSubscribeOnServiceMeta(serviceName, channel)) {
                    Transporter remotingTransporter = defaultRegistry.getServer().invokeSync(channel, sendConsumerRemotingTrasnporter, 3000L);

                    // 如果是ack返回是null说明是超时了，需要重新发送
                    if (remotingTransporter == null) {
                        logger.warn("push consumer message time out,need send again");
                        MessageNonAck msgNonAck = new MessageNonAck(remotingTransporter, channel, serviceName);
                        messagesNonAcks.add(msgNonAck);
                    }
                    // 如果消费者端消费者消费失败
                    AckCustomBody ackCustomBody = (AckCustomBody) remotingTransporter.getContent();
                    if (!ackCustomBody.isSuccess()) {
                        logger.warn("consumer fail handler this message");
                        MessageNonAck msgNonAck = new MessageNonAck(remotingTransporter, channel, serviceName);
                        messagesNonAcks.add(msgNonAck);
                    }
                }
            }
        }
    }


    static class MessageNonAck {

        private final long id;

        private final String serviceName;
        private final Transporter msg;
        private final Channel channel;

        public MessageNonAck(Transporter msg, Channel channel, String serviceName) {
            this.msg = msg;
            this.channel = channel;
            this.serviceName = serviceName;

            id = msg.getRequestId();
        }

        public long getId() {
            return id;
        }

        public Transporter getMsg() {
            return msg;
        }

        public Channel getChannel() {
            return channel;
        }

        public String getServiceName() {
            return serviceName;
        }

    }

}
