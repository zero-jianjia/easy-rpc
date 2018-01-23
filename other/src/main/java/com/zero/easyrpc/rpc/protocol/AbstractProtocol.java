package com.zero.easyrpc.rpc.protocol;

import com.zero.easyrpc.rpc.*;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractProtocol implements Protocol {
    protected ConcurrentHashMap<String, Exporter<?>> exporterMap = new ConcurrentHashMap<String, Exporter<?>>();

    public Map<String, Exporter<?>> getExporterMap() {
        return Collections.unmodifiableMap(exporterMap);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Exporter<T> export(Provider<T> provider, URL url) {
        if (url == null) {
        }

        if (provider == null) {
        }

        String protocolKey = null;
        //形如 protocl://10.222.22.75/group/com.weibo.api.motan.protocol.example.IWorld/1.0

        synchronized (exporterMap) {
            Exporter<T> exporter = (Exporter<T>) exporterMap.get(protocolKey);

            if (exporter != null) {
                throw new RuntimeException(this.getClass().getSimpleName() + " export Error: service already exist, url=" + url);
            }

            exporter = createExporter(provider, url);
            exporter.init();

            exporterMap.put(protocolKey, exporter);
            return exporter;
        }

    }

    public <T> Referer<T> refer(Class<T> clz, URL url) {
        return refer(clz, url, url);
    }

    @Override
    public <T> Referer<T> refer(Class<T> clz, URL url, URL serviceUrl) {
        if (url == null) {
        }

        if (clz == null) {

        }
        long start = System.currentTimeMillis();
        Referer<T> referer = createReferer(clz, url, serviceUrl);
        referer.init();

        return referer;
    }

    protected abstract <T> Exporter<T> createExporter(Provider<T> provider, URL url);

    protected abstract <T> Referer<T> createReferer(Class<T> clz, URL url, URL serviceUrl);

    @Override
    public void destroy() {
        for (String key : exporterMap.keySet()) {
            Node node = exporterMap.remove(key);

            if (node != null) {
                try {
                    node.destroy();

                } catch (Throwable t) {
                }
            }
        }
    }
}
