package com.zero.easyrpc.transport.support;


import com.zero.easyrpc.closable.Closable;
import com.zero.easyrpc.closable.ShutDownHook;
import com.zero.easyrpc.transport.api.Client;
import com.zero.easyrpc.transport.api.Endpoint;
import com.zero.easyrpc.transport.EndpointManager;
import com.zero.easyrpc.transport.HeartbeatFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class HeartbeatClientEndpointManager implements EndpointManager {

    private ConcurrentMap<Client, HeartbeatFactory> endpoints = new ConcurrentHashMap<Client, HeartbeatFactory>();

    // 一般这个类创建的实例会比较少，如果共享的话，容易“被影响”，如果某个任务阻塞了
    private ScheduledExecutorService executorService = null;

    @Override
    public void init() {
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {

                for (Map.Entry<Client, HeartbeatFactory> entry : endpoints.entrySet()) {
                    Client endpoint = entry.getKey();

                    try {
                        // 如果节点是存活状态，那么没必要走心跳
//                        if (endpoint.isAvailable()) {
//                            continue;
//                        }

                        HeartbeatFactory factory = entry.getValue();
//                        endpoint.heartbeat(factory.createRequest());
                    } catch (Exception e) {
//                        LoggerUtil.error("HeartbeatEndpointManager send heartbeat Error: url=" + endpoint.getUrl().getUri() + ", " + e.getMessage());
                    }
                }

            }
        }, 500, 500, TimeUnit.MILLISECONDS);
        ShutDownHook.registerShutdownHook(new Closable() {
            @Override
            public void close() {
                if (!executorService.isShutdown()) {
                    executorService.shutdown();
                }
            }
        });
    }

    @Override
    public void destroy() {
        executorService.shutdownNow();
    }

    @Override
    public void addEndpoint(Endpoint endpoint) {
        if (!(endpoint instanceof Client)) {
//            throw new MotanFrameworkException("HeartbeatClientEndpointManager addEndpoint Error: class not support " + endpoint.getClass());
        }

        Client client = (Client) endpoint;

//        URL url = endpoint.getUrl();
//
//        String heartbeatFactoryName = url.getParameter(URLParamType.heartbeatFactory.getName(), URLParamType.heartbeatFactory.getValue());

        HeartbeatFactory heartbeatFactory = null;// ExtensionLoader.getExtensionLoader(HeartbeatFactory.class).getExtension(heartbeatFactoryName);

//        if (heartbeatFactory == null) {
//            throw new MotanFrameworkException("HeartbeatFactory not exist: " + heartbeatFactoryName);
//        }

        endpoints.put(client, heartbeatFactory);
    }

    @Override
    public void removeEndpoint(Endpoint endpoint) {
        endpoints.remove(endpoint);
    }

    public Set<Client> getClients() {
        return Collections.unmodifiableSet(endpoints.keySet());
    }
}
