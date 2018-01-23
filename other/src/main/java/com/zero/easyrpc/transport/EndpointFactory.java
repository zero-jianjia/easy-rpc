package com.zero.easyrpc.transport;


import com.zero.easyrpc.transport.api.Client;
import com.zero.easyrpc.transport.api.Server;
import com.zero.easyrpc.rpc.URL;

public interface EndpointFactory {

    /**
     * create remote server
     * 
     * @param url
     * @param messageHandler
     * @return
     */
    Server createServer(URL url, MessageHandler messageHandler);

    /**
     * create remote client
     * 
     * @param url
     * @return
     */
    Client createClient(URL url);

    /**
     * safe release server
     * 
     * @param server
     * @param url
     */
    void safeReleaseResource(Server server, URL url);

    /**
     * safe release client
     * 
     * @param client
     * @param url
     */
    void safeReleaseResource(Client client, URL url);

}
