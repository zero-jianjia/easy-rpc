package com.zero.rpc.consumer.future;


import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import com.zero.rpc.Listener;

import static org.zero.common.util.StackTraceUtil.stackTrace;

/**
 * 用于实现failover集群容错方案的 {@link InvokeFuture}.
 *
 */
public class FailOverInvokeFuture<V> extends AbstractListenableFuture<V> implements InvokeFuture<V> {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(FailOverInvokeFuture.class);

    private final Class<V> returnType;

    public static <T> FailOverInvokeFuture<T> of(Class<T> returnType) {
        return new FailOverInvokeFuture<>(returnType);
    }

    private FailOverInvokeFuture(Class<V> returnType) {
        this.returnType = returnType;
    }

    public void setSuccess(V result) {
        set(result);
    }

    public void setFailure(Throwable cause) {
        setException(cause);
    }

    @Override
    public Class<V> returnType() {
        return returnType;
    }

    @Override
    public V getResult() throws Throwable {
        return get();
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
}
