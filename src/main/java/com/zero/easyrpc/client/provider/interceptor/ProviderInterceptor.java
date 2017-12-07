package com.zero.easyrpc.client.provider.interceptor;

/**
 * Created by jianjia1 on 17/12/04.
 */
public interface ProviderInterceptor {

    void beforeInvoke(String methodName, Object[] args);

    void afterInvoke(String methodName, Object[] args, Object result);

}
