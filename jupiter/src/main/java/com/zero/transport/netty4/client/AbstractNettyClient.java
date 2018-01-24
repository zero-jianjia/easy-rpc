package com.zero.transport.netty4.client;

import com.zero.transport.Directory;
import com.zero.transport.api.Client;
import com.zero.transport.api.channel.ChannelGroup;
import com.zero.transport.netty4.NettyUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.zero.common.concurrent.NamedThreadFactory;
import org.zero.common.util.ClassUtil;
import org.zero.common.util.Maps;
import com.zero.transport.api.Connection;
import com.zero.transport.api.ConnectionManager;
import com.zero.transport.UnresolvedAddress;
import com.zero.transport.api.channel.CopyOnWriteGroupList;
import com.zero.transport.api.channel.DirectoryChannelGroup;
import com.zero.transport.netty4.NettyChannelGroup;
import com.zero.transport.api.processor.ConsumerProcessor;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;

import static org.zero.common.util.Preconditions.checkNotNull;

public abstract class AbstractNettyClient implements Client<Connection> {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractNettyClient.class);

    static {
        // because getProcessId() sometimes too slow
        ClassUtil.classInitialize("io.netty.channel.DefaultChannelId", 500);
    }

    protected final HashedWheelTimer timer = new HashedWheelTimer(new NamedThreadFactory("connector.timer"));

    private final ConcurrentMap<UnresolvedAddress, ChannelGroup> addressGroups = Maps.newConcurrentMap();
    private final DirectoryChannelGroup directoryGroup = new DirectoryChannelGroup();
    private final ConnectionManager connectionManager = new ConnectionManager();

    private Bootstrap bootstrap;
    private EventLoopGroup worker;
    private int nWorkers;

    protected volatile ByteBufAllocator allocator;

    public AbstractNettyClient() {
        this(Runtime.getRuntime().availableProcessors() + 1);
    }

    public AbstractNettyClient(int nWorkers) {
        this.nWorkers = nWorkers;
        init();
    }

    protected void init() {
        ThreadFactory workerFactory = workerThreadFactory("netty.client.worker");
        worker = NettyUtil.initEventLoopGroup(nWorkers, workerFactory);

        bootstrap = new Bootstrap().group(worker);


        setIoRatio(100);

        allocator = new PooledByteBufAllocator(PlatformDependent.directBufferPreferred());
        bootstrap.option(ChannelOption.ALLOCATOR, allocator);

        NettyUtil.initClientChannelFactory(bootstrap);
    }


    protected ThreadFactory workerThreadFactory(String name) {
        return new DefaultThreadFactory(name, Thread.MAX_PRIORITY);
    }

    @Override
    public void withProcessor(ConsumerProcessor processor) {
        // the default implementation does nothing
    }

    @Override
    public ChannelGroup group(UnresolvedAddress address) {
        checkNotNull(address, "address");

        ChannelGroup group = addressGroups.get(address);
        if (group == null) {
            ChannelGroup newGroup = channelGroup(address);
            group = addressGroups.putIfAbsent(address, newGroup);
            if (group == null) {
                group = newGroup;
            }
        }
        return group;
    }

    @Override
    public Collection<ChannelGroup> groups() {
        return addressGroups.values();
    }

    @Override
    public boolean addChannelGroup(Directory directory, ChannelGroup group) {
        CopyOnWriteGroupList groups = directory(directory);
        boolean added = groups.addIfAbsent(group);
        if (added) {
            logger.info("Added channel group: {} to {}.", group, directory.directory());
        }
        return added;
    }

    @Override
    public boolean removeChannelGroup(Directory directory, ChannelGroup group) {
        CopyOnWriteGroupList groups = directory(directory);
        boolean removed = groups.remove(group);
        if (removed) {
            logger.warn("Removed channel group: {} in directory: {}.", group, directory.directory());
        }
        return removed;
    }

    @Override
    public CopyOnWriteGroupList directory(Directory directory) {
        return directoryGroup.find(directory);
    }

    @Override
    public boolean isDirectoryAvailable(Directory directory) {
        CopyOnWriteGroupList groups = directory(directory);
        ChannelGroup[] snapshot = groups.snapshot();
        for (ChannelGroup g : snapshot) {
            if (g.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DirectoryChannelGroup directoryGroup() {
        return directoryGroup;
    }

    @Override
    public ConnectionManager connectionManager() {
        return connectionManager;
    }

    @Override
    public void shutdownGracefully() {
        connectionManager.cancelAllReconnect();
        worker.shutdownGracefully();
    }

    /**
     * A {@link Bootstrap} that makes it easy to bootstrap a {@link io.netty.channel.Channel} to use
     * for clients.
     */
    protected Bootstrap bootstrap() {
        return bootstrap;
    }

    /**
     * Lock object of bootstrap.
     */
    protected Object bootstrapLock() {
        return bootstrap;
    }

    /**
     * The {@link EventLoopGroup} for the child. These {@link EventLoopGroup}'s are used to handle
     * all the events and IO for {@link io.netty.channel.Channel}'s.
     */
    protected EventLoopGroup worker() {
        return worker;
    }

    /**
     * Creates the same address of the channel group.
     */
    protected ChannelGroup channelGroup(UnresolvedAddress address) {
        return new NettyChannelGroup(address);
    }


    public void setIoRatio(int workerIoRatio) {
        EventLoopGroup worker = worker();
        if (worker instanceof EpollEventLoopGroup) {
            ((EpollEventLoopGroup) worker).setIoRatio(workerIoRatio);
        } else if (worker instanceof KQueueEventLoopGroup) {
            ((KQueueEventLoopGroup) worker).setIoRatio(workerIoRatio);
        } else if (worker instanceof NioEventLoopGroup) {
            ((NioEventLoopGroup) worker).setIoRatio(workerIoRatio);
        }
    }
}
