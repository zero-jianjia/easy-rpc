package com.zero.easyrpc.z_example.transport.netty4;

import com.zero.easyrpc.rpc.protocol.Response_001;
import com.zero.easyrpc.transport.api.*;
import com.zero.easyrpc.transport.netty4.NettyServer;
import com.zero.easyrpc.transport.netty4.ServerConfig;

/**
 * Created by zero on 2018/1/17.
 */
public class NettyServerTest {
    public static void main(String[] args) {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setWorkerThreads(1);
        serverConfig.setListenPort(9100);
        Server server = new NettyServer(serverConfig, new StringCodec(), new EasyChannelHandler() {

            @Override
            public Object handle(Channel channel, Object message) {
                System.out.println("handle " + message);
                String s = message + "-netty";
                System.out.println(345);

//                this.sent(channel, s);
                Response_001 response = new Response_001();
                response.setRequestId(message.hashCode());
                response.setValue(s);
                return response;
            }
        });
        server.open();


    }
}
