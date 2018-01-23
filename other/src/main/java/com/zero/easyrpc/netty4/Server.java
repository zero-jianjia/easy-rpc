package com.zero.easyrpc.netty4;

import com.zero.easyrpc.common.exception.RemotingSendRequestException;
import com.zero.easyrpc.common.exception.RemotingTimeoutException;
import com.zero.easyrpc.common.utils.Constants;
import com.zero.easyrpc.common.utils.NamedThreadFactory;
import com.zero.easyrpc.common.utils.Pair;
import com.zero.easyrpc.netty4.codec.TransporterDecoder;
import com.zero.easyrpc.netty4.codec.TransporterEncoder;
import com.zero.easyrpc.netty4.model.ChannelInactiveProcessor;
import com.zero.easyrpc.netty4.model.Processor;
import com.zero.easyrpc.netty4.headler.AcceptorIdleStateTrigger;
import com.zero.easyrpc.netty4.headler.IdleStateChecker;
import com.zero.easyrpc.netty4.util.ConnectionUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by jianjia1 on 17/12/04.
 */
public class Server extends BaseServer implements NettyServer {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private ServerBootstrap serverBootstrap;
    private EventLoopGroup boss;
    private EventLoopGroup worker;

    protected final HashedWheelTimer timer = new HashedWheelTimer(new NamedThreadFactory("netty.acceptor.timer"));

    protected volatile ByteBufAllocator allocator;

    private ServerConfig serverConfig;

    // handler的执行线程
    private DefaultEventExecutorGroup defaultEventExecutorGroup;

    // processor的默认执行线程
    private final ExecutorService defaultExecutor;

    private final AcceptorIdleStateTrigger acceptorIdleStateTrigger = new AcceptorIdleStateTrigger();

    private InvokeHook invokeHook;

    public Server() {
        this.defaultExecutor = Executors.newFixedThreadPool(4, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "NettyServerPublicExecutor_" + this.threadIndex.incrementAndGet());
            }
        });
    }

    public Server(ServerConfig serverConfig) {
        this();
        this.serverConfig = serverConfig;
    }

    @Override
    public void init() {
        if (serverConfig == null) {
            throw new IllegalArgumentException("Not config.");
        }

        ThreadFactory bossFactory = new DefaultThreadFactory("netty.boss");
        boss = initEventLoopGroup(1, bossFactory);

        int workerNum = serverConfig.getWorkerThreads();
        if (workerNum <= 0) {
            workerNum = Runtime.getRuntime().availableProcessors() << 1;
        }
        worker = initEventLoopGroup(workerNum, new DefaultThreadFactory("netty.worker"));


        serverBootstrap = new ServerBootstrap().group(boss, worker);

        allocator = new PooledByteBufAllocator(PlatformDependent.directBufferPreferred());

        serverBootstrap.childOption(ChannelOption.ALLOCATOR, allocator)
                .childOption(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);

        if (boss instanceof EpollEventLoopGroup) {
            ((EpollEventLoopGroup) boss).setIoRatio(100);
        } else if (boss instanceof NioEventLoopGroup) {
            ((NioEventLoopGroup) boss).setIoRatio(100);
        }
        if (worker instanceof EpollEventLoopGroup) {
            ((EpollEventLoopGroup) worker).setIoRatio(100);
        } else if (worker instanceof NioEventLoopGroup) {
            ((NioEventLoopGroup) worker).setIoRatio(100);
        }

        serverBootstrap.option(ChannelOption.SO_BACKLOG, 32768)
                .option(ChannelOption.SO_REUSEADDR, true);

        // child options
        serverBootstrap.childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.ALLOW_HALF_CLOSURE, false);

        int writeBufferLowWaterMark = serverConfig.getWriteBufferLowWaterMark();
        int writeBufferHighWaterMark = serverConfig.getWriteBufferHighWaterMark();
        if (writeBufferLowWaterMark >= 0 && writeBufferHighWaterMark > 0) {
            WriteBufferWaterMark waterMark = new WriteBufferWaterMark(writeBufferLowWaterMark, writeBufferHighWaterMark);
            serverBootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, waterMark);
        }
    }

    @Override
    public void start() {

        if (serverBootstrap == null) {
            init();
        }

        defaultEventExecutorGroup = new DefaultEventExecutorGroup(Constants.AVAILABLE_PROCESSORS,
                new ThreadFactory() {
                    private AtomicInteger count = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "NettyServerWorkerThread-" + count.incrementAndGet());
                    }
                });
        if (isNativeEt()) {
            serverBootstrap.channel(EpollServerSocketChannel.class);
        } else {
            serverBootstrap.channel(NioServerSocketChannel.class);
        }

        serverBootstrap.localAddress(new InetSocketAddress(serverConfig.getListenPort()))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                defaultEventExecutorGroup,
                                new IdleStateChecker(timer, Constants.READER_IDLE_TIME_SECONDS, 0, 0),
                                acceptorIdleStateTrigger,
                                new TransporterDecoder(),
                                new TransporterEncoder(),
                                new NettyServerHandler());
                    }
                });

        try {
            serverBootstrap.bind().sync();
            logger.info("Netty start listen on {}...", this.serverConfig.getListenPort());
        } catch (InterruptedException e1) {
            logger.error("start serverBootstrap exception [{}]", e1.getMessage());
            throw new RuntimeException("this.serverBootstrap.bind().sync() InterruptedException", e1);
        }
    }

    @Override
    public void shutdown() {
        try {
            if (this.timer != null) {
                this.timer.stop();
            }

            this.boss.shutdownGracefully();

            this.worker.shutdownGracefully();

            if (this.defaultEventExecutorGroup != null) {
                this.defaultEventExecutorGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            logger.error("Server shutdown exception, ", e);
        }

        if (this.defaultExecutor != null) {
            try {
                this.defaultExecutor.shutdown();
            } catch (Exception e) {
                logger.error("Server shutdown exception, ", e);
            }
        }
    }

    @Override
    public void registerInvokeHook(InvokeHook invokeHook) {
        this.invokeHook = invokeHook;
    }

    @Override
    public void registerProecessor(byte sign, Processor processor, ExecutorService executor) {
        ExecutorService e = executor;
        if (e == null) {
            e = this.defaultExecutor;
        }
        Pair<Processor, ExecutorService> pair = new Pair<>(processor, e);
        this.processorMap.put(sign, pair);
    }

    @Override
    public void registerDefaultProcessor(Processor processor, ExecutorService executor) {
        this.defaultRequestProcessor = new Pair<>(processor, executor);
    }

    @Override
    public void registerChannelInactiveProcessor(ChannelInactiveProcessor processor, ExecutorService executor) {
        if (executor == null) {
            executor = super.defaultExecutor;
        }
        this.defaultChannelInactiveProcessor = new Pair<>(processor, executor);
    }

    @Override
    public Pair<Processor, ExecutorService> getProcessorPair(int sign) {
        return processorMap.get((byte) sign);
    }

    @Override
    public Transporter invokeSync(Channel channel, Transporter request, long timeoutMillis)
            throws InterruptedException, RemotingSendRequestException, RemotingTimeoutException {
        return super.invokeSyncImpl(channel, request, timeoutMillis);
    }

    class NettyServerHandler extends SimpleChannelInboundHandler<Transporter> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Transporter msg) throws Exception {
            processMessageReceived(ctx, msg);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            logger.info("Accept connection {}." , ConnectionUtils.parseChannelRemoteAddr(ctx.channel()));
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            processChannelInactive(ctx);
            logger.info("Destory connection {}." , ConnectionUtils.parseChannelRemoteAddr(ctx.channel()));
        }
    }
}

