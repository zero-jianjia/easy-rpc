package com.zero.rpc.consumer.loadbalance;

import com.zero.transport.Directory;
import com.zero.transport.api.channel.ChannelGroup;
import org.zero.common.util.SystemClock;

public abstract class AbstractLoadBalancer implements LoadBalancer {

    private static final ThreadLocal<WeightArray> weightsThreadLocal = new ThreadLocal<WeightArray>() {

        @Override
        protected WeightArray initialValue() {
            return new WeightArray();
        }
    };

    protected WeightArray weightArray(int length) {
        return weightsThreadLocal.get().refresh(length);
    }

    // 计算权重, 包含预热逻辑
    protected int getWeight(ChannelGroup group, Directory directory) {
        int weight = group.getWeight(directory);
        int warmUp = group.getWarmUp();
        int upTime = (int) (SystemClock.millisClock().now() - group.timestamp());

        if (upTime > 0 && upTime < warmUp) {
            // 对端服务预热中, 计算预热权重
            weight = (int) (((float) upTime / warmUp) * weight);
        }

        return weight > 0 ? weight : 0;
    }
}
