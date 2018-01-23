package com.zero.easyrpc.rpc;

import com.zero.easyrpc.transport.api.Server;
import com.zero.easyrpc.rpc.protocol.AbstractExporter;
import com.zero.easyrpc.transport.EndpointFactory;
import com.zero.easyrpc.transport.ProviderMessageRouter;
import com.zero.easyrpc.transport.ProviderProtectedMessageRouter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务暴露
 * 1.Provider
 * 2.Server
 */
public class DefaultRpcExporter<T> extends AbstractExporter<T> {

    protected Server server;
    protected EndpointFactory endpointFactory;
    protected final ConcurrentHashMap<String, ProviderMessageRouter> ipPort2RequestRouter;
    protected final ConcurrentHashMap<String, Exporter<?>> exporterMap;

    public DefaultRpcExporter(Provider<T> provider, URL url, ConcurrentHashMap<String, ProviderMessageRouter> ipPort2RequestRouter,
                              ConcurrentHashMap<String, Exporter<?>> exporterMap) {
        super(provider, url);
        this.exporterMap = exporterMap;
        this.ipPort2RequestRouter = ipPort2RequestRouter;

        ProviderMessageRouter requestRouter = initRequestRouter(url);
        endpointFactory = null;
        server = endpointFactory.createServer(url, requestRouter);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unexport() {
        //protocol://host:port/group/interface/version
//        String protocolKey = MotanFrameworkUtil.getProtocolKey(url);
        String protocolKey = null;
        String ipPort = url.getServerPortStr();

        Exporter<T> exporter = (Exporter<T>) exporterMap.remove(protocolKey);

        if (exporter != null) {
            exporter.destroy();
        }
        ProviderMessageRouter requestRouter = ipPort2RequestRouter.get(ipPort);

        if (requestRouter != null) {
            requestRouter.removeProvider(provider);
        }
    }

    @Override
    protected boolean doInit() {
        boolean result = server.open();

        return result;
    }

    @Override
    public boolean isAvailable() {
//        return server.isAvailable();
        return false;
    }


    @Override
    public void destroy() {
        endpointFactory.safeReleaseResource(server, url);
    }

    protected ProviderMessageRouter initRequestRouter(URL url) {
        String ipPort = url.getServerPortStr();
        ProviderMessageRouter requestRouter = ipPort2RequestRouter.get(ipPort);

        if (requestRouter == null) {
            ipPort2RequestRouter.putIfAbsent(ipPort, new ProviderProtectedMessageRouter());
            requestRouter = ipPort2RequestRouter.get(ipPort);
        }
        requestRouter.addProvider(provider);

        return requestRouter;
    }
}
