package com.zero.rpc.consumer;

import com.zero.rpc.ConsumerHook;
import com.zero.rpc.RPCClient;
import com.zero.rpc.ServiceProvider;
import com.zero.rpc.consumer.invoker.InvokeType;
import com.zero.serialization.api.Serializer;
import com.zero.transport.api.Client;
import org.zero.common.util.Lists;
import org.zero.common.util.Proxies;
import org.zero.common.util.Strings;
import com.zero.rpc.consumer.cluster.ClusterInvoker;
import com.zero.rpc.consumer.dispatcher.DefaultDispatcher;
import com.zero.rpc.consumer.dispatcher.Dispatcher;
import com.zero.rpc.consumer.invoker.AsyncInvoker;
import com.zero.rpc.consumer.invoker.SyncInvoker;
import com.zero.rpc.consumer.loadbalance.LoadBalancerFactory;
import com.zero.rpc.consumer.loadbalance.LoadBalancerType;
import com.zero.rpc.model.ClusterStrategyConfig;
import com.zero.rpc.model.MethodSpecialConfig;
import com.zero.rpc.model.ServiceMetaData;
import com.zero.transport.Directory;
import com.zero.transport.api.Connection;
import com.zero.transport.UnresolvedAddress;

import java.util.Collections;
import java.util.List;

import static org.zero.common.util.Preconditions.checkArgument;
import static org.zero.common.util.Preconditions.checkNotNull;

public class ProxyFactory<I> {
    // 接口类型
    private final Class<I> interfaceClass;
    // 服务组别
    private String group;
    // 服务名称
    private String providerName;
    // 服务版本号, 通常在接口不兼容时版本号才需要升级
    private String version;

    private RPCClient client;
    private Serializer serializer;
    // 软负载均衡类型
    private LoadBalancerType loadBalancerType = LoadBalancerType.getDefault();
    // provider地址
    private List<UnresolvedAddress> addresses;
    // 调用方式 [同步, 异步]
    private InvokeType invokeType = InvokeType.getDefault();
    // 调用超时时间设置
    private long timeoutMillis;
    // 指定方法的单独配置, 方法参数类型不做区别对待
    private List<MethodSpecialConfig> methodSpecialConfigs;
    // 消费者端钩子函数
    private List<ConsumerHook> hooks;
    // 集群容错策略
    private ClusterInvoker.Strategy strategy = ClusterInvoker.Strategy.getDefault();
    // failover重试次数
    private int retries = 2;

    public static <I> ProxyFactory<I> factory(Class<I> interfaceClass) {
        ProxyFactory<I> factory = new ProxyFactory<>(interfaceClass);
        // 初始化数据
        factory.addresses = Lists.newArrayList();
        factory.hooks = Lists.newArrayList();
        factory.methodSpecialConfigs = Lists.newArrayList();

        return factory;
    }

    private ProxyFactory(Class<I> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public Class<I> getInterfaceClass() {
        return interfaceClass;
    }

    public ProxyFactory<I> group(String group) {
        this.group = group;
        return this;
    }

    public ProxyFactory<I> providerName(String providerName) {
        this.providerName = providerName;
        return this;
    }

    public ProxyFactory<I> version(String version) {
        this.version = version;
        return this;
    }

    public ProxyFactory<I> directory(Directory directory) {
        return group(directory.getGroup())
                .providerName(directory.getServiceProviderName())
                .version(directory.getVersion());
    }

    public ProxyFactory<I> client(RPCClient client) {
        this.client = client;
        return this;
    }

    public ProxyFactory<I> serializer(Serializer serializer) {
        this.serializer = serializer;
        return this;
    }

    public ProxyFactory<I> loadBalancerType(LoadBalancerType loadBalancerType) {
        this.loadBalancerType = loadBalancerType;
        return this;
    }

    public ProxyFactory<I> addProviderAddress(UnresolvedAddress... addresses) {
        Collections.addAll(this.addresses, addresses);
        return this;
    }

    public ProxyFactory<I> addProviderAddress(List<UnresolvedAddress> addresses) {
        this.addresses.addAll(addresses);
        return this;
    }

    public ProxyFactory<I> invokeType(InvokeType invokeType) {
        this.invokeType = checkNotNull(invokeType);
        return this;
    }

    public ProxyFactory<I> timeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    public ProxyFactory<I> addMethodSpecialConfig(MethodSpecialConfig... methodSpecialConfigs) {
        Collections.addAll(this.methodSpecialConfigs, methodSpecialConfigs);
        return this;
    }

    public ProxyFactory<I> addHook(ConsumerHook... hooks) {
        Collections.addAll(this.hooks, hooks);
        return this;
    }

    public ProxyFactory<I> clusterStrategy(ClusterInvoker.Strategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public ProxyFactory<I> failoverRetries(int retries) {
        this.retries = retries;
        return this;
    }

    public I newProxyInstance() {
        // check arguments
        checkNotNull(interfaceClass, "interfaceClass");

        ServiceProvider annotation = interfaceClass.getAnnotation(ServiceProvider.class);

        if (annotation != null) {
            checkArgument(group == null,
                    interfaceClass.getName() + " has a @ServiceProvider annotation, can't set [group] again");
            checkArgument(providerName == null,
                    interfaceClass.getName() + " has a @ServiceProvider annotation, can't set [providerName] again");

            group = annotation.group();
            String name = annotation.name();
            providerName = Strings.isNotBlank(name) ? name : interfaceClass.getName();
        }

        checkArgument(Strings.isNotBlank(group), "group");
        checkArgument(Strings.isNotBlank(providerName), "providerName");
        checkNotNull(client, "client");
        checkNotNull(serializer, "serializer");

        // metadata
        ServiceMetaData metadata = new ServiceMetaData(
                group, providerName,
                Strings.isNotBlank(version) ? version : "1.0.0");

        Client<Connection> connector = client.connector();
        for (UnresolvedAddress address : addresses) {
            connector.addChannelGroup(metadata, connector.group(address));
        }

        // dispatcher
        Dispatcher dispatcher = new DefaultDispatcher(client, LoadBalancerFactory.loadBalancer(loadBalancerType), serializer)
                .hooks(hooks)
                .timeoutMillis(timeoutMillis)
                .methodSpecialConfigs(methodSpecialConfigs);

        ClusterStrategyConfig strategyConfig = ClusterStrategyConfig.of(strategy, retries);
        Object handler;
        switch (invokeType) {
            case SYNC:
                handler = new SyncInvoker(client.appName(), metadata, dispatcher, strategyConfig, methodSpecialConfigs);
                break;
            case ASYNC:
                handler = new AsyncInvoker(client.appName(), metadata, dispatcher, strategyConfig, methodSpecialConfigs);
                break;
            default:
                throw reject("invokeType: " + invokeType);
        }

        return Proxies.getDefault().newProxy(interfaceClass, handler);
    }

    private static UnsupportedOperationException reject(String message) {
        return new UnsupportedOperationException(message);
    }
}
