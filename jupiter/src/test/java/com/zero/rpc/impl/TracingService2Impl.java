package com.zero.rpc.impl;

import com.zero.rpc.ServiceProviderImpl;

@ServiceProviderImpl(version = "1.0.0")
public class TracingService2Impl implements TracingService {

    @Override
    public String call2(String text) {
        return "Hello call2 [" + text + "]";
    }
}
