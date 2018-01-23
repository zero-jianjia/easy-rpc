package com.zero.rpc.impl;

import com.zero.rpc.ServiceProviderImpl;

@ServiceProviderImpl(version = "1.0.1")
public class TracingService1Impl implements TracingService {

    @Override
    public String call2(String text) {
        return "Hello call1 [" + text + "]";
    }
}
