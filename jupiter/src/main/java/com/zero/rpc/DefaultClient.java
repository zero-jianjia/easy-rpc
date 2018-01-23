package com.zero.rpc;

import com.zero.registry.NotifyListener;
import com.zero.registry.OfflineListener;
import com.zero.registry.RegisterMeta;
import com.zero.registry.api.AbstractRegistryService;
import com.zero.registry.api.RegistryService;
import com.zero.transport.Connection;
import com.zero.transport.Directory;
import com.zero.transport.UnresolvedAddress;
import com.zero.transport.api.Client;
import com.zero.transport.api.ConnectionManager;
import com.zero.transport.api.channel.ChannelGroup;
import org.zero.common.util.*;
import com.zero.rpc.consumer.processor.DefaultConsumerProcessor;
import com.zero.rpc.model.ServiceMetaData;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.zero.common.util.Preconditions.checkNotNull;

public class DefaultClient implements RPCClient {
    static {
        ClassUtil.classInitialize("com.zero.rpc.tracing.TracingUtil", 500);
    }

    // 服务订阅(SPI)
    private RegistryService registryService;
    private final String appName;

    private Client<Connection> connector;

    public DefaultClient() {
        this("UNKNOWN");
    }


    public DefaultClient(String appName) {
        this.appName = appName;
    }

    @Override
    public String appName() {
        return appName;
    }

    @Override
    public Client<Connection> connector() {
        return connector;
    }

    @Override
    public RPCClient withConnector(Client<Connection> connector) {
        connector.withProcessor(new DefaultConsumerProcessor());
        this.connector = connector;
        return this;
    }

    @Override
    public RPCClient withRegistryService(RegistryService registryService) {
        this.registryService = registryService;

        return this;
    }

    @Override
    public RegistryService registryService() {
        return registryService;
    }

    @Override
    public Collection<RegisterMeta> lookup(Directory directory) {
        RegisterMeta.ServiceMeta serviceMeta = toServiceMeta(directory);
        return registryService.lookup(serviceMeta);
    }

    @Override
    public Client.ConnectionWatcher watchConnections(Class<?> interfaceClass) {
        return watchConnections(interfaceClass, null);
    }

    @Override
    public Client.ConnectionWatcher watchConnections(Class<?> interfaceClass, String version) {
        checkNotNull(interfaceClass, "interfaceClass");
        ServiceProvider annotation = interfaceClass.getAnnotation(ServiceProvider.class);
        checkNotNull(annotation, interfaceClass + " is not a ServiceProvider interface");

        String providerName = annotation.name();
        providerName = Strings.isNotBlank(providerName) ? providerName : interfaceClass.getName();

        version = Strings.isNotBlank(version) ? version : "1.0.0";

        return watchConnections(new ServiceMetaData(annotation.group(), providerName, version));
    }

    @Override
    public Client.ConnectionWatcher watchConnections(final Directory directory) {
        Client.ConnectionWatcher manager = new Client.ConnectionWatcher() {

            private final ConnectionManager connectionManager = connector.connectionManager();

            private final ReentrantLock lock = new ReentrantLock();
            private final Condition notifyCondition = lock.newCondition();
            // attempts to elide conditional wake-ups when the lock is uncontended.
            private final AtomicBoolean signalNeeded = new AtomicBoolean(false);

            @Override
            public void start() {
                subscribe(directory, new NotifyListener() {
                    @Override
                    public void notify(RegisterMeta registerMeta, NotifyEvent event) {
                        UnresolvedAddress address = new UnresolvedAddress(registerMeta.getHost(), registerMeta.getPort());
                        final ChannelGroup group = connector.group(address);

                        if (event == NotifyEvent.ADDED) {
                            if (!group.isAvailable()) {
                                Connection[] connections = connectTo(address, group, registerMeta, true);
                                for (Connection c : connections) {
                                    c.operationComplete(new Runnable() {
                                        @Override
                                        public void run() {
                                            onSucceed(group, signalNeeded.getAndSet(false));
                                        }
                                    });
                                }
                            } else {
                                onSucceed(group, signalNeeded.getAndSet(false));
                            }
                            group.setWeight(directory, registerMeta.getWeight()); // 设置权重
                        } else if (event == NotifyEvent.REMOVED) {
                            connector.removeChannelGroup(directory, group);
                            group.removeWeight(directory);
                            if (connector.directoryGroup().getRefCount(group) <= 0) {
                                connectionManager.cancelReconnect(address); // 取消自动重连
                            }
                        }
                    }

                    private Connection[] connectTo(final UnresolvedAddress address, final ChannelGroup group, RegisterMeta registerMeta, boolean async) {
                        int connCount = registerMeta.getConnCount();
                        connCount = connCount < 1 ? 1 : connCount;

                        Connection[] connections = new Connection[connCount];
                        group.setCapacity(connCount);
                        for (int i = 0; i < connCount; i++) {
                            Connection connection = connector.connect(address, async);
                            connections[i] = connection;
                            connectionManager.manage(connection);

                            addOfflineListener(address, () -> {
                                connectionManager.cancelReconnect(address); // 取消自动重连
                                if (!group.isAvailable()) {
                                    connector.removeChannelGroup(directory, group);
                                }
                            });
                        }

                        return connections;
                    }

                    private void onSucceed(ChannelGroup group, boolean doSignal) {
                        connector.addChannelGroup(directory, group);

                        if (doSignal) {
                            final ReentrantLock _look = lock;
                            _look.lock();
                            try {
                                notifyCondition.signalAll();
                            } finally {
                                _look.unlock();
                            }
                        }
                    }
                });
            }

            @Override
            public boolean waitForAvailable(long timeoutMillis) {
                if (connector.isDirectoryAvailable(directory)) {
                    return true;
                }

                long remains = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);

                boolean available = false;
                final ReentrantLock _look = lock;
                _look.lock();
                try {
                    signalNeeded.set(true);
                    // avoid "spurious wakeup" occurs
                    while (!(available = connector.isDirectoryAvailable(directory))) {
                        if ((remains = notifyCondition.awaitNanos(remains)) <= 0) {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    ExceptionUtil.throwException(e);
                } finally {
                    _look.unlock();
                }

                return available || connector.isDirectoryAvailable(directory);
            }
        };

        manager.start();

        return manager;
    }

    @Override
    public boolean awaitConnections(Directory directory, long timeoutMillis) {
        //添加ConnectionWatcher并启动
        Client.ConnectionWatcher watcher = watchConnections(directory);
        return watcher.waitForAvailable(timeoutMillis);
    }

    @Override
    public void subscribe(Directory directory, NotifyListener listener) {
        registryService.subscribe(toServiceMeta(directory), listener);
    }

    @Override
    public void addOfflineListener(UnresolvedAddress address, OfflineListener listener) {
        if (registryService instanceof AbstractRegistryService) {
            ((AbstractRegistryService) registryService).offlineListening(toAddress(address), listener);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void shutdownGracefully() {
        connector.shutdownGracefully();
    }

    @Override
    public void connectToRegistryServer(String connectString) {
        registryService.connectToRegistryServer(connectString);
    }


    private static RegisterMeta.ServiceMeta toServiceMeta(Directory directory) {
        RegisterMeta.ServiceMeta serviceMeta = new RegisterMeta.ServiceMeta();
        serviceMeta.setGroup(checkNotNull(directory.getGroup(), "group"));
        serviceMeta.setServiceProviderName(checkNotNull(directory.getServiceProviderName(), "serviceProviderName"));
        serviceMeta.setVersion(checkNotNull(directory.getVersion(), "version"));
        return serviceMeta;
    }

    private static RegisterMeta.Address toAddress(UnresolvedAddress address) {
        return new RegisterMeta.Address(address.getHost(), address.getPort());
    }
}
