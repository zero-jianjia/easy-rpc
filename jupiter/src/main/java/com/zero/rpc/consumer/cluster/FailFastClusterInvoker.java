package com.zero.rpc.consumer.cluster;

import com.zero.rpc.Request;
import com.zero.rpc.consumer.cluster.ClusterInvoker;
import com.zero.rpc.consumer.dispatcher.Dispatcher;
import com.zero.rpc.consumer.future.InvokeFuture;

/**
 * 快速失败, 只发起一次调用, 失败立即报错(jupiter缺省设置)
 *
 * 通常用于非幂等性的写操作.
 *
 */
public class FailFastClusterInvoker implements ClusterInvoker {

    private final Dispatcher dispatcher;

    public FailFastClusterInvoker(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public Strategy strategy() {
        return Strategy.FAIL_FAST;
    }

    @Override
    public <T> InvokeFuture<T> invoke(Request request, Class<T> returnType) throws Exception {
        return dispatcher.dispatch(request, returnType);
    }
}
