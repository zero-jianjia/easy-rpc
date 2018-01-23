package com.zero.transport.netty4.server;


import com.zero.transport.api.Server;
import com.zero.transport.netty4.NativeSupport;
import com.zero.transport.netty4.NettyUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.PlatformDependent;
import org.zero.common.concurrent.NamedThreadFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;

public abstract class AbstractNettyServer implements Server {

    protected final SocketAddress localAddress;

    protected final HashedWheelTimer timer = new HashedWheelTimer(new NamedThreadFactory("acceptor.timer"));

    private final int nBosses;
    private final int nWorkers;

    private ServerBootstrap bootstrap;
    private EventLoopGroup boss;
    private EventLoopGroup worker;

    protected volatile ByteBufAllocator allocator;

    public AbstractNettyServer(SocketAddress localAddress) {
        this(localAddress, Runtime.getRuntime().availableProcessors() << 1);
    }

    public AbstractNettyServer(SocketAddress localAddress, int nWorkers) {
        this(localAddress, 1, nWorkers);
    }

    public AbstractNettyServer(SocketAddress localAddress, int nBosses, int nWorkers) {
        this.localAddress = localAddress;
        this.nBosses = nBosses;
        this.nWorkers = nWorkers;

        init();
    }

    protected void init() {
        ThreadFactory bossFactory = bossThreadFactory("netty.boss");
        ThreadFactory workerFactory = workerThreadFactory("netty.worker");
        boss = initEventLoopGroup(nBosses, bossFactory);
        worker = initEventLoopGroup(nWorkers, workerFactory);
        setIoRatio(100, 100);

        bootstrap = new ServerBootstrap().group(boss, worker);

        NettyUtil.initServerChannelFactory(bootstrap);

        allocator = new PooledByteBufAllocator(PlatformDependent.directBufferPreferred());
        bootstrap.childOption(ChannelOption.ALLOCATOR, allocator);
    }

    protected EventLoopGroup initEventLoopGroup(int nThreads, ThreadFactory tFactory) {
        if (NativeSupport.isNativeEPollAvailable()) {
            return new EpollEventLoopGroup(nThreads, tFactory);
        }
        if (NativeSupport.isNativeKQueueAvailable()) {
            return new KQueueEventLoopGroup(nThreads, tFactory);
        }
        return new NioEventLoopGroup(nThreads, tFactory);
    }

    private void setIoRatio(int bossIoRatio, int workerIoRatio) {
        EventLoopGroup boss = boss();
        if (boss instanceof EpollEventLoopGroup) {
            ((EpollEventLoopGroup) boss).setIoRatio(bossIoRatio);
        } else if (boss instanceof KQueueEventLoopGroup) {
            ((KQueueEventLoopGroup) boss).setIoRatio(bossIoRatio);
        } else if (boss instanceof NioEventLoopGroup) {
            ((NioEventLoopGroup) boss).setIoRatio(bossIoRatio);
        }

        EventLoopGroup worker = worker();
        if (worker instanceof EpollEventLoopGroup) {
            ((EpollEventLoopGroup) worker).setIoRatio(workerIoRatio);
        } else if (worker instanceof KQueueEventLoopGroup) {
            ((KQueueEventLoopGroup) worker).setIoRatio(workerIoRatio);
        } else if (worker instanceof NioEventLoopGroup) {
            ((NioEventLoopGroup) worker).setIoRatio(workerIoRatio);
        }
    }

    @Override
    public SocketAddress localAddress() {
        return localAddress;
    }

    @Override
    public int boundPort() {
        if (!(localAddress instanceof InetSocketAddress)) {
            throw new UnsupportedOperationException("Unsupported address type to get port");
        }
        return ((InetSocketAddress) localAddress).getPort();
    }

    @Override
    public void shutdownGracefully() {
        boss.shutdownGracefully();
        worker.shutdownGracefully();
    }

    private ThreadFactory bossThreadFactory(String name) {
        return new DefaultThreadFactory(name, Thread.MAX_PRIORITY);
    }

    private ThreadFactory workerThreadFactory(String name) {
        return new DefaultThreadFactory(name, Thread.MAX_PRIORITY);
    }

    protected ServerBootstrap bootstrap() {
        return bootstrap;
    }

    protected EventLoopGroup boss() {
        return boss;
    }

    protected EventLoopGroup worker() {
        return worker;
    }


    protected abstract ChannelFuture bind(SocketAddress localAddress);


    @Override
    public void start() throws InterruptedException {
        start(true);
    }

    @Override
    public void start(boolean sync) throws InterruptedException {
        // wait until the server socket is bind succeed.
        ChannelFuture future = bind(localAddress).sync();

        if (sync) {
            // wait until the server socket is closed.
            future.channel().closeFuture().sync();
        }
    }

}
