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
 * Asynchronous call, {@link #invoke(Method, Object[])}
 * returns a default value of the corresponding method.
 *
 * 异步调用.
 *
 */
public class AsyncInvoker extends AbstractInvoker {

    public AsyncInvoker(String appName,
                        ServiceMetaData metadata,
                        Dispatcher dispatcher,
                        ClusterStrategyConfig defaultStrategy,
                        List<MethodSpecialConfig> methodSpecialConfigs) {
        super(appName, metadata, dispatcher, defaultStrategy, methodSpecialConfigs);
    }

    @RuntimeType
    public Object invoke(@Origin Method method, @AllArguments @RuntimeType Object[] args) throws Throwable {
        Class<?> returnType = method.getReturnType();

        Object result = doInvoke(method.getName(), args, returnType, false);

        return result;
    }
}
