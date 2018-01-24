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

    protected int getWeight(ChannelGroup group, Directory directory) {
        int weight = group.getWeight(directory);
        return weight > 0 ? weight : 0;
    }
}
