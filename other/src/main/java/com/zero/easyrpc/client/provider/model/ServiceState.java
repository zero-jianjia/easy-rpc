package com.zero.easyrpc.client.provider.model;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 服务状态
 */
public class ServiceState {

    private AtomicBoolean degrade = new AtomicBoolean(false);    // 降级
    private AtomicBoolean rateLimit = new AtomicBoolean(true); // 限流
    private AtomicBoolean isAutoDegrade = new AtomicBoolean(false); // 是否已经开始自动降级
    private Integer minSuccecssRate = 90;                            // 服务最低的成功率，调用成功率低于多少开始自动降级

    public AtomicBoolean getDegrade() {
        return degrade;
    }

    public void setDegrade(AtomicBoolean degrade) {
        this.degrade = degrade;
    }

    public AtomicBoolean getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(AtomicBoolean rateLimit) {
        this.rateLimit = rateLimit;
    }

    public AtomicBoolean getIsAutoDegrade() {
        return isAutoDegrade;
    }

    public void setIsAutoDegrade(AtomicBoolean isAutoDegrade) {
        this.isAutoDegrade = isAutoDegrade;
    }

    public Integer getMinSuccecssRate() {
        return minSuccecssRate;
    }

    public void setMinSuccecssRate(Integer minSuccecssRate) {
        this.minSuccecssRate = minSuccecssRate;
    }
}
