package com.zero.easyrpc.netty4;

import com.zero.easyrpc.common.exception.RemotingException;
import com.zero.easyrpc.common.exception.RemotingSendRequestException;
import com.zero.easyrpc.common.exception.RemotingTimeoutException;
import com.zero.easyrpc.common.utils.Constants;
import com.zero.easyrpc.common.utils.NamedThreadFactory;
import com.zero.easyrpc.common.utils.Pair;
import com.zero.easyrpc.netty4.codec.TransporterEncoder;
import com.zero.easyrpc.netty4.model.ChannelInactiveProcessor;
import com.zero.easyrpc.netty4.model.Processor;
import com.zero.easyrpc.netty4.headler.ConnectionIdleStateTrigger;
import com.zero.easyrpc.netty4.headler.IdleStateChecker;
import com.zero.easyrpc.netty4.codec.TransporterDecoder;
import com.zero.easyrpc.netty4.util.ConnectionUtils;
import com.zero.easyrpc.netty4.headler.ConnectionWatchdog;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by jianjia1 on 17/12/04.
 */
public class Client extends BaseServer implements NettyClient {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private ClientConfig clientConfig;

    private Bootstrap bootstrap;
    private EventLoopGroup worker;

    private final Map<String, ChannelWrapper> channelMap = new ConcurrentHashMap<>();

    private volatile ByteBufAllocator allocator;

    private final Lock channelMapLock = new ReentrantLock();

    private static final long LockTimeoutMillis = 3000;

    private DefaultEventExecutorGroup defaultEventExecutorGroup;

    private final ConnectionIdleStateTrigger connectionIdleStateTrigger = new ConnectionIdleStateTrigger();

    private HashedWheelTimer timer = new HashedWheelTimer(new NamedThreadFactory("netty.timer"));

    private boolean isReconnect = true;

    public Client() {

    }

    public Client(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    @Override
    public void init() {
        ClientConfig config = clientConfig;
        if (config == null) {
            throw new IllegalArgumentException("No config.");
        }

        ThreadFactory workerFactory = new DefaultThreadFactory("netty.client");
        worker = initEventLoopGroup(config.getWorkerThreads(), workerFactory);

        bootstrap = new Bootstrap().group(worker);

        if (worker instanceof EpollEventLoopGroup) {
            ((EpollEventLoopGroup) worker).setIoRatio(100);
        } else if (worker instanceof NioEventLoopGroup) {
            ((NioEventLoopGroup) worker).setIoRatio(100);
        }

        bootstrap.option(ChannelOption.ALLOCATOR, allocator)
                .option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.SECONDS.toMillis(3));

        bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOW_HALF_CLOSURE, false);

        int writeBufferLowWaterMark = config.getWriteBufferLowWaterMark();
        int writeBufferHighWaterMark = config.getWriteBufferHighWaterMark();

        if (writeBufferLowWaterMark >= 0 && writeBufferHighWaterMark > 0) {
            WriteBufferWaterMark waterMark = new WriteBufferWaterMark(writeBufferLowWaterMark, writeBufferHighWaterMark);
            bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, waterMark);
        }
    }

    @Override
    public void start() {
        if (bootstrap == null) {
            init();
        }

        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
                getClientConfig().getWorkerThreads(),
                new ThreadFactory() {
                    private AtomicInteger count = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "NettyClientWorkerThread-" + count.incrementAndGet());
                    }
                });

        if (isNativeEt()) {
            bootstrap.channel(EpollSocketChannel.class);
        } else {
            bootstrap.channel(NioSocketChannel.class);
        }

        // 重连
        final ConnectionWatchdog watchdog = new ConnectionWatchdog(bootstrap, timer) {
            @Override
            public ChannelHandler[] handlers() {
                return new ChannelHandler[]{
                        this,
                        new TransporterDecoder(),
                        new TransporterEncoder(),
                        new IdleStateChecker(timer, 0, Constants.WRITER_IDLE_TIME_SECONDS, 0),
                        connectionIdleStateTrigger,
                        new NettyClientHandler()};
            }
        };
        watchdog.setReconnect(isReconnect);

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(defaultEventExecutorGroup, watchdog.handlers());
            }
        });
    }

    public Channel getAndCreateChannel(final String address) throws InterruptedException {
        if (address == null) {
            logger.warn("address is null");
            return null;
        }

        ChannelWrapper cw = channelMap.get(address);
        if (cw != null && cw.isOK()) {
            return cw.getChannel();
        }

        return createChannel(address);
    }

    public Channel createChannel(String address) throws InterruptedException {
        ChannelWrapper cw = channelMap.get(address);
        if (cw != null && cw.isOK()) {
            return cw.getChannel();
        }

        if (channelMapLock.tryLock(LockTimeoutMillis, TimeUnit.MILLISECONDS)) {
            try {
                boolean createNewConnection = false;
                cw = channelMap.get(address);
                if (cw != null) {
                    if (cw.isOK()) {
                        return cw.getChannel();
                    } else if (!cw.getChannelFuture().isDone()) {
                        createNewConnection = false;
                    } else {
                        // 如果缓存中channel的状态不正确的情况下，则将此不健康的channel从缓存中移除，重新创建
                        channelMap.remove(address);
                        createNewConnection = true;
                    }
                } else {
                    createNewConnection = true;
                }

                if (createNewConnection) {
                    ChannelFuture channelFuture = bootstrap.connect(ConnectionUtils.string2SocketAddress(address));
                    logger.info("createChannel: begin to connect remote host {}.", address);
                    cw = new ChannelWrapper(channelFuture);
                    // 放入缓存
                    channelMap.put(address, cw);
                }
            } catch (Exception e) {
                logger.error("createChannel: create channel exception", e);
            } finally {
                channelMapLock.unlock();
            }
        } else {
            logger.warn("createChannel: try to lock channel table, but timeout, {}ms", LockTimeoutMillis);
        }

        if (cw != null) {
            ChannelFuture channelFuture = cw.getChannelFuture();
            if (channelFuture.awaitUninterruptibly(clientConfig.getConnectTimeoutMillis())) {
                if (cw.isOK()) {
                    logger.info("createChannel: connect remote host[{}] success, {}", address, channelFuture.toString());
                    return cw.getChannel();
                } else {
                    logger.warn("createChannel: connect remote host[" + address + "] failed, " + channelFuture.toString(), channelFuture.cause());
                }
            } else {
                logger.warn("createChannel: connect remote host[{}] timeout {}ms, {}", address, clientConfig.getConnectTimeoutMillis(),
                        channelFuture.toString());
            }
        }

        return null;
    }

    @Override
    public void registerProcessor(byte sign, Processor processor, ExecutorService executor) {
        ExecutorService e = executor;
        if (e == null) {
            e = super.defaultExecutor;
        }

        Pair<Processor, ExecutorService> pair = new Pair<>(processor, e);
        processorMap.put(sign, pair);
    }

    @Override
    public boolean isChannelWriteable(String address) {
        ChannelWrapper cw = this.channelMap.get(address);
        if (cw != null && cw.isOK()) {
            return cw.isWriteable();
        }
        return true;
    }

    class NettyClientHandler extends SimpleChannelInboundHandler<Transporter> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Transporter msg) throws Exception {
            processMessageReceived(ctx, msg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            processChannelInactive(ctx);
        }
    }

    @Override
    public void shutdown() {
        try {
            this.timer.stop();
            this.timer = null;
            for (ChannelWrapper cw : this.channelMap.values()) {
                this.closeChannel(null, cw.getChannel());
            }

            this.channelMap.clear();

            this.worker.shutdownGracefully();

            if (this.defaultEventExecutorGroup != null) {
                this.defaultEventExecutorGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            logger.error("Client shutdown exception, ", e);
        }

        if (this.defaultExecutor != null) {
            try {
                this.defaultExecutor.shutdown();
            } catch (Exception e) {
                logger.error("Client shutdown exception, ", e);
            }
        }
    }

    @Override
    public void registerInvokeHook(InvokeHook invokeHook) {
        this.invokeHook = invokeHook;
    }

    @Override
    public Transporter invokeSync(String address, Transporter request, long timeoutMillis) throws InterruptedException, RemotingException {

        final Channel channel = getAndCreateChannel(address);
        if (channel != null && channel.isActive()) {
            try {
                // 回调前置钩子
                if (this.invokeHook != null) {
                    this.invokeHook.doBeforeRequest(address, request);
                }
                // 有了channel，有了request，request中也有了请求的Request.Code和Topic值，那么就是万事具备了，channel.writeAndFlush(request)就OK了
                Transporter response = this.invokeSyncImpl(channel, request, timeoutMillis);
                // 后置回调钩子
                if (this.invokeHook != null) {
                    this.invokeHook.doAfterResponse(ConnectionUtils.parseChannelRemoteAddr(channel), request, response);
                }
                return response;
            } catch (RemotingSendRequestException e) {
                logger.warn("invokeSync: send request exception, so close the channel[{}]", address);
                this.closeChannel(address, channel);
                throw e;
            } catch (RemotingTimeoutException e) {
                logger.warn("invokeSync: wait response timeout exception, the channel[{}]", address);
                throw e;
            }
        } else {
            // 如果该channel是不健康的(创建的时候也许是好的，放入到缓存table的时候也是好的，就是在用的时候，它不行了，尼玛，不好意思，就需要将你从table中移除掉)
            this.closeChannel(address, channel);
            throw new RemotingException(address + " connection exception");
        }
    }

    private void closeChannel(String addr, Channel channel) {
        if (null == channel)
            return;

        final String addrRemote = null == addr ? ConnectionUtils.parseChannelRemoteAddr(channel) : addr;

        try {
            if (this.channelMapLock.tryLock(LockTimeoutMillis, TimeUnit.MILLISECONDS)) {
                try {
                    boolean removeItemFromTable = true;
                    final ChannelWrapper prevCW = this.channelMap.get(addrRemote);

                    logger.info("closeChannel: begin close the channel[{}] Found: {}", addrRemote, (prevCW != null));

                    if (null == prevCW) {
                        logger.info("closeChannel: the channel[{}] has been removed from the channel table before", addrRemote);
                        removeItemFromTable = false;
                    } else if (prevCW.getChannel() != channel) {
                        logger.info("closeChannel: the channel[{}] has been closed before, and has been created again, nothing to do.", addrRemote);
                        removeItemFromTable = false;
                    }

                    if (removeItemFromTable) {
                        this.channelMap.remove(addrRemote);
                        logger.info("closeChannel: the channel[{}] was removed from channel table", addrRemote);
                    }

                    ConnectionUtils.closeChannel(channel);
                } catch (Exception e) {
                    logger.error("closeChannel: close the channel exception", e);
                } finally {
                    this.channelMapLock.unlock();
                }
            } else {
                logger.warn("closeChannel: try to lock channel table, but timeout, {}ms", LockTimeoutMillis);
            }
        } catch (InterruptedException e) {
            logger.error("closeChannel exception", e);
        }
    }


    @Override
    public void setReconnect(boolean isReconnect) {
        this.isReconnect = isReconnect;
    }

    @Override
    public void registerChannelInactiveProcessor(ChannelInactiveProcessor processor, ExecutorService executor) {
        if (executor == null) {
            executor = defaultExecutor;
        }
        defaultChannelInactiveProcessor = new Pair<>(processor, executor);
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    // channel的包装类
    class ChannelWrapper {

        private final ChannelFuture channelFuture;

        public ChannelWrapper(ChannelFuture channelFuture) {
            this.channelFuture = channelFuture;
        }

        public boolean isOK() {
            return (this.channelFuture.channel() != null && this.channelFuture.channel().isActive());
        }

        public boolean isWriteable() {
            return this.channelFuture.channel().isWritable();
        }

        private Channel getChannel() {
            return this.channelFuture.channel();
        }

        public ChannelFuture getChannelFuture() {
            return channelFuture;
        }
    }

}
