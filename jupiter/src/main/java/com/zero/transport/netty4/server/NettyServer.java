package com.zero.transport.netty4.server;


import com.zero.transport.api.processor.ProviderProcessor;
import com.zero.transport.netty4.handler.NettyDecoder;
import com.zero.transport.netty4.handler.NettyEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class NettyServer extends AbstractNettyServer {
    private static final int DEFAULT_ACCEPTOR_PORT = 9000;

    // handlers
    private final AcceptorHandler handler = new AcceptorHandler();

    public NettyServer() {
        super(new InetSocketAddress(DEFAULT_ACCEPTOR_PORT));
    }

    public NettyServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress) {
        ServerBootstrap boot = bootstrap();

        final NettyEncoder encoder = new NettyEncoder();

        boot.childHandler(new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(
                        new NettyDecoder(),
                        encoder,
                        handler);
            }
        });

        return boot.bind(localAddress);
    }

    @Override
    public void withProcessor(ProviderProcessor processor) {
        handler.processor(processor);
    }

}
