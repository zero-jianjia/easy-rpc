/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zero.easyrpc.remoting.netty4;

import com.zero.easyrpc.common.utils.StandardThreadExecutor;
import com.zero.easyrpc.netty4.Server;
import com.zero.easyrpc.remoting.api.AbstractServer;
import com.zero.easyrpc.remoting.api.Channel;
import com.zero.easyrpc.remoting.NetUtils;
import com.zero.easyrpc.remoting.api.ChannelHandler;
import com.zero.easyrpc.remoting.api.RemotingException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

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

    public NettyServer(ServerConfig serverConfig, ChannelHandler handler) throws RemotingException {
        this.serverConfig = serverConfig;
    }

    public void doOpen() throws Throwable {

        serverBootstrap = new ServerBootstrap();

        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("NettyServerBoss", true));
        workerGroup = new NioEventLoopGroup(serverConfig.getWorkerThreads(), new DefaultThreadFactory("NettyServerWorker", true));


        final NettyServerHandler nettyServerHandler = new NettyServerHandler();
        channels = nettyServerHandler.getChannels();

        standardThreadExecutor = (standardThreadExecutor != null && !standardThreadExecutor.isShutdown()) ? standardThreadExecutor
                : new StandardThreadExecutor(10, 200, 100, new DefaultThreadFactory("NettyServer-Handler", true));
        standardThreadExecutor.prestartAllCoreThreads();

        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_TIMEOUT, 100)
                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_REUSEADDR, true)

                .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)

                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast("decoder", new NettyDecoder())
                                .addLast("encoder", new NettyEncoder())
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
}