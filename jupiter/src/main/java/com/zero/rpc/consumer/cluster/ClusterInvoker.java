package com.zero.rpc.consumer.cluster;

import com.zero.rpc.Request;
import com.zero.rpc.consumer.future.InvokeFuture;

public interface ClusterInvoker {

    /**
     * 集群容错策略
     */
    enum Strategy {
        FAIL_FAST,  // 快速失败
        FAIL_OVER,  // 失败重试
        FAIL_SAFE,  // 失败安全
        // FAIL_BACK,  没想到合适场景, 暂不支持
        // FORKING,    消耗资源太多, 暂不支持
        ;

        public static Strategy getDefault() {
            return FAIL_FAST;
        }
    }

    Strategy strategy();

    <T> InvokeFuture<T> invoke(Request request, Class<T> returnType) throws Exception;
}
