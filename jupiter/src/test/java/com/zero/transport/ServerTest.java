package com.zero.transport;

import com.zero.transport.Status;
import com.zero.transport.api.RequestBytes;
import com.zero.transport.api.ResponseBytes;
import com.zero.transport.api.Server;
import com.zero.transport.api.channel.Channel;
import com.zero.transport.api.processor.ProviderProcessor;
import com.zero.transport.netty4.server.NettyServer;

public class ServerTest {

    public static void main(String[] args) {
        Server server = new NettyServer(9100);
        server.withProcessor(new ProviderProcessor() {
            @Override
            public void handleRequest(Channel channel, RequestBytes request) throws Exception {
                String a = new String(request.getBytes());
                System.out.println(a);

                a += "007";
                ResponseBytes responseBytes = new ResponseBytes(request.invokeId());
                responseBytes.setBytes(a.getBytes());

                channel.write(responseBytes);
            }

            @Override
            public void handleException(Channel channel, RequestBytes request, Status status, Throwable cause) {

            }
        });

        try {
            server.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
