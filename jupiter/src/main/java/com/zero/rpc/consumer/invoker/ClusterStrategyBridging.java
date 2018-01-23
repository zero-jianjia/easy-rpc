package com.zero.rpc.consumer.invoker;

import org.zero.common.util.Maps;
import com.zero.rpc.consumer.cluster.ClusterInvoker;
import com.zero.rpc.consumer.cluster.FailFastClusterInvoker;
import com.zero.rpc.consumer.cluster.FailOverClusterInvoker;
import com.zero.rpc.consumer.cluster.FailSafeClusterInvoker;
import com.zero.rpc.consumer.dispatcher.Dispatcher;
import com.zero.rpc.model.ClusterStrategyConfig;
import com.zero.rpc.model.MethodSpecialConfig;

import java.util.List;
import java.util.Map;

public class ClusterStrategyBridging {

    private final ClusterInvoker defaultClusterInvoker;
    private final Map<String, ClusterInvoker> methodSpecialClusterInvokerMapping;

    public ClusterStrategyBridging(Dispatcher dispatcher,
                                   ClusterStrategyConfig defaultStrategy,
                                   List<MethodSpecialConfig> methodSpecialConfigs) {

        this.defaultClusterInvoker = createClusterInvoker(dispatcher, defaultStrategy);
        this.methodSpecialClusterInvokerMapping = Maps.newHashMap();

        for (MethodSpecialConfig config : methodSpecialConfigs) {
            ClusterStrategyConfig strategy = config.getStrategy();
            if (strategy != null) {
                methodSpecialClusterInvokerMapping.put(
                        config.getMethodName(),
                        createClusterInvoker(dispatcher, strategy)
                );
            }
        }
    }

    public ClusterInvoker findClusterInvoker(String methodName) {
        ClusterInvoker invoker = methodSpecialClusterInvokerMapping.get(methodName);
        return invoker != null ? invoker : defaultClusterInvoker;
    }

    private ClusterInvoker createClusterInvoker(Dispatcher dispatcher, ClusterStrategyConfig strategy) {
        ClusterInvoker.Strategy s = strategy.getStrategy();
        switch (s) {
            case FAIL_FAST:
                return new FailFastClusterInvoker(dispatcher);
            case FAIL_OVER:
                return new FailOverClusterInvoker(dispatcher, strategy.getFailoverRetries());
            case FAIL_SAFE:
                return new FailSafeClusterInvoker(dispatcher);
            default:
                throw new UnsupportedOperationException("strategy: " + strategy);
        }
    }
}
