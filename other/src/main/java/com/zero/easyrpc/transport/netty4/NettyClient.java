package com.zero.easyrpc.transport.netty4;

import com.zero.easyrpc.transport.NetUtils;
import com.zero.easyrpc.transport.api.*;
import com.zero.easyrpc.rpc.DefaultResponseFuture;
import com.zero.easyrpc.rpc.protocol.Request_001;
import com.zero.easyrpc.rpc.protocol.Response_001;
import com.zero.easyrpc.transport.api.Channel;
import com.zero.easyrpc.transport.api.ChannelHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * NettyClient.
 */
public class NettyClient extends AbstractSharedClient {

    private static final EventLoopGroup workerGroup = NettyServer.initEventLoopGroup(Math.min(Runtime.getRuntime().availableProcessors() + 1, 32),
            new DefaultThreadFactory("NettyClientWorker", true));

    private Bootstrap bootstrap;
    private ClientConfig clientConfig;

    private volatile io.netty.channel.Channel channel; // volatile, please copy reference to use

    public NettyClient(ClientConfig clientConfig, Codec codec) {
        super(codec);
        this.clientConfig = clientConfig;
    }

    @Override
    protected void doOpen() throws Throwable {
        if (bootstrap == null) {
            final NettyClientChannelHandler nettyClientHandler = new NettyClientChannelHandler(NettyClient.this, getCodec(), new ChannelHandler() {
                @Override
                public void connected(Channel channel) throws RemotingException {
                }

                @Override
                public void disconnected(Channel channel) throws RemotingException {
                }

                @Override
                public void sent(Channel channel, Object message) throws RemotingException {
                }

                @Override
                public void received(Channel channel, Object message) throws RemotingException {
                    if (message != null) {
                        if (message instanceof Response_001) {
                            DefaultResponseFuture.received(channel, (Response_001) message);
                        } else {
                            System.out.println("message "+message);
                        }
                    }
                }

                @Override
                public void caught(Channel channel, Throwable exception) throws RemotingException {
                }
            });

            bootstrap = new Bootstrap();
            if (workerGroup instanceof EpollEventLoopGroup) {
                ((EpollEventLoopGroup) workerGroup).setIoRatio(90);
            } else if (workerGroup instanceof NioEventLoopGroup) {
                ((NioEventLoopGroup) workerGroup).setIoRatio(90);
            }
            bootstrap.group(workerGroup);

            if (NettyServer.isNativeEt()) {
                bootstrap.channel(EpollSocketChannel.class);
            } else {
                bootstrap.channel(NioSocketChannel.class);
            }

            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.ALLOW_HALF_CLOSURE, false);

            bootstrap.handler(new ChannelInitializer() {
                protected void initChannel(io.netty.channel.Channel ch) throws Exception {
                    ch.pipeline()
                            .addLast("decoder", new NettyDecoder(getCodec(), 100))
                            .addLast("encoder", new NettyEncoder(getCodec()))
                            .addLast("handler", nettyClientHandler);
                }
            });
        }
        doConnect();
    }

    protected synchronized void doConnect() throws Throwable {
        long start = System.currentTimeMillis();
        ChannelFuture future = bootstrap.connect(NetUtils.toAddress(clientConfig.getDefaultAddress()));
        try {
            boolean ret = future.awaitUninterruptibly(3000, TimeUnit.MILLISECONDS);

            if (ret && future.isSuccess()) {
                io.netty.channel.Channel newChannel = future.channel();
                try {
                    // Close old channel
                    io.netty.channel.Channel oldChannel = NettyClient.this.channel; // copy reference
                    if (oldChannel != null) {
                        try {
                            oldChannel.close();
                        } finally {
                            NettyChannel.removeChannelIfDisconnected(oldChannel);
                        }
                    }
                } finally {
                    if (NettyClient.this.isClosed()) {
                        try {
                            newChannel.close();
                        } finally {
                            NettyClient.this.channel = null;
                            NettyChannel.removeChannelIfDisconnected(newChannel);
                        }
                    } else {
                        NettyClient.this.channel = newChannel;
                    }
                }
            } else if (future.cause() != null) {
                throw new RemotingException(getChannel(), "failed to connect to server "
                        + ", error message is:" + future.cause().getMessage(), future.cause());
            } else {
                throw new RemotingException(getChannel(), "client( failed to connect to server "
                        + " client-side timeout "
                        + "ms (elapsed: " + (System.currentTimeMillis() - start) + "ms) from netty client "
                        + NetUtils.getLocalHost() + " using dubbo version ");
            }
        } finally {
//            if (!isConnected()) {
//                //future.cancel(true);
//            }
        }
    }

    protected void doDisConnect() throws Throwable {
        try {
            NettyChannel.removeChannelIfDisconnected(channel);
        } catch (Throwable t) {
        }
    }

    protected void doClose() throws Throwable {
        //can't shutdown nioEventLoopGroup
        //nioEventLoopGroup.shutdownGracefully();
    }

    protected Channel getChannel() {
        io.netty.channel.Channel c = channel;
        if (c == null || !c.isActive())
            return null;
        return NettyChannel.getOrAddChannel(c);
    }


    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    @Override
    protected SharedObjectFactory createSharedFactory() {
        return new NettyChannelFactory(this);
    }

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public void setClientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }


    public void setBootstrap(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }
}
