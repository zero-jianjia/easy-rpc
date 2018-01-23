package com.zero.rpc.consumer.cluster;


import com.zero.rpc.Request;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.zero.common.util.Reflects;
import com.zero.rpc.Listener;
import com.zero.rpc.consumer.dispatcher.DefaultDispatcher;
import com.zero.rpc.consumer.dispatcher.Dispatcher;
import com.zero.rpc.consumer.future.FailOverInvokeFuture;
import com.zero.rpc.consumer.future.InvokeFuture;
import org.zero.rpc.exception.JupiterBadRequestException;
import org.zero.rpc.exception.JupiterBizException;
import org.zero.rpc.exception.JupiterRemoteException;
import org.zero.rpc.exception.JupiterSerializationException;
import com.zero.rpc.model.MessageWrapper;

import static org.zero.common.util.Preconditions.checkArgument;
import static org.zero.common.util.StackTraceUtil.stackTrace;

/**
 * 失败自动切换, 当出现失败, 重试其它服务器, 要注意的是重试会带来更长的延时.
 * <p>
 * 建议只用于幂等性操作, 通常比较合适用于读操作.
 * <p>
 */
public class FailOverClusterInvoker implements ClusterInvoker {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(FailOverClusterInvoker.class);

    private final Dispatcher dispatcher;
    private final int retries; // 重试次数, 不包含第一次

    public FailOverClusterInvoker(Dispatcher dispatcher, int retries) {
        checkArgument(dispatcher instanceof DefaultDispatcher,
                Reflects.simpleClassName(dispatcher) + " is unsupported [FailOverClusterInvoker]");

        this.dispatcher = dispatcher;
        if (retries >= 0) {
            this.retries = retries;
        } else {
            this.retries = 2;
        }
    }

    @Override
    public Strategy strategy() {
        return Strategy.FAIL_OVER;
    }

    @Override
    public <T> InvokeFuture<T> invoke(Request request, Class<T> returnType) throws Exception {
        FailOverInvokeFuture<T> future = FailOverInvokeFuture.of(returnType);

        int tryCount = retries + 1;
        invoke0(request, returnType, tryCount, future, null);

        return future;
    }

    private <T> void invoke0(final Request request,
            final Class<T> returnType,
            final int tryCount,
            final FailOverInvokeFuture<T> future,
            Throwable lastCause) {

        if (tryCount > 0 && isFailoverNeeded(lastCause)) {
            InvokeFuture<T> f = dispatcher.dispatch(request, returnType);

            f.addListener(new Listener<T>() {

                @Override
                public void complete(T result) {
                    future.setSuccess(result);
                }

                @Override
                public void failure(Throwable cause) {
                    if (logger.isWarnEnabled()) {
                        MessageWrapper message = request.message();
                        logger.warn("[Fail-over] retry, [{}] attempts left, [method: {}], [metadata: {}], {}.",
                                tryCount - 1,
                                message.getMethodName(),
                                message.getMetadata(),
                                stackTrace(cause));
                    }

                    invoke0(request, returnType, tryCount - 1, future, cause);
                }
            });
        } else {
            future.setFailure(lastCause);
        }
    }

    private static boolean isFailoverNeeded(Throwable cause) {
        return cause == null
                || cause instanceof JupiterRemoteException
                && !(cause instanceof JupiterBadRequestException)
                && !(cause instanceof JupiterBizException)
                && !(cause instanceof JupiterSerializationException);

    }
}
