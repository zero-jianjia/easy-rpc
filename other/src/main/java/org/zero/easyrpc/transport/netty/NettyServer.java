package org.zero.easyrpc.transport.netty;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.zero.easyrpc.transport.netty.handler.NettyDecoder;
import org.zero.easyrpc.transport.netty.handler.NettyEncoder;
import org.zero.easyrpc.transport.netty.handler.ServerHandler;

import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;

/**
 * NettyServer
 */
public class NettyServer {
    private ServerBootstrap bootstrap;
    private EventLoopGroup boss;
    private EventLoopGroup worker;


    protected SocketAddress localAddress;


    public void init() {
        ThreadFactory bossFactory = bossThreadFactory("boss");
        ThreadFactory workerFactory = workerThreadFactory("worker");
        boss = initEventLoopGroup(1, bossFactory);
        worker = initEventLoopGroup(18, workerFactory);
        bootstrap = new ServerBootstrap().group(boss, worker);

        if (NativeSupport.isNativeEPollAvailable()) {
            bootstrap.channel(EpollServerSocketChannel.class);
        } else if (NativeSupport.isNativeKQueueAvailable()) {
            bootstrap.channel(KQueueServerSocketChannel.class);
        } else {
            bootstrap.channel(NioServerSocketChannel.class);
        }

        bootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(
                        new NettyDecoder(),
                        new NettyEncoder(),
                        new ServerHandler());
            }
        });

        ChannelFuture future = bootstrap.bind(9100);
        // wait until the server socket is closed.
        try {
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    protected ThreadFactory bossThreadFactory(String name) {
        return new DefaultThreadFactory(name, Thread.MAX_PRIORITY);
    }

    protected ThreadFactory workerThreadFactory(String name) {
        return new DefaultThreadFactory(name, Thread.MAX_PRIORITY);
    }


    protected EventLoopGroup initEventLoopGroup(int nThreads, ThreadFactory tFactory) {

        if (NativeSupport.isNativeEPollAvailable()) {
            return new EpollEventLoopGroup(nThreads, tFactory);
        } else if (NativeSupport.isNativeEPollAvailable()) {
            return new KQueueEventLoopGroup(nThreads, tFactory);
        } else {
            return new NioEventLoopGroup(nThreads, tFactory);
        }
    }

}