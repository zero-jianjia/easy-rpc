package com.zero.rpc.impl;

import com.zero.rpc.ServiceProvider;

/**
 * jupiter
 * org.jupiter.tracing.service
 *
 * @author jiachun.fjc
 */
@ServiceProvider(group = "group1")
public interface TracingService {

    String call2(String text);
}
