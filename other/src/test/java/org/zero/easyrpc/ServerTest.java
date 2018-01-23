package org.zero.easyrpc;

import org.zero.easyrpc.transport.netty.NettyServer;

public class ServerTest {

    public static void main(String[] args) {
        NettyServer server = new NettyServer();
        server.init();
    }
}
