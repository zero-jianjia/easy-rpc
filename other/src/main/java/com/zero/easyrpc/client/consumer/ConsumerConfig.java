package com.zero.easyrpc.client.consumer;

/**
 * 消费端配置属性
 * Created by jianjia1 on 17/12/07.
 */
public class ConsumerConfig {

    private int retryConnectionRegistryTimes = 4;

    private long maxRetryConnectionRegsitryTime = 5000;

    private long registryTimeout = 3000;

    public int getRetryConnectionRegistryTimes() {
        return retryConnectionRegistryTimes;
    }

    public void setRetryConnectionRegistryTimes(int retryConnectionRegistryTimes) {
        this.retryConnectionRegistryTimes = retryConnectionRegistryTimes;
    }

    public long getMaxRetryConnectionRegsitryTime() {
        return maxRetryConnectionRegsitryTime;
    }

    public void setMaxRetryConnectionRegsitryTime(long maxRetryConnectionRegsitryTime) {
        this.maxRetryConnectionRegsitryTime = maxRetryConnectionRegsitryTime;
    }

    public long getRegistryTimeout() {
        return registryTimeout;
    }

    public void setRegistryTimeout(long registryTimeout) {
        this.registryTimeout = registryTimeout;
    }


}
