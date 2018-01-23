package com.zero.rpc.consumer.loadbalance;


import com.zero.transport.Directory;
import com.zero.transport.api.channel.CopyOnWriteGroupList;
import com.zero.transport.api.channel.ChannelGroup;

/**
 * Load balancer.
 */
public interface LoadBalancer {

    /**
     * Select one in elements list.
     *
     * @param groups    elements for select
     * @param directory service directory
     */
    ChannelGroup select(CopyOnWriteGroupList groups, Directory directory);
}
