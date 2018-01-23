package com.zero.easyrpc.proxy;

import java.lang.reflect.InvocationHandler;

public interface ProxyFactory {

    <T> T getProxy(Class<T> clz, InvocationHandler invocationHandler);

}
