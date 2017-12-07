package com.zero.easyrpc.example.netty;

import com.zero.easyrpc.common.protocal.Protocol;
import com.zero.easyrpc.transport.model.NettyRequestProcessor;
import com.zero.easyrpc.transport.model.RemotingTransporter;
import com.zero.easyrpc.transport.netty.NettyRemotingServer;
import com.zero.easyrpc.transport.netty.NettyServerConfig;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.Executors;

import static com.zero.easyrpc.common.serialization.SerializerHolder.serializerImpl;

/**
 * Created by jianjia1 on 17/12/04.
 */
public class NettyServerTest {
    public static final byte TEST = -1;

    public static void main(String[] args) {

        NettyServerConfig config = new NettyServerConfig();
        config.setListenPort(18001);
        NettyRemotingServer server = new NettyRemotingServer(config);
        server.registerProecessor(TEST, new NettyRequestProcessor() {
            @Override
            public RemotingTransporter processRequest(ChannelHandlerContext ctx, RemotingTransporter transporter) throws Exception {
                transporter.setCustomHeader(serializerImpl().readObject(transporter.bytes(), TestCommonCustomBody.class));
                System.out.println(transporter);
                transporter.setTransporterType(Protocol.RESPONSE_REMOTING);
                return transporter;
            }
        }, Executors.newCachedThreadPool());
        server.start();
    }
}
