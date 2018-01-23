package com.zero.easyrpc.z_example.netty;

import com.zero.easyrpc.common.protocal.Protocol;
import com.zero.easyrpc.netty4.model.Processor;
import com.zero.easyrpc.netty4.Transporter;
import com.zero.easyrpc.netty4.Server;
import com.zero.easyrpc.netty4.ServerConfig;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.Executors;

import static com.zero.easyrpc.common.serialization.SerializerFactory.serializerImpl;

/**
 * Created by jianjia1 on 17/12/04.
 */
public class NettyServerTest {
    public static final byte TEST = -1;

    public static void main(String[] args) {

        ServerConfig config = new ServerConfig();
        config.setListenPort(18001);

        Server server = new Server(config);
        server.registerProecessor(TEST, new Processor() {
            @Override
            public Transporter processRequest(ChannelHandlerContext ctx, Transporter transporter) throws Exception {
                transporter.setContent(serializerImpl().readObject(transporter.getBytes(), TestContentBody.class));
                System.out.println(transporter);
                transporter.setType(Protocol.RESPONSE);
                return transporter;
            }
        }, Executors.newCachedThreadPool());
        server.init();
        server.start();
    }
}
