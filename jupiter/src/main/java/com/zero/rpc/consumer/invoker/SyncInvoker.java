package com.zero.rpc.consumer.invoker;

import com.zero.rpc.consumer.dispatcher.Dispatcher;
import com.zero.rpc.model.ClusterStrategyConfig;
import com.zero.rpc.model.ServiceMetaData;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import com.zero.rpc.model.MethodSpecialConfig;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Synchronous call.
 *
 * 同步调用.
 *
 */
public class SyncInvoker extends AbstractInvoker {

    public SyncInvoker(String appName,
                       ServiceMetaData metadata,
                       Dispatcher dispatcher,
                       ClusterStrategyConfig defaultStrategy,
                       List<MethodSpecialConfig> methodSpecialConfigs) {
        super(appName, metadata, dispatcher, defaultStrategy, methodSpecialConfigs);
    }

    @RuntimeType
    public Object invoke(@Origin Method method, @AllArguments @RuntimeType Object[] args) throws Throwable {
        return doInvoke(method.getName(), args, method.getReturnType(), true);
    }
}
