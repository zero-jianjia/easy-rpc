package com.zero.easyrpc.rpc.protocol;

import com.zero.easyrpc.rpc.*;

/**
 * 暴露服务
 * 该类包含有provider
 */
public abstract class AbstractExporter<T> extends AbstractNode implements Exporter<T> {
    protected Provider<T> provider;

    public AbstractExporter(Provider<T> provider, URL url) {
        super(url);
        this.provider = provider;
    }

    public Provider<T> getProvider() {
        return provider;
    }

    @Override
    public String desc() {
        return "[" + this.getClass().getSimpleName() + "] url=" + url;
    }
}