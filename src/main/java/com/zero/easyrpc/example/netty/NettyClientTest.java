package com.zero.easyrpc.example.netty;

import com.zero.easyrpc.common.exception.RemotingException;
import com.zero.easyrpc.transport.model.RemotingTransporter;
import com.zero.easyrpc.transport.netty.NettyClientConfig;
import com.zero.easyrpc.transport.netty.NettyRemotingClient;

/**
 * Created by jianjia1 on 17/12/04.
 */
public class NettyClientTest {
    public static final byte TEST = -1;

    public static void main(String[] args) throws InterruptedException, RemotingException {
        NettyClientConfig nettyClientConfig = new NettyClientConfig();
        NettyRemotingClient client = new NettyRemotingClient(nettyClientConfig);
        client.start();

        TestCommonCustomBody.ComplexTestObj complexTestObj = new TestCommonCustomBody.ComplexTestObj("attr1", 2);
        TestCommonCustomBody commonCustomHeader = new TestCommonCustomBody(1, "test",complexTestObj);

        RemotingTransporter remotingTransporter = RemotingTransporter.createRequestTransporter(TEST, commonCustomHeader);
        RemotingTransporter request = client.invokeSync("127.0.0.1:18001", remotingTransporter, 3000);
        System.out.println(request);
    }

}
