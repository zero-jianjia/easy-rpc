package com.zero.easyrpc.transport.netty4;

import com.zero.easyrpc.common.utils.StandardThreadExecutor;
import com.zero.easyrpc.transport.NetUtils;
import com.zero.easyrpc.transport.api.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

/**
 * NettyServer
 */
public class NettyServer extends AbstractServer {

    private Map<String, Channel> channels; // <ip:port, channel>

    private ServerConfig serverConfig;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap serverBootstrap;
    private io.netty.channel.Channel serverChannel;

    private StandardThreadExecutor standardThreadExecutor = null;
    private ChannelHandler handler = null;

    public NettyServer(ServerConfig serverConfig, Codec codec, ChannelHandler handler) {
        super(codec);
        this.serverConfig = serverConfig;
        this.handler = handler;
    }

    public void doOpen() throws Throwable {
        serverBootstrap = new ServerBootstrap();

        bossGroup = initEventLoopGroup(1, new DefaultThreadFactory("NettyServerBoss", true));
        if (bossGroup instanceof EpollEventLoopGroup) {
            ((EpollEventLoopGroup) bossGroup).setIoRatio(80);
        } else if (bossGroup instanceof NioEventLoopGroup) {
            ((NioEventLoopGroup) bossGroup).setIoRatio(80);
        }
        workerGroup = initEventLoopGroup(serverConfig.getWorkerThreads(), new DefaultThreadFactory("NettyServerWorker", false));
        if (workerGroup instanceof EpollEventLoopGroup) {
            ((EpollEventLoopGroup) workerGroup).setIoRatio(80);
        } else if (workerGroup instanceof NioEventLoopGroup) {
            ((NioEventLoopGroup) workerGroup).setIoRatio(80);
        }
        serverBootstrap.group(bossGroup, workerGroup);

        standardThreadExecutor = (standardThreadExecutor != null && !standardThreadExecutor.isShutdown()) ? standardThreadExecutor
                : new StandardThreadExecutor(10, 200, 100, new DefaultThreadFactory("NettyServer-Handler", false));
        standardThreadExecutor.prestartAllCoreThreads();

        final NettyServerChannelHandler nettyServerHandler = new NettyServerChannelHandler(handler, standardThreadExecutor);
        channels = nettyServerHandler.getChannels();

        if (isNativeEt()) {
            serverBootstrap.channel(EpollServerSocketChannel.class);
        } else {
            serverBootstrap.channel(NioServerSocketChannel.class);
        }

        serverBootstrap.option(ChannelOption.SO_BACKLOG, 32768)
                .option(ChannelOption.SO_REUSEADDR, true);

        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ch.pipeline()
//                        .addLast("logging", new LoggingHandler(LogLevel.INFO))//for debug
                        .addLast("decoder", new NettyDecoder(getCodec(), 10))
                        .addLast("encoder", new NettyEncoder(getCodec()))
                        .addLast("handler", nettyServerHandler);
            }
        });
        // bind
        ChannelFuture channelFuture = serverBootstrap.bind(serverConfig.getListenPort());
        channelFuture.syncUninterruptibly();
        serverChannel = channelFuture.channel();
    }

    protected void doClose() throws Throwable {
        try {
            if (serverChannel != null) {
                serverChannel.close();
            }
        } catch (Throwable e) {
        }
        try {
            Collection<Channel> channels = getChannels();
            if (channels != null && channels.size() > 0) {
                for (Channel channel : channels) {
                    try {
                        channel.close();
                    } catch (Throwable e) {
                    }
                }
            }
        } catch (Throwable e) {
        }
        try {
            if (serverBootstrap != null) {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
                bossGroup = null;
                workerGroup = null;
            }
        } catch (Throwable e) {
        }
        try {
            if (channels != null) {
                channels.clear();
            }
        } catch (Throwable e) {
        }
    }

    public Collection<Channel> getChannels() {
        Collection<Channel> chs = new HashSet<>();
        for (Channel channel : this.channels.values()) {
            if (channel.isConnected()) {
                chs.add(channel);
            } else {
                channels.remove(NetUtils.toAddressString(channel.getRemoteAddress()));
            }
        }
        return chs;
    }

    public Channel getChannel(InetSocketAddress remoteAddress) {
        return channels.get(NetUtils.toAddressString(remoteAddress));
    }

    public boolean isBound() {
        return serverChannel.isActive();
    }


    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public void close(int timeout) {

    }

    @Override
    public boolean isClosed() {
        return false;
    }


    public static EventLoopGroup initEventLoopGroup(int nWorkers, ThreadFactory workerFactory) {
        return isNativeEt() ? new EpollEventLoopGroup(nWorkers, workerFactory) : new NioEventLoopGroup(nWorkers, workerFactory);
    }

    public static boolean isNativeEt() {
        return NettyNativeSupport.isSupportNativeET();
    }

}