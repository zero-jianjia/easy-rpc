package com.zero.easyrpc.registry;

import com.alibaba.fastjson.JSON;
import com.zero.easyrpc.common.utils.NamedThreadFactory;
import com.zero.easyrpc.common.utils.PersistUtils;
import com.zero.easyrpc.netty4.Server;
import com.zero.easyrpc.registry.base.*;
import com.zero.easyrpc.registry.model.RegistryPersistRecord;
import com.zero.easyrpc.netty4.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * #######注册中心######
 * <p>
 * 可以有多个注册中心，所有的注册中心之前不进行通讯，都是无状态的
 * 1.rovider端与每一个注册中心之间保持长连接，保持重连
 * 2.consumer随机选择一个注册中心保持长连接，如果断了，不去主动重连，选择其他可用的注册中心
 * <p>
 * 默认的注册中心，处理注册端的所有事宜：
 * 1)处理 provider 端发送过来的注册信息
 * 2)处理 consumer 端发送过来的订阅信息
 * 3)当服务下线需要通知对应的consumer变更后的注册信息
 * 4)所有的注册订阅信息的储存和健康检查
 * 5)接收管理者的一些信息请求，比如 服务统计 | 某个实例的服务降级 | 通知消费者的访问策略  | 改变某个服务实例的比重
 * 6)将管理者对服务的一些信息 例如审核结果，负载算法等信息持久化到硬盘
 */
public class DefaultRegistry implements Registry {
    private static final Logger logger = LoggerFactory.getLogger(DefaultRegistry.class);

    private final ServerConfig nettyServerConfig;       //netty Server的一些配置
    private Server server;                              //注册中心的netty server端

    private RegistryConfig registryConfig;              //注册中心的配置文件
    private RegistryConsumerManager consumerManager;    //注册中心消费侧的管理逻辑控制类
    private RegistryProviderManager providermanager;    //注册中心服务提供者的管理逻辑控制类

    private ExecutorService executor;                   //执行器
    private ExecutorService channelInactiveExecutor;    //channel inactive的线程执行器

    //定时任务
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("registry-timer"));


    public DefaultRegistry(ServerConfig nettyServerConfig, RegistryConfig registryConfig) {
        this.nettyServerConfig = nettyServerConfig;
        this.registryConfig = registryConfig;
        consumerManager = new RegistryConsumerManager(this);
        providermanager = new RegistryProviderManager(this);
        initialize();
    }

    private void initialize() {
        this.server = new Server(this.nettyServerConfig);

        this.executor = Executors.newFixedThreadPool(nettyServerConfig.getWorkerThreads(), new NamedThreadFactory("RegistryExecutorThread-"));
        this.channelInactiveExecutor = Executors.newFixedThreadPool(nettyServerConfig.getChannelInactiveHandlerThreads(), new NamedThreadFactory("RegistryChannelInActiveExecutorThread-"));

        //注册处理器
        this.registerProcessor();

        //从硬盘上恢复一些服务的信息
        this.recoverServiceInfoFromDisk();

        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                // 延迟60秒，每隔60秒开始 定时向consumer发送消费者消费失败的信息
                try {
                   getConsumerManager().checkSendFailedMessage();
                } catch (Exception e) {
                    logger.warn("schedule publish failed [{}]", e.getMessage());
                }
            }
        }, 60, 60, TimeUnit.SECONDS);

        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // 延迟60秒，每隔一段时间将一些服务信息持久化到硬盘上
                try {
                   getProvidermanager().persistServiceInfo();
                } catch (Exception e) {
                    logger.warn("schedule persist failed [{}]", e.getMessage());
                }
            }
        }, 60, registryConfig.getPersistInterval(), TimeUnit.SECONDS);
    }

    /**
     * 从硬盘上恢复一些服务的审核负载算法的信息
     */
    private void recoverServiceInfoFromDisk() {

        String persistString = PersistUtils.file2String(this.registryConfig.getStorePathRootDir());

        if (persistString != null) {
            List<RegistryPersistRecord> registryPersistRecords = JSON.parseArray(persistString.trim(), RegistryPersistRecord.class);

            if (registryPersistRecords != null) {
                for (RegistryPersistRecord metricsReporter : registryPersistRecords) {
                    String serviceName = metricsReporter.getServiceName();
                    getProvidermanager().getHistoryRecords().put(serviceName, metricsReporter);
                }
            }
        }

    }

    private void registerProcessor() {
        this.server.registerDefaultProcessor(new DefaultRegistryProcessor(this), this.executor);
        this.server.registerChannelInactiveProcessor(new DefaultRegistryChannelInactiveProcessor(this), channelInactiveExecutor);
    }


    @Override
    public void start() {
        this.server.start();
    }

    public RegistryConsumerManager getConsumerManager() {
        return consumerManager;
    }

    public RegistryProviderManager getProvidermanager() {
        return providermanager;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public RegistryConfig getRegistryConfig() {
        return registryConfig;
    }

    public void setRegistryConfig(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }

}
