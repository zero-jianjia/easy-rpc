package com.zero.easyrpc.transport.api;

/**
 * Created by zero on 2018/1/13.
 */
public interface Transporter {

    /**
     * Bind a server.
     */
    Server bind(ChannelHandler handler) throws RemotingException;

    /**
     * Connect to a server.
     */
    Client connect(ChannelHandler handler) throws RemotingException;

}
