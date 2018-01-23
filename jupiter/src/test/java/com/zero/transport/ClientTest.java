package com.zero.transport;

import com.zero.transport.api.Client;
import com.zero.transport.api.RequestBytes;
import com.zero.transport.api.ResponseBytes;
import com.zero.transport.api.channel.Channel;
import com.zero.transport.api.channel.ChannelGroup;
import com.zero.transport.api.channel.FutureListener;
import com.zero.transport.api.processor.ConsumerProcessor;
import com.zero.transport.netty4.client.NettyClient;

public class ClientTest {
    public static void main(String[] args) {
        Client client = new NettyClient();
        client.withProcessor(new ConsumerProcessor() {
            @Override
            public void handleResponse(Channel channel, ResponseBytes response) throws Exception {
                String a = new String(response.getBytes());
                System.out.println(a);
            }
        });

        client.connect(new UnresolvedAddress("127.0.0.1", 9100));


        ChannelGroup group = client.group(new UnresolvedAddress("127.0.0.1", 9100));
        group.waitForAvailable(1000);

        Channel channel =  group.next();
        channel.write(new RequestBytes().setBytes("com/zero".getBytes()), new FutureListener<Channel>() {
            @Override
            public void onSuccess(Channel channel) throws Exception {
                System.out.println("onSuccess");
            }

            @Override
            public void onFailure(Channel channel, Throwable cause) throws Exception {
                cause.printStackTrace();
            }
        });

    }
}
