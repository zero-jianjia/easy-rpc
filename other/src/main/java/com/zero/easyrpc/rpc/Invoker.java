package com.zero.easyrpc.rpc;

import org.aopalliance.intercept.Invocation;

/**
 * 服务提供方
 * Created by zero on 2018/1/15.
 */
public interface Invoker<T> {

    Class<T> getInterface();

    Response invoke(Invocation invocation);


    void destroy();

}
