package com.zero.easyrpc.client.provider;

import com.zero.easyrpc.client.provider.interceptor.ProviderProxyHandler;
import com.zero.easyrpc.client.provider.model.ServiceWrapper;

import java.util.List;

/**
 * Created by jianjia1 on 17/12/04.
 */
public interface ServiceWrapperWorker {

    ServiceWrapperWorker provider(Object serviceProvider);

    ServiceWrapperWorker provider(ProviderProxyHandler proxyHandler,Object serviceProvider);

    List<ServiceWrapper> create();

}