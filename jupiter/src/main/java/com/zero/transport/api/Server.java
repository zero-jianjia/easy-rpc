package com.zero.transport.api;


import com.zero.transport.api.processor.ProviderProcessor;

import java.net.SocketAddress;

/**
 * Server acceptor.
 * 注意 Server 单例即可, 不要创建多个实例.
 */
public interface Server {

    SocketAddress localAddress();

    /**
     * Returns bound port.
     */
    int boundPort();

    /**
     * Binds the rpc processor.
     */
    void withProcessor(ProviderProcessor processor);

    void start() throws InterruptedException;

    void start(boolean sync) throws InterruptedException;

    void shutdownGracefully();

    /**
//     * Acceptor options [parent, child].
//     */
//    JConfigGroup configGroup();
}
