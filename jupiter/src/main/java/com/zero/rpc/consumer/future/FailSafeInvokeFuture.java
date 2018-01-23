package com.zero.rpc.consumer.future;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.zero.common.util.Reflects;
import com.zero.rpc.Listener;

import static org.zero.common.util.StackTraceUtil.stackTrace;

/**
 * 用于实现fail-safe集群容错方案的 {@link InvokeFuture}.
 *
 * 同步调用时发生异常时只打印日志.
 *
 */
@SuppressWarnings("unchecked")
public class FailSafeInvokeFuture<V> implements InvokeFuture<V> {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(FailSafeInvokeFuture.class);

    private final InvokeFuture<V> future;

    public static <T> FailSafeInvokeFuture<T> of(InvokeFuture<T> future) {
        return new FailSafeInvokeFuture<>(future);
    }

    private FailSafeInvokeFuture(InvokeFuture<V> future) {
        this.future = future;
    }

    @Override
    public Class<V> returnType() {
        return future.returnType();
    }

    @Override
    public V getResult() throws Throwable {
        try {
            return future.getResult();
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("Ignored exception on [Fail-safe]: {}.", stackTrace(t));
            }
        }
        return (V) Reflects.getTypeDefaultValue(returnType());
    }

    @Override
    public InvokeFuture<V> addListener(Listener<V> listener) {
        future.addListener(listener);
        return this;
    }

    @Override
    public InvokeFuture<V> addListeners(Listener<V>... listeners) {
        future.addListeners(listeners);
        return this;
    }

    @Override
    public InvokeFuture<V> removeListener(Listener<V> listener) {
        future.removeListener(listener);
        return this;
    }

    @Override
    public InvokeFuture<V> removeListeners(Listener<V>... listeners) {
        future.removeListeners(listeners);
        return this;
    }

    public InvokeFuture<V> future() {
        return future;
    }
}
