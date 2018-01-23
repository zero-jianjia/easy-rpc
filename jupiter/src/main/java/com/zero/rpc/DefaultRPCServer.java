package com.zero.rpc;

import com.zero.rpc.model.ServiceRegistry;
import com.zero.transport.api.Server;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.zero.common.concurrent.NamedThreadFactory;
import org.zero.common.util.*;
import com.zero.registry.RegisterMeta;
import com.zero.registry.api.RegistryService;
import com.zero.rpc.provider.flow.control.FlowController;
import com.zero.rpc.model.ServiceMetaData;
import com.zero.rpc.model.ServiceWrapper;
import com.zero.rpc.provider.ProviderInterceptor;
import com.zero.rpc.provider.DefaultProviderProcessor;
import com.zero.transport.Directory;
import com.zero.serialization.api.Serializer;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.zero.common.util.Preconditions.checkArgument;
import static org.zero.common.util.Preconditions.checkNotNull;
import static org.zero.common.util.StackTraceUtil.stackTrace;

public class DefaultRPCServer implements RPCServer {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultRPCServer.class);

    static {
        //加载TracingUtil，完成ip、pid的静态类变量的初始化
        ClassUtil.classInitialize("com.zero.rpc.tracing.TracingUtil", 500);
    }

    // 服务延迟初始化的默认线程池
    private final Executor defaultInitializerExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("initializer"));

    // provider本地容器
    private final LocalProviderContainer providerContainer = new DefaultLocalProviderContainer();

    // 服务发布(SPI)
    private RegistryService registryService;

    // 网络层server
    private Server acceptor;

    private Serializer serializer;

    // 全局拦截器
    private ProviderInterceptor[] globalInterceptors;
    // 全局流量控制
    private FlowController<Request> globalFlowController;


    public DefaultRPCServer() {
    }

    /**
     * after invoker withSerializer
     * @param acceptor
     * @return
     */
    @Override
    public RPCServer withAcceptor(Server acceptor) {
        acceptor.withProcessor(new DefaultProviderProcessor(this));
        this.acceptor = acceptor;
        return this;
    }

    @Override
    public RegistryService registryService() {
        return registryService;
    }

    @Override
    public void connectToRegistryServer(String connectString) {
        registryService.connectToRegistryServer(connectString);
    }

    @Override
    public void withGlobalInterceptors(ProviderInterceptor... globalInterceptors) {
        this.globalInterceptors = globalInterceptors;
    }

    @Override
    public void withGlobalFlowController(FlowController<Request> globalFlowController) {
        this.globalFlowController = globalFlowController;
    }


    @Override
    public ServiceWrapper lookupService(Directory directory) {
        return providerContainer.lookupService(directory.directory());
    }

    @Override
    public ServiceWrapper removeService(Directory directory) {
        return providerContainer.removeService(directory.directory());
    }

    @Override
    public List<ServiceWrapper> allRegisteredServices() {
        return providerContainer.getAllServices();
    }

    @Override
    public void publish(ServiceWrapper serviceWrapper) {
        ServiceMetaData metadata = serviceWrapper.getMetadata();

        RegisterMeta meta = new RegisterMeta();
        meta.setHost(NetUtil.getLocalAddress());
        meta.setPort(acceptor.boundPort());
        meta.setGroup(metadata.getGroup());
        meta.setServiceProviderName(metadata.getServiceProviderName());
        meta.setVersion(metadata.getVersion());
        meta.setWeight(serviceWrapper.getWeight());
        meta.setConnCount(4);

        registryService.register(meta);
    }

    @Override
    public void publish(ServiceWrapper... serviceWrappers) {
        for (ServiceWrapper wrapper : serviceWrappers) {
            publish(wrapper);
        }
    }

    @Override
    public <T> void publishWithInitializer(ServiceWrapper serviceWrapper, ProviderInitializer<T> initializer) {
        publishWithInitializer(serviceWrapper, initializer, null);
    }

    @Override
    public <T> void publishWithInitializer(
            final ServiceWrapper serviceWrapper, final ProviderInitializer<T> initializer, Executor executor) {
        Runnable task = new Runnable() {

            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                try {
                    initializer.init((T) serviceWrapper.getServiceProvider());
                    publish(serviceWrapper);
                } catch (Exception e) {
                    logger.error("Error on {} #publishWithInitializer: {}.", serviceWrapper.getMetadata(), stackTrace(e));
                }
            }
        };
        if (executor == null) {
            defaultInitializerExecutor.execute(task);
        } else {
            executor.execute(task);
        }
    }

    @Override
    public void publishAll() {
        for (ServiceWrapper wrapper : providerContainer.getAllServices()) {
            publish(wrapper);
        }
    }

    @Override
    public void unpublish(ServiceWrapper serviceWrapper) {

        ServiceMetaData metadata = serviceWrapper.getMetadata();

        RegisterMeta meta = new RegisterMeta();
        meta.setHost(NetUtil.getLocalAddress());
        meta.setPort(acceptor.boundPort());
        meta.setGroup(metadata.getGroup());
        meta.setVersion(metadata.getVersion());
        meta.setServiceProviderName(metadata.getServiceProviderName());
        meta.setWeight(serviceWrapper.getWeight());
        meta.setConnCount(4);

        registryService.unregister(meta);
    }

    @Override
    public void unpublishAll() {
        for (ServiceWrapper wrapper : providerContainer.getAllServices()) {
            unpublish(wrapper);
        }
    }

    @Override
    public void start() throws InterruptedException {
        acceptor.start();
    }

    @Override
    public void startAsync() throws InterruptedException {
        acceptor.start(false);
    }

    @Override
    public void shutdownGracefully() {
        registryService.shutdownGracefully();
        acceptor.shutdownGracefully();
    }

    @Override
    public RPCServer withRegistryService(RegistryService registryService) {
        this.registryService = registryService;
        return this;
    }


    @Override
    public ServiceWrapper registInLocal(ServiceRegistry serviceRegistry) {
        ProviderInterceptor[] allInterceptors = null;
        List<ProviderInterceptor> tempList = Lists.newArrayList();
        if (globalInterceptors != null) {
            Collections.addAll(tempList, globalInterceptors);
        }
        if (serviceRegistry.getInterceptors() != null) {
            Collections.addAll(tempList, serviceRegistry.getInterceptors());
        }
        if (!tempList.isEmpty()) {
            allInterceptors = tempList.toArray(new ProviderInterceptor[tempList.size()]);
        }

        ServiceWrapper wrapper = new ServiceWrapper(serviceRegistry.getGroup(), serviceRegistry.getProviderName(),
                serviceRegistry.getVersion(),
                serviceRegistry.getServiceProvider(),
                allInterceptors, serviceRegistry.getExtensions());

        wrapper.setWeight(serviceRegistry.getWeight());
        wrapper.setExecutor(serviceRegistry.getExecutor());
        wrapper.setFlowController(serviceRegistry.getFlowController());

        providerContainer.registerService(wrapper.getMetadata().directory(), wrapper);

        return wrapper;
    }


    @Override
    public FlowController<Request> globalFlowController() {
        return globalFlowController;
    }

    @Override
    public RPCServer withSerializer(Serializer serializer) {
        this.serializer = serializer;
        return this;
    }

    @Override
    public Serializer serializer() {
        return this.serializer;
    }
}
