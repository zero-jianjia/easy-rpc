package com.zero.rpc;

import com.zero.registry.NotifyListener;
import com.zero.registry.OfflineListener;
import com.zero.registry.RegisterMeta;
import com.zero.registry.Registry;
import com.zero.registry.api.RegistryService;
import com.zero.transport.api.Client;
import com.zero.transport.Directory;
import com.zero.transport.Connection;
import com.zero.transport.UnresolvedAddress;

import java.util.Collection;

public interface RPCClient {

    /**
     * 每一个应用都建议设置一个appName.
     */
    String appName();

    /**
     * 网络层connector.
     */
    Client<Connection> connector();

    /**
     * 设置网络层connector.
     */
    RPCClient withConnector(Client<Connection> connector);

    RPCClient withRegistryService(RegistryService registryService);

    void connectToRegistryServer(String connectString);

    /**
     * 注册服务实例
     */
    RegistryService registryService();

    /**
     * 从本地容器查找服务信息.
     */
    Collection<RegisterMeta> lookup(Directory directory);

    /**
     * 设置对指定服务由jupiter自动管理连接.
     */
    Client.ConnectionWatcher watchConnections(Class<?> interfaceClass);

    /**
     * 设置对指定服务由jupiter自动管理连接.
     */
    Client.ConnectionWatcher watchConnections(Class<?> interfaceClass, String version);

    /**
     * 设置对指定服务由jupiter自动管理连接.
     */
    Client.ConnectionWatcher watchConnections(Directory directory);

    /**
     * 阻塞等待一直到该服务有可用连接或者超时.
     */
    boolean awaitConnections(Directory directory, long timeoutMillis);

    /**
     * 从注册中心订阅一个服务.
     */
    void subscribe(Directory directory, NotifyListener listener);

    /**
     * 服务下线通知.
     */
    void addOfflineListener(UnresolvedAddress address, OfflineListener listener);

    /**
     * 优雅关闭jupiter client.
     */
    void shutdownGracefully();
}
