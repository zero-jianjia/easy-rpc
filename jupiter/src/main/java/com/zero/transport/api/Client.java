package com.zero.transport.api;


import com.zero.transport.Directory;
import com.zero.transport.api.channel.ChannelGroup;
import com.zero.transport.UnresolvedAddress;
import com.zero.transport.api.channel.CopyOnWriteGroupList;
import com.zero.transport.api.channel.DirectoryChannelGroup;
import com.zero.transport.api.processor.ConsumerProcessor;

import java.util.Collection;

/**
 * 注意 Client 单例即可, 不要创建多个实例.
 */
public interface Client<C> {

    /**
     * Binds the rpc processor.
     */
    void withProcessor(ConsumerProcessor processor);

    /**
     * Connects to the remote peer.
     */
    C connect(UnresolvedAddress address);

    /**
     * Connects to the remote peer.
     */
    C connect(UnresolvedAddress address, boolean async);

    ChannelGroup group(UnresolvedAddress address);

    /**
     * Returns all {@link ChannelGroup}s.
     */
    Collection<ChannelGroup> groups();

    /**
     * Adds a {@link ChannelGroup} by {@link Directory}.
     */
    boolean addChannelGroup(Directory directory, ChannelGroup group);

    /**
     * Removes a {@link ChannelGroup} by {@link Directory}.
     */
    boolean removeChannelGroup(Directory directory, ChannelGroup group);

    /**
     * Returns list of {@link ChannelGroup}s by the same {@link Directory}.
     */
    CopyOnWriteGroupList directory(Directory directory);

    /**
     * Returns {@code true} if has available {@link ChannelGroup}s
     * on this {@link Directory}.
     */
    boolean isDirectoryAvailable(Directory directory);

    /**
     * Returns the {@link DirectoryChannelGroup}.
     */
    DirectoryChannelGroup directoryGroup();

    /**
     * Returns the {@link ConnectionManager}.
     */
    ConnectionManager connectionManager();

    /**
     * Shutdown the server.
     */
    void shutdownGracefully();

    interface ConnectionWatcher {

        /**
         * Start to connect to server.
         */
        void start();

        /**
         * Wait until the connections is available or timeout,
         * if available return true, otherwise return false.
         */
        boolean waitForAvailable(long timeoutMillis);
    }
}
