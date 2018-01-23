package com.zero.rpc.consumer.cluster;

import com.zero.rpc.Request;
import com.zero.rpc.consumer.dispatcher.Dispatcher;
import com.zero.rpc.consumer.future.FailSafeInvokeFuture;
import com.zero.rpc.consumer.future.InvokeFuture;

/**
 * 失败安全, 同步调用时发生异常时只打印日志.
 *
 * 通常用于写入审计日志等操作.
 *
 */
public class FailSafeClusterInvoker implements ClusterInvoker {

    private final Dispatcher dispatcher;

    public FailSafeClusterInvoker(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public Strategy strategy() {
        return Strategy.FAIL_SAFE;
    }

    @Override
    public <T> InvokeFuture<T> invoke(Request request, Class<T> returnType) throws Exception {
        InvokeFuture<T> future = dispatcher.dispatch(request, returnType);
        return FailSafeInvokeFuture.of(future);
    }
}
