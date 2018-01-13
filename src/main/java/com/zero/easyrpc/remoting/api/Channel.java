package com.zero.easyrpc.remoting.api;

import java.net.InetSocketAddress;

/**
 * 信道
 */
public interface Channel  {

    InetSocketAddress getLocalAddress();

    InetSocketAddress getRemoteAddress();

    boolean open();

    boolean isConnected();

    void close();

    void close(int timeout);

    boolean isClosed();

}