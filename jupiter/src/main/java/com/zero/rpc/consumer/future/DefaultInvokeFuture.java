package com.zero.rpc.consumer.future;

import com.zero.rpc.Response;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.zero.common.util.Maps;
import org.zero.common.util.Signal;
import com.zero.rpc.ConsumerHook;
import com.zero.rpc.Listener;
import org.zero.rpc.exception.JupiterRemoteException;
import org.zero.rpc.exception.JupiterTimeoutException;
import com.zero.rpc.model.ResultWrapper;
import com.zero.transport.Status;
import com.zero.transport.api.channel.Channel;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static org.zero.common.util.Preconditions.checkNotNull;
import static org.zero.common.util.StackTraceUtil.stackTrace;

public class DefaultInvokeFuture<V> extends AbstractListenableFuture<V> implements InvokeFuture<V> {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultInvokeFuture.class);

    private static final long DEFAULT_TIMEOUT_NANOSECONDS = TimeUnit.MILLISECONDS.toNanos(3000);

    private static final ConcurrentMap<Long, DefaultInvokeFuture<?>> futures = Maps.newConcurrentMapLong();

    private final long invokeId; // request.invokeId, 广播的场景可以重复
    private final Channel channel;
    private final Class<V> returnType;
    private final long timeout;
    private final long startTime = System.nanoTime();

    private volatile boolean sent = false;

    private ConsumerHook[] hooks = ConsumerHook.EMPTY_HOOKS;

    public static <T> DefaultInvokeFuture<T> of(long invokeId, Channel channel, Class<T> returnType, long timeoutMillis) {
        return new DefaultInvokeFuture<>(invokeId, channel, returnType, timeoutMillis);
    }

    private DefaultInvokeFuture(long invokeId, Channel channel, Class<V> returnType, long timeoutMillis) {
        this.invokeId = invokeId;
        this.channel = channel;
        this.returnType = returnType;
        this.timeout = timeoutMillis > 0 ? TimeUnit.MILLISECONDS.toNanos(timeoutMillis) : DEFAULT_TIMEOUT_NANOSECONDS;

        futures.put(invokeId, this);
    }

    @Override
    public Class<V> returnType() {
        return returnType;
    }

    @Override
    public V getResult() throws Throwable {
        try {
            return get(timeout, TimeUnit.NANOSECONDS);
        } catch (Signal s) {
            SocketAddress address = channel.remoteAddress();
            if (s == TIMEOUT) {
                throw new JupiterTimeoutException(address, sent ? Status.SERVER_TIMEOUT : Status.CLIENT_TIMEOUT);
            } else {
                throw new JupiterRemoteException(s.name(), address);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void notifyListener0(Listener<V> listener, int state, Object x) {
        try {
            if (state == NORMAL) {
                listener.complete((V) x);
            } else {
                listener.failure((Throwable) x);
            }
        } catch (Throwable t) {
            logger.error("An exception was thrown by {}.{}, {}.",
                    listener.getClass().getName(), state == NORMAL ? "complete()" : "failure()", stackTrace(t));
        }
    }

    public void markSent() {
        sent = true;
    }

    public ConsumerHook[] hooks() {
        return hooks;
    }

    public DefaultInvokeFuture<V> withHooks(ConsumerHook[] hooks) {
        checkNotNull(hooks, "withHooks");

        this.hooks = hooks;
        return this;
    }

    @SuppressWarnings("all")
    private void doReceived(Response response) {
        byte status = response.status();

//        if (status == Status.OK.value()) {
        ResultWrapper wrapper = response.result();
        set((V) wrapper.getResult());
//        } else {
//            setException(status, response);
//        }

        // call hook's after method
        for (int i = 0; i < hooks.length; i++) {
            hooks[i].after(response, channel);
        }
    }

    public static void received(Channel channel, Response response) {
        long invokeId = response.id();
        DefaultInvokeFuture<?> future = futures.remove(invokeId);
        if (future == null) {
            logger.warn("A timeout response [{}] finally returned on {}.", response, channel);
            return;
        }

        future.doReceived(response);
    }

    private static String subInvokeId(Channel channel, long invokeId) {
        return channel.id() + invokeId;
    }

    // timeout scanner
    @SuppressWarnings("all")
    private static class TimeoutScanner implements Runnable {

        public void run() {
            for (; ; ) {
                try {
                    // round
                    for (DefaultInvokeFuture<?> future : futures.values()) {
                        process(future);
                    }
                } catch (Throwable t) {
                    logger.error("An exception was caught while scanning the timeout futures {}.", stackTrace(t));
                }

                try {
                    Thread.sleep(30);
                } catch (InterruptedException ignored) {
                }
            }
        }

        private void process(DefaultInvokeFuture<?> future) {
            if (future == null || future.isDone()) {
                return;
            }

            if (System.nanoTime() - future.startTime > future.timeout) {
                Response response = new Response(future.invokeId);
                response.status(future.sent ? Status.SERVER_TIMEOUT : Status.CLIENT_TIMEOUT);
                DefaultInvokeFuture.received(future.channel, response);
            }
        }
    }

    static {
        Thread t = new Thread(new TimeoutScanner(), "timeout.scanner");
        t.setDaemon(true);
        t.start();
    }
}
