package com.zero.rpc.consumer.dispatcher;


import com.zero.rpc.ConsumerHook;
import com.zero.rpc.Request;
import com.zero.rpc.consumer.future.InvokeFuture;
import com.zero.rpc.model.MethodSpecialConfig;

import java.util.List;

/**
 * 分配器
 */
public interface Dispatcher {

    <T> InvokeFuture<T> dispatch(Request request, Class<T> returnType);

    Dispatcher hooks(List<ConsumerHook> hooks);

    Dispatcher timeoutMillis(long timeoutMillis);

    Dispatcher methodSpecialConfigs(List<MethodSpecialConfig> methodSpecialConfigs);
}
