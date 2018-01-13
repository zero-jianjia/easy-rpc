package com.zero.easyrpc.remoting.netty4;

import com.zero.easyrpc.remoting.NetUtils;
import com.zero.easyrpc.remoting.api.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * NettyClient.
 */
public class NettyClient extends AbstractSharedClient {

    private static final NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(Math.min(Runtime.getRuntime().availableProcessors() + 1, 32),
            new DefaultThreadFactory("NettyClientWorker", true));

    private Bootstrap bootstrap;

    private volatile Channel channel; // volatile, please copy reference to use

    public NettyClient()  {
    }

    protected void doOpen() throws Throwable {
        final NettyClientHandler nettyClientHandler = new NettyClientHandler();

        bootstrap = new Bootstrap();
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_REUSEADDR, true);

        bootstrap.group(nioEventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new ChannelInitializer() {

                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline()//.addLast("logging",new LoggingHandler(LogLevel.INFO))//for debug
                                .addLast("decoder", new NettyDecoder())
                                .addLast("encoder", new NettyEncoder())
                                .addLast("handler", nettyClientHandler);
                    }
                });
    }

    protected void doConnect() throws Throwable {
        long start = System.currentTimeMillis();
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(9100));
        try {
            boolean ret = future.awaitUninterruptibly(3000, TimeUnit.MILLISECONDS);

            if (ret && future.isSuccess()) {
                Channel newChannel = future.channel();
                try {
                    // Close old channel
                    Channel oldChannel = NettyClient.this.channel; // copy reference
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

    protected com.zero.easyrpc.remoting.api.Channel getChannel() {
        Channel c = channel;
        if (c == null || !c.isActive())
            return null;
        return NettyChannel.getOrAddChannel(c);
    }

    public boolean isClosed() {
        return false;
    }

    @Override
    public boolean open() {
        return false;
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

    public NettyClient(Codec codec) {
//        super(codec);
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return super.getLocalAddress();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return super.getRemoteAddress();
    }

    @Override
    public void setLocalAddress(InetSocketAddress localAddress) {
        super.setLocalAddress(localAddress);
    }

    @Override
    public void setRemoteAddress(InetSocketAddress remoteAddress) {
        super.setRemoteAddress(remoteAddress);
    }

    public void setBootstrap(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    @Override
    protected SharedObjectFactory createSharedFactory() {
        return new NettyChannelFactory(this);
    }
}
