package com.zero.rpc.model;

import java.io.Serializable;

/**
 * 方法名称
 * 集群容错策略
 */
public class MethodSpecialConfig implements Serializable {

    private static final long serialVersionUID = -3689442191636868738L;

    private final String methodName;

    private long timeoutMillis;
    private ClusterStrategyConfig strategy;

    public static MethodSpecialConfig of(String methodName) {
        return new MethodSpecialConfig(methodName);
    }

    private MethodSpecialConfig(String methodName) {
        this.methodName = methodName;
    }

    public MethodSpecialConfig timeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    public MethodSpecialConfig strategy(ClusterStrategyConfig strategy) {
        this.strategy = strategy;
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public ClusterStrategyConfig getStrategy() {
        return strategy;
    }

    public void setStrategy(ClusterStrategyConfig strategy) {
        this.strategy = strategy;
    }
}
