package com.zero.rpc.consumer.invoker;


import com.zero.rpc.Request;
import com.zero.rpc.consumer.cluster.ClusterInvoker;
import com.zero.rpc.consumer.dispatcher.Dispatcher;
import com.zero.rpc.consumer.future.InvokeFuture;
import com.zero.rpc.model.ClusterStrategyConfig;
import com.zero.rpc.model.MessageWrapper;
import com.zero.rpc.model.ServiceMetaData;
import com.zero.rpc.model.MethodSpecialConfig;
import com.zero.rpc.tracing.TraceId;
import com.zero.rpc.tracing.TracingUtil;

import java.util.List;

public abstract class AbstractInvoker {

    private final String appName;
    private final ServiceMetaData metadata; // 目标服务元信息
    private final ClusterStrategyBridging clusterStrategyBridging;

    public AbstractInvoker(String appName,
            ServiceMetaData metadata,
            Dispatcher dispatcher,
            ClusterStrategyConfig defaultStrategy,
            List<MethodSpecialConfig> methodSpecialConfigs) {
        this.appName = appName;
        this.metadata = metadata;
        clusterStrategyBridging = new ClusterStrategyBridging(dispatcher, defaultStrategy, methodSpecialConfigs);
    }

    protected Object doInvoke(String methodName, Object[] args, Class<?> returnType, boolean sync) throws Throwable {
        Request request = createRequest(methodName, args);
        ClusterInvoker invoker = clusterStrategyBridging.findClusterInvoker(methodName);
        // invoke
        InvokeFuture<?> future = invoker.invoke(request, returnType);

        if (sync) {
            return future.getResult();
        } else {
            return future;
        }
    }

    private Request createRequest(String methodName, Object[] args) {
        MessageWrapper message = new MessageWrapper(metadata);
        message.setAppName(appName);
        message.setMethodName(methodName);
        message.setArgs(args);
        message.setTraceId(getTraceId());

        Request request = new Request();
        request.message(message);

        return request;
    }

    private TraceId getTraceId() {
        if (TracingUtil.isTracingNeeded()) {
            TraceId traceId = TracingUtil.getCurrent();
            if (traceId == TraceId.NULL_TRACE_ID) {
                traceId = TraceId.newInstance(TracingUtil.generateTraceId());
            }
            return traceId;
        }
        return null;
    }
}
