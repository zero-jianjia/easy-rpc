package com.zero.easyrpc.client.metrics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * RPC调用统计
 */
public class Meter {
    private final String serviceName;						  //服务名
    private AtomicLong callCount = new AtomicLong(0L);        //调用次数
    private AtomicLong failedCount = new AtomicLong(0L);	  //失败次数
    private AtomicLong totalCallTime = new AtomicLong(0L);    //总的调用时间
    private AtomicLong totalRequestSize = new AtomicLong(0L); //入参大小

    public Meter(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public AtomicLong getCallCount() {
        return callCount;
    }

    public AtomicLong getFailedCount() {
        return failedCount;
    }

    public AtomicLong getTotalCallTime() {
        return totalCallTime;
    }

    public AtomicLong getTotalRequestSize() {
        return totalRequestSize;
    }

}