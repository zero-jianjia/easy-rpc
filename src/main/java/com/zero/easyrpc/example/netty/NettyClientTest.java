package com.zero.easyrpc.example.netty;

import com.zero.easyrpc.common.exception.RemotingException;
import com.zero.easyrpc.netty4.Transporter;
import com.zero.easyrpc.netty4.ClientConfig;
import com.zero.easyrpc.netty4.Client;

/**
 * Created by jianjia1 on 17/12/04.
 */
public class NettyClientTest {
    public static final byte TEST = -1;

    public static void main(String[] args) throws InterruptedException, RemotingException {
        ClientConfig nettyClientConfig = new ClientConfig();
        Client client = new Client(nettyClientConfig);
        client.init();
        client.start();

        TestContentBody.ComplexTestObj complexTestObj = new TestContentBody.ComplexTestObj("attr1", 2);
        TestContentBody commonCustomHeader = new TestContentBody(1, "test",complexTestObj);

        Transporter transporter = Transporter.createRequestTransporter(TEST, commonCustomHeader);
        Transporter request = client.invokeSync("127.0.0.1:18001", transporter, 3000);
        System.out.println(request);
    }

}
