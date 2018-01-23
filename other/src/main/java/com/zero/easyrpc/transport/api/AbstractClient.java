package com.zero.easyrpc.transport.api;

import com.zero.easyrpc.rpc.DefaultResponseFuture;
import com.zero.easyrpc.rpc.protocol.Request_001;

import java.net.InetSocketAddress;

/**
 * Created by zero on 2018/1/13.
 */
public abstract class AbstractClient implements Client {

    protected InetSocketAddress localAddress;
    protected InetSocketAddress remoteAddress;

    private volatile boolean closed;

    protected Codec codec;

    public AbstractClient(Codec codec) {
        this.codec = codec;
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
            doDisConnect();
        } catch (Throwable t) {
        }
        try {
            doClose();
        } catch (Throwable e) {
        }
        closed = true;
    }

    protected abstract void doOpen() throws Throwable;

    protected abstract void doClose() throws Throwable;

    protected abstract void doDisConnect() throws Throwable;


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

    public Codec getCodec() {
        return codec;
    }

    @Override
    public void send(Object message) throws RemotingException {
        Channel channel = getChannel();
        //TODO Can the value returned by getChannel() be null? need improvement.
        if (channel == null || !channel.isConnected()) {
            throw new RemotingException(this, "message can not send, because channel is closed ");
        }
        channel.send(message);
    }

    protected abstract Channel getChannel();


    @Override
    public Object request(Object request) throws TransportException {
        Request_001 req;
        if (request instanceof Request_001) {
            req = (Request_001) request;
        } else {
            req = new Request_001() {
                @Override
                public long getRequestId() {
                    return request.hashCode();
                }
            };
        }

        DefaultResponseFuture response = new DefaultResponseFuture(req);
        try {
            Channel channel = getChannel();
            if (channel == null) {
                response.cancel();
                return response;
            }
            channel.send(request);
        } catch (RemotingException e) {
            e.printStackTrace();
            response.cancel();
        }
        return response;
    }


    @Override
    public boolean isConnected() {
        return !closed;
    }

    @Override
    public void close(int timeout) {

    }

    @Override
    public boolean isClosed() {
        return closed;
    }
}
