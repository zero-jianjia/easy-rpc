package com.zero.transport.netty4;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.ThreadFactory;

public class NettyUtil {

    public static EventLoopGroup initEventLoopGroup(int nThreads, ThreadFactory tFactory) {
        if (NativeSupport.isNativeEPollAvailable()) {
            return new EpollEventLoopGroup(nThreads, tFactory);
        }
        if (NativeSupport.isNativeKQueueAvailable()) {
            return new KQueueEventLoopGroup(nThreads, tFactory);
        }
        return new NioEventLoopGroup(nThreads, tFactory);
    }

    public static void initServerChannelFactory(ServerBootstrap bootstrap) {
        if (NativeSupport.isNativeEPollAvailable()) {
            bootstrap.channel(EpollServerSocketChannel.class);
        } else if (NativeSupport.isNativeKQueueAvailable()) {
            bootstrap.channel(KQueueServerSocketChannel.class);
        } else {
            bootstrap.channel(NioServerSocketChannel.class);
        }
    }


    public static void initClientChannelFactory(Bootstrap bootstrap) {
        if (NativeSupport.isNativeEPollAvailable()) {
            bootstrap.channel(EpollSocketChannel.class);
        } else if (NativeSupport.isNativeKQueueAvailable()) {
            bootstrap.channel(KQueueSocketChannel.class);
        } else {
            bootstrap.channel(NioSocketChannel.class);
        }
    }
}
