package com.zero.rpc;


import com.zero.rpc.model.ServiceRegistry;
import com.zero.transport.api.Server;
import com.zero.registry.Registry;
import com.zero.registry.api.RegistryService;
import com.zero.rpc.provider.flow.control.FlowController;
import com.zero.rpc.model.ServiceWrapper;
import com.zero.rpc.provider.ProviderInterceptor;
import com.zero.transport.Directory;
import com.zero.serialization.api.Serializer;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * rpc server.
 */
public interface RPCServer {

    /**
     * 设置网络层server.
     */
    RPCServer withAcceptor(Server acceptor);

    /**
     * 设置注册服务实例
     */
    RPCServer withRegistryService(RegistryService registryService);

    void connectToRegistryServer(String connectString);

    /**
     * 设置全局的拦截器, 会拦截所有的服务提供者.
     */
    void withGlobalInterceptors(ProviderInterceptor... globalInterceptors);

    /**
     * 设置全局的流量控制器.
     */
    void withGlobalFlowController(FlowController<Request> flowController);

    FlowController<Request> globalFlowController();

    RPCServer withSerializer(Serializer serializer);

    Serializer serializer();

    ServiceWrapper lookupService(Directory directory);

    ServiceWrapper removeService(Directory directory);

    /**
     * 注册到本地，并返回ServiceWrapper
     */
    ServiceWrapper registInLocal(ServiceRegistry serviceRegistry);

    /**
     * 获取所有注册到本地服务.
     */
    List<ServiceWrapper> allRegisteredServices();

    /**
     * 发布指定服务到注册中心.
     */
    void publish(ServiceWrapper serviceWrapper);

    /**
     * 发布指定服务列表到注册中心.
     */
    void publish(ServiceWrapper... serviceWrappers);

    /**
     * 发布本地所有服务到注册中心.
     */
    void publishAll();

    /**
     * 注册服务实例
     */
    RegistryService registryService();

    /**
     * 服务提供者初始化完成后再发布服务到注册中心(延迟发布服务).
     */
    <T> void publishWithInitializer(ServiceWrapper serviceWrapper, ProviderInitializer<T> initializer);

    /**
     * 服务提供者初始化完成后再发布服务到注册中心(延迟发布服务), 并设置服务私有的线程池来执行初始化操作.
     */
    <T> void publishWithInitializer(ServiceWrapper serviceWrapper, ProviderInitializer<T> initializer, Executor executor);

    /**
     * 从注册中心把指定服务下线.
     */
    void unpublish(ServiceWrapper serviceWrapper);

    /**
     * 从注册中心把本地所有服务全部下线.
     */
    void unpublishAll();

    /**
     * 启动网络层server, 以同步阻塞的方式启动.
     */
    void start() throws InterruptedException;

    /**
     * 启动网络层server, 异步方式启动.
     */
    void startAsync() throws InterruptedException;

    /**
     * 关闭server.
     */
    void shutdownGracefully();


    interface ProviderInitializer<T> {
        /**
         * 初始化指定服务提供者.
         */
        void init(T provider);

    }
}
