package com.zero.easyrpc.z_example.transport.netty4;

import com.zero.easyrpc.rpc.DefaultResponseFuture;
import com.zero.easyrpc.rpc.protocol.Response_001;
import com.zero.easyrpc.transport.api.Client;
import com.zero.easyrpc.transport.netty4.ClientConfig;
import com.zero.easyrpc.transport.netty4.NettyClient;

import java.util.concurrent.TimeUnit;

/**
 * Created by zero on 2018/1/15.
 */
public class NettyClientTest {
    public static void main(String[] args) {

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setDefaultAddress("127.0.0.1:9100");

        Client client = new NettyClient(clientConfig, new StringCodec());
        client.open();


        try {
            for (int i = 0; i < 1; i++) {
                client.send("测试第" + i + "次");
                DefaultResponseFuture future = (DefaultResponseFuture)client.request("zero");
                System.out.println(((Response_001)future.get()).getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
