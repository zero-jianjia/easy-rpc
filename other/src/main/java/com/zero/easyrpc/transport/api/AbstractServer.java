package com.zero.easyrpc.transport.api;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * Created by zero on 2018/1/13.
 */
public abstract class AbstractServer implements Server {
    protected InetSocketAddress localAddress;
    protected InetSocketAddress remoteAddress;

    protected Codec codec;

    public AbstractServer() throws RemotingException {
    }

    @Override
    public boolean open() {
        try {
            doOpen();
            return true;
        } catch (Throwable t) {
        }
        return false;
    }

    @Override
    public void close() {
        try {
            doClose();
        } catch (Throwable t) {
        }
    }

    protected abstract void doOpen() throws Throwable;

    protected abstract void doClose() throws Throwable;


    public AbstractServer(Codec codec) {
        this.codec = codec;
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

    @Override
    public Collection<Channel> getChannels() {
        throw new RuntimeException(this.getClass().getName() + " getChannels() method unsupport ");
    }

    @Override
    public Channel getChannel(InetSocketAddress remoteAddress) {
        throw new RuntimeException(this.getClass().getName() + " getChannel(InetSocketAddress) method unsupport ");
    }

    public void setCodec(Codec codec) {
        this.codec = codec;
    }


    public Codec getCodec() {
        return codec;
    }

    @Override
    public void send(Object message) throws RemotingException {
        Collection<Channel> channels = getChannels();
        for (Channel channel : channels) {
            if (channel.isConnected()) {
                channel.send(message);
            }
        }
    }
}