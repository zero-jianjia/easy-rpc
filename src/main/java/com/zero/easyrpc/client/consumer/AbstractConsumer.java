package com.zero.easyrpc.client.consumer;

/**
 * Created by jianjia1 on 17/12/07.
 */

import com.zero.easyrpc.client.loadbalance.LoadBalanceStrategies;
import com.zero.easyrpc.common.loadbalance.LoadBalanceStrategy;
import com.zero.easyrpc.common.utils.ChannelGroup;
import com.zero.easyrpc.common.utils.UnresolvedAddress;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 消费端的抽象类，这个类的意义：
 * 1)保存从注册中心获取到的每个服务的提供者的信息
 * 2)保存每一个服务的负载均衡的策略
 */
public abstract class AbstractConsumer implements Consumer {

    /**
     * 服务名对应的ChannelGroup组
     */
    private volatile static Map<String, CopyOnWriteArrayList<ChannelGroup>> serverGroups = new ConcurrentHashMap<>();

    /**
     * 服务提地址对应的ChannelGroup组
     */
    protected final Map<UnresolvedAddress, ChannelGroup> addressGroups = new ConcurrentHashMap<>();

    /**
     * 服务名对应的负载均衡的策略
     */
    protected final Map<String, LoadBalanceStrategy> balanceStrategyMap = new ConcurrentHashMap<>();

    /**
     * 为某个服务增加一个ChannelGroup
     * @param serviceName
     * @param group
     */
    public static boolean addIfAbsent(String serviceName, ChannelGroup group) {
        CopyOnWriteArrayList<ChannelGroup> groupList = serverGroups.computeIfAbsent(serviceName, key -> new CopyOnWriteArrayList<>());
        return groupList.addIfAbsent(group);
    }

    /**
     * 当某个group 失效或者下线的时候，将其冲value中移除
     * @param serviceName
     * @param group
     */
    public static boolean removedIfAbsent(String serviceName, ChannelGroup group) {
        String _serviceName = serviceName;
        CopyOnWriteArrayList<ChannelGroup> groupList = serverGroups.get(_serviceName);
        if (groupList == null) {
            return false;
        }
        return groupList.remove(group);
    }

    public static CopyOnWriteArrayList<ChannelGroup> getChannelGroupByServiceName(String service) {
        return serverGroups.get(service);
    }

    @Override
    public void setServiceLoadBalanceStrategy(String serviceName, LoadBalanceStrategy loadBalanceStrategy) {
        LoadBalanceStrategy balanceStrategy = balanceStrategyMap.get(serviceName);
        if (balanceStrategy == null) {
            balanceStrategy = LoadBalanceStrategy.WEIGHTINGRANDOM;
            balanceStrategyMap.put(serviceName, balanceStrategy);
        }
        balanceStrategy = loadBalanceStrategy;
    }

    public static Map<String, CopyOnWriteArrayList<ChannelGroup>> getServerGroups() {
        return serverGroups;
    }

    @Override
    public ChannelGroup loadBalance(String serviceName, LoadBalanceStrategy directBalanceStrategy) {
        LoadBalanceStrategy balanceStrategy = balanceStrategyMap.get(serviceName);

        CopyOnWriteArrayList<ChannelGroup> list = serverGroups.get(serviceName);
        if (balanceStrategy == null) {
            if (directBalanceStrategy == null) {
                balanceStrategy = LoadBalanceStrategy.WEIGHTINGRANDOM;
            } else {
                balanceStrategy = directBalanceStrategy;
            }
        }

        if (null == list || list.size() == 0) {
            return null;
        }
        switch (balanceStrategy) {
            case RANDOM:
                return LoadBalanceStrategies.RANDOM.select(list);
            case WEIGHTINGRANDOM:
                return LoadBalanceStrategies.WEIGHTRANDOM.select(list);
            case ROUNDROBIN:
                return LoadBalanceStrategies.ROUNDROBIN.select(list);
            default:
                break;
        }
        return null;
    }

}
