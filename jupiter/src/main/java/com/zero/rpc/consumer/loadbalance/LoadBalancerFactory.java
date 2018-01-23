package com.zero.rpc.consumer.loadbalance;

public final class LoadBalancerFactory {

    public static LoadBalancer loadBalancer(LoadBalancerType type) {
        if (type == LoadBalancerType.RANDOM) {
            return RandomLoadBalancer.instance();
        }

        if (type == LoadBalancerType.ROUND_ROBIN) {
            return RoundRobinLoadBalancer.instance();
        }

        // 如果不指定, 默认的负载均衡算法是加权随机
        return RandomLoadBalancer.instance();
    }

    private LoadBalancerFactory() {}
}
