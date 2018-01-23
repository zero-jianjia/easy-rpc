package com.zero.transport.api.channel;


import com.zero.transport.Directory;
import com.zero.transport.UnresolvedAddress;

import java.util.List;

/**
 * Based on the same address of the channel group.
 *
 * 要注意的是它管理的是相同地址的Channel.
 *
 */
public interface ChannelGroup {

    UnresolvedAddress remoteAddress();

    Channel next();

    List<? extends Channel> channels();

    boolean isEmpty();

    boolean add(Channel channel);

    boolean remove(Channel channel);

    int size();

    void setCapacity(int capacity);

    int getCapacity();

    boolean isAvailable();

    boolean waitForAvailable(long timeoutMillis);

    int getWeight(Directory directory);

    void setWeight(Directory directory, int weight);

    void removeWeight(Directory directory);

    int getWarmUp();

    void setWarmUp(int warmUp);

    boolean isWarmUpComplete();

    long timestamp();

    long deadlineMillis();
}
