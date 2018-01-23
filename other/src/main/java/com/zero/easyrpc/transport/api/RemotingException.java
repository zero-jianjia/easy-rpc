package com.zero.easyrpc.transport.api;

import java.net.InetSocketAddress;

public class RemotingException extends Exception {

    private static final long serialVersionUID = -3160452149606778709L;

    private InetSocketAddress localAddress;

    private InetSocketAddress remoteAddress;

    public RemotingException(String msg) {
    }

    public RemotingException(Channel channel, String msg) {
    }

    public RemotingException(InetSocketAddress localAddress, InetSocketAddress remoteAddress, String message) {
    }

    public RemotingException(Channel channel, Throwable cause) {
    }

    public RemotingException(InetSocketAddress localAddress, InetSocketAddress remoteAddress, Throwable cause) {
        super(cause);

        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }

    public RemotingException(Channel channel, String message, Throwable cause) {
    }

    public RemotingException(InetSocketAddress localAddress, InetSocketAddress remoteAddress, String message,
            Throwable cause) {
        super(message, cause);

        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }

    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }
}