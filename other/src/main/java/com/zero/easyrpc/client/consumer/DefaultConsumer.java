package com.zero.easyrpc.client.consumer;

import com.zero.easyrpc.common.protocal.Protocol;
import com.zero.easyrpc.common.rpc.RegisterMeta;
import com.zero.easyrpc.common.utils.ChannelGroup;
import com.zero.easyrpc.common.utils.JUnsafe;
import com.zero.easyrpc.common.utils.NettyChannelGroup;
import com.zero.easyrpc.common.utils.UnresolvedAddress;
import com.zero.easyrpc.netty4.Client;
import com.zero.easyrpc.netty4.util.ConnectionUtils;
import com.zero.easyrpc.netty4.ClientConfig;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by jianjia1 on 17/12/07.
 */
public abstract class DefaultConsumer extends AbstractConsumer {
    private static final Logger logger = LoggerFactory.getLogger(DefaultConsumer.class);

    private ClientConfig registryClientConfig;
    private ClientConfig providerClientConfig;
    private ConsumerConfig consumerConfig;

    protected Client registryNettyClient;
    protected Client providerNettyClient;

    private DefaultConsumerRegistry defaultConsumerRegistry;
    private ConsumerManager consumerManager;
    private Channel registyChannel;

    public DefaultConsumer(ClientConfig registryClientConfig, ClientConfig providerClientConfig, ConsumerConfig consumerConfig) {
        this.registryClientConfig = registryClientConfig;
        this.providerClientConfig = providerClientConfig;
        this.consumerConfig = consumerConfig;

        defaultConsumerRegistry = new DefaultConsumerRegistry(this);
        consumerManager = new ConsumerManager(this);
        initialize();
    }

    private void initialize() {

        //因为服务消费端可以直连provider，所以当传递过来的与注册中心连接的配置文件为空的时候，可以不初始化registryNettyClient
        if (this.registryClientConfig != null) {
            this.registryNettyClient = new Client(this.registryClientConfig);
            // 注册处理器
            registerProcessor();
        }

        this.providerNettyClient = new Client(this.providerClientConfig);
    }

    private void registerProcessor() {
        this.registryNettyClient.registerProcessor(Protocol.SUBCRIBE_RESULT, new DefaultRegistryProcessor(this), null);
        this.registryNettyClient.registerProcessor(Protocol.SUBCRIBE_SERVICE_CANCEL, new DefaultRegistryProcessor(this), null);
        this.registryNettyClient.registerProcessor(Protocol.CHANGE_LOADBALANCE, new DefaultRegistryProcessor(this), null);
    }

    @Override
    public SubscribeManager subscribeService(final String service) {

        SubscribeManager manager = new SubscribeManager() {

            private final ReentrantLock lock = new ReentrantLock();
            private final Condition notifyCondition = lock.newCondition();
            private final AtomicBoolean signalNeeded = new AtomicBoolean(false);

            @Override
            public void start() {
                subcribeService(service, new NotifyListener() {
                    @Override
                    public void notify(RegisterMeta registerMeta, NotifyEvent event) {
                        String remoteHost = registerMeta.getAddress().getHost();

                        // port （ vip服务 port端口号-2 ）
                        int remotePort = registerMeta.isVIPService() ? (registerMeta.getAddress().getPort() - 2) : registerMeta.getAddress().getPort();

                        final ChannelGroup group = group(new UnresolvedAddress(remoteHost, remotePort));
                        if (event == NotifyEvent.CHILD_ADDED) {
                            // 链路复用，如果此host和port对应的链接的channelGroup是已经存在的，则无需建立新的链接，只需要将此group与service建立关系即可
                            if (!group.isAvailable()) {

                                int connCount = registerMeta.getConnCount() < 0 ? 1 : registerMeta.getConnCount();

                                group.setWeight(registerMeta.getWeight());

                                for (int i = 0; i < connCount; i++) {
                                    try {
                                        // 所有的consumer与provider之间的链接不进行断线重连操作
                                        DefaultConsumer.this.getProviderNettyClient().setReconnect(false);
                                        DefaultConsumer.this.getProviderNettyClient().getBootstrap()
                                                .connect(ConnectionUtils.string2SocketAddress(remoteHost + ":" + remotePort))
                                                .addListener(new ChannelFutureListener() {
                                                    @Override
                                                    public void operationComplete(ChannelFuture future) throws Exception {
                                                        group.add(future.channel());
                                                        onSucceed(signalNeeded.getAndSet(false));
                                                    }

                                                });
                                    } catch (Exception e) {
                                        logger.error("connection provider host [{}] and port [{}] occor exception [{}]", remoteHost, remotePort, e.getMessage());
                                    }
                                }
                            } else {
                                onSucceed(signalNeeded.getAndSet(false));
                            }
                            addChannelGroup(service, group);
                        } else if (event == NotifyEvent.CHILD_REMOVED) {
                            removedIfAbsent(service, group);
                        }
                    }
                });
            }

            private void onSucceed(boolean doSignal) {
                if (doSignal) {
                    final ReentrantLock l = lock;
                    l.lock();
                    try {
                        notifyCondition.signalAll();
                    } finally {
                        l.unlock();
                    }
                }
            }

            @Override
            public boolean waitForAvailable(long timeoutMillis) {
                if (isServiceAvailable(service)) {
                    return true;
                }
                boolean available = false;
                long start = System.nanoTime();
                final ReentrantLock l = lock;
                l.lock();
                try {
                    while (!isServiceAvailable(service)) {
                        signalNeeded.set(true);
                        notifyCondition.await(timeoutMillis, TimeUnit.MILLISECONDS);

                        available = isServiceAvailable(service);
                        if (available || (System.nanoTime() - start) > TimeUnit.MILLISECONDS.toNanos(timeoutMillis)) {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    JUnsafe.throwException(e);
                } finally {
                    l.unlock();
                }
                return available;
            }

            private boolean isServiceAvailable(String service) {
                CopyOnWriteArrayList<ChannelGroup> list = DefaultConsumer.super.getChannelGroupByServiceName(service);
                if (list == null) {
                    return false;
                } else {
                    for (ChannelGroup channelGroup : list) {
                        if (channelGroup.isAvailable()) {
                            return true;
                        }
                    }
                }
                return false;
            }

        };
        manager.start();
        return manager;
    }

    @Override
    public void subcribeService(String subcribeServices, NotifyListener listener) {
        if (subcribeServices != null) {
            defaultConsumerRegistry.subcribeService(subcribeServices, listener);
        }
    }

    @Override
    public boolean addChannelGroup(String serviceName, ChannelGroup group) {
        return DefaultConsumer.super.addIfAbsent(serviceName, group);
    }

    @Override
    public boolean removeChannelGroup(String serviceName, ChannelGroup group) {
        return DefaultConsumer.super.removedIfAbsent(serviceName, group);
    }

    @Override
    public Channel directGetProviderByChannel(UnresolvedAddress address) throws InterruptedException {
        return this.providerNettyClient.getAndCreateChannel(address.getHost() + ":" + address.getPort());
    }


    @Override
    public void start() {

        logger.info("######### consumer start.... #########");
        // 如果连接注册中心的client初始化成功的情况下，且连接注册中心的地址不为空的时候去尝试连接注册中心
        if (null != this.registryClientConfig && null != this.registryNettyClient) {
            this.registryNettyClient.start();
            // 获取到与注册中心集群的一个健康的的Netty 长连接的channel
            getOrUpdateHealthyChannel();
        }

        this.providerNettyClient.setReconnect(false);
        this.providerNettyClient.start();

    }

    @Override
    public void getOrUpdateHealthyChannel() {

        //获取到注册中心的地址
        String addresses = registryClientConfig.getDefaultAddress();

        if (registyChannel != null && registyChannel.isActive() && registyChannel.isWritable()) {
            return;
        }

        if (addresses == null || "".equals(addresses)) {
            logger.error("registry address is empty");
            return;
        }

        //与注册中心连接的时候重试次数
        int retryConnectionTimes = consumerConfig.getRetryConnectionRegistryTimes();
        //连接给每次注册中心的时候最大的超时时间
        long maxTimeout = consumerConfig.getMaxRetryConnectionRegsitryTime();

        String[] adds = addresses.split(",");

        for (int i = 0; i < adds.length; i++) {
            if (registyChannel != null && registyChannel.isActive() && registyChannel.isWritable())
                return;

            String currentAddress = adds[i];
            //开始计时
            final long beginTimestamp = System.currentTimeMillis();
            long endTimestamp = beginTimestamp;

            //当重试次数小于最大次数且每个实例重试的时间小于最大的时间的时候，不断重试
            for (int times = 0; times < retryConnectionTimes && (endTimestamp - beginTimestamp) < maxTimeout; times++) {
                try {
                    Channel channel = registryNettyClient.createChannel(currentAddress);
                    if (channel != null && channel.isActive() && channel.isWritable()) {
                        registyChannel = channel;
                        break;
                    } else {
                        TimeUnit.MILLISECONDS.sleep(1 << times);
                    }
                } catch (InterruptedException e) {
                    logger.warn("connection registry center [{}] fail", currentAddress);
                    endTimestamp = System.currentTimeMillis();
                }
            }
        }
    }

    public Client getRegistryNettyClient() {
        return registryNettyClient;
    }

    public void setRegistryNettyClient(Client registryNettyClient) {
        this.registryNettyClient = registryNettyClient;
    }

    public Channel getRegistyChannel() {
        return registyChannel;
    }

    public void setRegistyChannel(Channel registyChannel) {
        this.registyChannel = registyChannel;
    }

    public ConsumerConfig getConsumerConfig() {
        return consumerConfig;
    }

    public void setConsumerConfig(ConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public ClientConfig getRegistryClientConfig() {
        return registryClientConfig;
    }

    public void setRegistryClientConfig(ClientConfig registryClientConfig) {
        this.registryClientConfig = registryClientConfig;
    }

    public ConsumerManager getConsumerManager() {
        return consumerManager;
    }

    public void setConsumerManager(ConsumerManager consumerManager) {
        this.consumerManager = consumerManager;
    }

    public Client getProviderNettyClient() {
        return providerNettyClient;
    }

    public void setProviderNettyClient(Client providerNettyClient) {
        this.providerNettyClient = providerNettyClient;
    }

    public DefaultConsumerRegistry getDefaultConsumerRegistry() {
        return defaultConsumerRegistry;
    }

    public void setDefaultConsumerRegistry(DefaultConsumerRegistry defaultConsumerRegistry) {
        this.defaultConsumerRegistry = defaultConsumerRegistry;
    }

    public ChannelGroup group(UnresolvedAddress address) {

        ChannelGroup group = addressGroups.get(address);
        if (group == null) {
            ChannelGroup newGroup = newChannelGroup(address);
            group = addressGroups.putIfAbsent(address, newGroup);
            if (group == null) {
                group = newGroup;
            }
        }
        return group;
    }

    private ChannelGroup newChannelGroup(UnresolvedAddress address) {
        return new NettyChannelGroup(address);
    }


}
