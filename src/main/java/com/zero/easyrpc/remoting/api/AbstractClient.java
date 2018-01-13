package com.zero.easyrpc.remoting.api;

import java.net.InetSocketAddress;

/**
 * Created by zero on 2018/1/13.
 */
public abstract class AbstractClient implements Client {

    protected InetSocketAddress localAddress;
    protected InetSocketAddress remoteAddress;

    protected Codec codec;

    public AbstractClient(){

    }

    public AbstractClient(Codec codec) {
        this.codec =codec;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    public void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }
}
