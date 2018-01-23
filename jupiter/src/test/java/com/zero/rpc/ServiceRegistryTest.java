package com.zero.rpc;

import com.zero.rpc.impl.TracingService;
import com.zero.rpc.impl.TracingService2Impl;
import com.zero.rpc.model.ServiceRegistry;
import org.junit.Test;

public class ServiceRegistryTest {

    @Test
    public void test() {
        TracingService service = new TracingService2Impl();
        ServiceRegistry serviceRegistry = new ServiceRegistry.Builder().provider(service).build();

        System.out.println(serviceRegistry.getProviderName());
    }
}
