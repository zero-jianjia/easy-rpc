package com.zero.easyrpc.client.provider;

import com.zero.easyrpc.client.provider.model.DefaultProviderInactiveProcessor;
import com.zero.easyrpc.client.provider.model.ServiceState;
import com.zero.easyrpc.client.provider.model.ServiceWrapper;
import com.zero.easyrpc.common.exception.RemotingException;
import com.zero.easyrpc.common.protocal.Protocol;
import com.zero.easyrpc.common.serialization.SerializerFactory;
import com.zero.easyrpc.common.transport.body.AckCustomBody;
import com.zero.easyrpc.common.transport.body.ManagerServiceCustomBody;
import com.zero.easyrpc.common.transport.body.PublishServiceCustomBody;
import com.zero.easyrpc.common.utils.NamedThreadFactory;
import com.zero.easyrpc.common.utils.Pair;
import com.zero.easyrpc.netty4.Transporter;
import com.zero.easyrpc.netty4.ClientConfig;
import com.zero.easyrpc.netty4.Client;
import com.zero.easyrpc.netty4.Server;
import com.zero.easyrpc.netty4.ServerConfig;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 服务提供者端的具体实现
 */
public class DefaultProvider implements Provider {
    private static final Logger logger = LoggerFactory.getLogger(DefaultProvider.class);

    private int exposePort;            //提供RPC服务的端口
    private String registryAddress;    //注册中心的地址
    private String monitorAddress;     //监控中心的地址
    private Object[] obj;              //要提供的服务

    private Client nettyClient;          // 用于连接monitor和注册中心的Client
    private Server nettyRPCServer;       // 提供PRC服务的Server，等待被Consumer连接
    private Server nettyVIPRPCServer;    // 提供PRC服务的Server，等待被Consumer VIP连接

    private ClientConfig clientConfig;   // 向注册中心连接的netty client配置
    private ServerConfig serverConfig;     // 等待服务提供者连接的netty server的配置

    private RegistryController registryController;    // provider端向注册中心连接的业务逻辑的控制器
    private MonitorController monitorController;      // provider与monitor端通信的控制器
    private RPCController rpcController;              // consumer端远程调用的核心控制器

    private ExecutorService rpcExecutor;             // RPC调用的核心线程执行器
    private ExecutorService rpcVipExecutor;          // RPC调用VIP的核心线程执行器


    // 当前provider端状态是否健康，也就是说如果注册宕机后，该provider端的实例信息是失效，这是需要重新发送注册信息,因为默认状态下start就是发送，
    // 只有channel inactive的时候说明短线了，需要重新发布信息
    private boolean providerStateIsHealthy = true;

    private List<Transporter> publishedServiceList; //发布的服务的信息列表

    private Map<String, PublishServiceCustomBody> globalPublishService = new ConcurrentHashMap<>(); //全局发布的信息

    // 定时任务执行器
    // 做一些定时校验的活动和操作。比如定时检查监控中心的是否健康，定时发送一些统计的数据给监控中心，定时重发那些发给注册中心失败的注册信息
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("provider-timer"));

    public DefaultProvider() {
        this(new ClientConfig(), new ServerConfig());
    }

    public DefaultProvider(ClientConfig clientConfig, ServerConfig serverConfig) {

        this.clientConfig = clientConfig;
        this.serverConfig = serverConfig;

        this.registryController = new RegistryController(this);
        this.monitorController = new MonitorController(this);
        this.rpcController = new RPCController(this);
        initialize();
    }

    private void initialize() {
        this.nettyClient = new Client(this.clientConfig);

        this.nettyRPCServer = new Server(this.serverConfig);
        this.nettyVIPRPCServer = new Server(this.serverConfig);

        this.rpcExecutor = Executors.newFixedThreadPool(serverConfig.getWorkerThreads(), new NamedThreadFactory("providerExecutorThread-"));
        this.rpcVipExecutor = Executors.newFixedThreadPool(serverConfig.getWorkerThreads() / 2, new NamedThreadFactory("providerVIPExecutorThread-"));

        // 注册处理器
        registerProcessor();

        scheduledTask();
    }

    private void registerProcessor() {
        DefaultProviderRegistryProcessor defaultProviderRegistryProcessor = new DefaultProviderRegistryProcessor(this);

        // provider端作为client端去连接registry注册中心的处理器
        nettyClient.registerProcessor(Protocol.DEGRADE_SERVICE, defaultProviderRegistryProcessor, null);
        nettyClient.registerProcessor(Protocol.AUTO_DEGRADE_SERVICE, defaultProviderRegistryProcessor, null);

        // provider端连接registry时 链接inactive的时候要进行的操作
        // 设置registry的状态为不健康，告之registry重新发送服务注册信息
        nettyClient.registerChannelInactiveProcessor(new DefaultProviderInactiveProcessor(this), null);

        // provider端作为Server端去待调用者连接的处理器，此处理器只处理RPC请求
        nettyRPCServer.registerDefaultProcessor(new DefaultProviderRPCProcessor(this), this.rpcExecutor);
        nettyVIPRPCServer.registerDefaultProcessor(new DefaultProviderRPCProcessor(this), this.rpcVipExecutor);
    }

    private void scheduledTask() {
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // 延迟5秒，每隔60秒开始 发送注册服务信息
                try {
                    logger.info("schedule check publish service");
                    if (!providerStateIsHealthy) {
                        logger.info("channel which connected to registry has been inactived, need to republish service");
                        publishedAndStartProvider();
                    }
                } catch (Exception e) {
                    logger.warn("schedule publish failed [{}]", e.getMessage());
                }
            }
        }, 5, 60, TimeUnit.SECONDS);

        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("ready send message");
                    checkPublishFailMessage();
                } catch (InterruptedException | RemotingException e) {
                    logger.warn("schedule republish failed [{}]", e.getMessage());
                }
            }
        }, 1, 1, TimeUnit.MINUTES);

        //清理所有的服务的单位时间的失效过期的统计信息
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                logger.info("ready prepare send Report");
                clearAllServiceNextMinuteCallCount();
            }
        }, 5, 45, TimeUnit.SECONDS);

        // 如果监控中心的地址不是null，则需要定时发送统计信息
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                sendMetricsInfo();
            }
        }, 5, 60, TimeUnit.SECONDS);


        //检查是否有服务需要自动降级
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                checkAutoDegrade();
            }
        }, 30, 60, TimeUnit.SECONDS);
    }

    @Override
    public Provider registryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
        return this;
    }

    @Override
    public Provider monitorAddress(String monitorAddress) {
        this.monitorAddress = monitorAddress;
        return this;
    }

    @Override
    public Provider serviceListenPort(int port) {
        this.exposePort = port;
        return this;
    }

    @Override
    public Provider publishService(Object... obj) {
        this.obj = obj;
        return this;
    }

    @Override
    public void start() throws InterruptedException, RemotingException {

        logger.info("######### provider starting..... ########");
        // 编织服务
        this.publishedServiceList = registryController.getLocalServerWrapperManager().wrapperRegisterInfo(getExposePort(), this.obj);

        logger.info("registry center address [{}] servicePort [{}] service [{}]", this.registryAddress, this.exposePort, this.publishedServiceList);

        // 记录发布的信息的记录，方便其他地方做使用
        initGlobalService();

        nettyClient.start();

        try {
            // 发布任务
            publishedAndStartProvider();
            logger.info("######### provider start successfully..... ########");
        } catch (Exception e) {
            logger.error("publish service to registry failed [{}]", e.getMessage());
        }

        int port = this.exposePort;

        if (port != 0) {
            this.serverConfig.setListenPort(exposePort);
            this.nettyRPCServer.start();

            int vipPort = port - 2;
            this.serverConfig.setListenPort(vipPort);
            this.nettyVIPRPCServer.start();
        }
    }

    private void checkAutoDegrade() {
        registryController.checkAutoDegrade();
    }

    private void sendMetricsInfo() {
        monitorController.sendMetricsInfo();
    }

    private void clearAllServiceNextMinuteCallCount() {
        registryController.getServiceFlowControllerManager().clearAllServiceNextMinuteCallCount();
    }

    private void checkPublishFailMessage() throws InterruptedException, RemotingException {
        registryController.checkPublishFailMessage();
    }

    public List<Transporter> getPublishedServiceList() {
        return publishedServiceList;
    }

    @Override
    public void publishedAndStartProvider() throws InterruptedException, RemotingException {
        logger.info("publish service....");
        registryController.publishedAndStartProvider();
        // 发布之后再次将服务状态改成true
        providerStateIsHealthy = true;
    }

    @Override
    public void handlerRPCRequest(Transporter request, Channel channel) {
        rpcController.handlerRPCRequest(request, channel);
    }

    /**
     * 处理降级请求,把已有的服务降级
     * @param request
     * @param channel
     * @param degradeService
     * @return
     */
    public Transporter handlerDegradeServiceRequest(Transporter request, Channel channel, byte degradeService) {
        // 默认的ack返回体
        AckCustomBody ackCustomBody = new AckCustomBody(request.getRequestId(), false);
        Transporter response = Transporter.createResponseTransporter(Protocol.ACK, ackCustomBody, request.getRequestId());

        // 发布的服务是空的时候，默认返回操作失败
        if (publishedServiceList == null || publishedServiceList.size() == 0) {
            return response;
        }

        // 请求体
        ManagerServiceCustomBody subcribeRequestCustomBody = SerializerFactory.serializerImpl().readObject(request.getBytes(), ManagerServiceCustomBody.class);
        // 服务名
        String serviceName = subcribeRequestCustomBody.getSerivceName();

        // 判断请求的服务名是否在发布的服务中
        boolean checkSerivceIsExist = false;

        for (Transporter service : publishedServiceList) {
            PublishServiceCustomBody body = (PublishServiceCustomBody) service.getContent();
            if (body.getServiceProviderName().equals(serviceName) && body.isSupportDegradeService()) {
                checkSerivceIsExist = true;
                break;
            }
        }

        if (checkSerivceIsExist) {
            // 获取到当前服务的状态
            final Pair<ServiceState, ServiceWrapper> pair = registryController.getServiceContainer().lookupService(serviceName);
            ServiceState serviceState = pair.getKey();

            if (degradeService == Protocol.DEGRADE_SERVICE) {
                // 如果已经降级了，则直接返回成功
                serviceState.getDegrade().set(!serviceState.getDegrade().get());
            } else if (degradeService == Protocol.AUTO_DEGRADE_SERVICE) {
                serviceState.getIsAutoDegrade().set(true);
            }
            ackCustomBody.setSuccess(true);
        }
        return response;
    }


    private void initGlobalService() {
        List<Transporter> list = this.publishedServiceList;

        if (list != null && !list.isEmpty()) {
            for (Transporter remotingTransporter : list) {
                PublishServiceCustomBody customBody = (PublishServiceCustomBody) remotingTransporter.getContent();
                String serviceName = customBody.getServiceProviderName();
                this.globalPublishService.put(serviceName, customBody);
            }
        }
    }


    public Client getNettyClient() {
        return nettyClient;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public int getExposePort() {
        return exposePort;
    }

    public void setExposePort(int exposePort) {
        this.exposePort = exposePort;
    }

    public RPCController getRpcController() {
        return rpcController;
    }

    public boolean isProviderStateIsHealthy() {
        return providerStateIsHealthy;
    }

    public void setProviderStateIsHealthy(boolean providerStateIsHealthy) {
        this.providerStateIsHealthy = providerStateIsHealthy;
    }

    public String getMonitorAddress() {
        return monitorAddress;
    }

    public void setMonitorAddress(String monitorAddress) {
        this.monitorAddress = monitorAddress;
    }

    public Map<String, PublishServiceCustomBody> getGlobalPublishService() {
        return globalPublishService;
    }

    public void setGlobalPublishService(ConcurrentMap<String, PublishServiceCustomBody> globalPublishService) {
        this.globalPublishService = globalPublishService;
    }

    public RegistryController getRegistryController() {
        return registryController;
    }
}
