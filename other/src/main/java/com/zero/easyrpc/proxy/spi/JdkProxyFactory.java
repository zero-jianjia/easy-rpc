package com.zero.easyrpc.proxy.spi;


import com.zero.easyrpc.proxy.ProxyFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * jdk proxy
 * 
 *
 */
public class JdkProxyFactory implements ProxyFactory {

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clz, InvocationHandler invocationHandler) {
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] {clz}, invocationHandler);
    }

}
