package com.zero.easyrpc.transport.api;

/**
 * Created by zero on 2018/1/13.
 */
public interface Client extends Endpoint {

    Object request(Object request) throws TransportException;

}
