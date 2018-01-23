package com.zero.easyrpc.transport.api;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * Created by zero on 2018/1/13.
 */
public interface Server extends Endpoint {

    Collection<Channel> getChannels();

    Channel getChannel(InetSocketAddress remoteAddress);

}
