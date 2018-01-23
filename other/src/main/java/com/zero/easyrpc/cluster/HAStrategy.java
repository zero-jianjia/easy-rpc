package com.zero.easyrpc.cluster;


import com.zero.easyrpc.rpc.Request;
import com.zero.easyrpc.rpc.Response;
import com.zero.easyrpc.rpc.URL;

/**
 * 
 * Ha strategy.
 *
 */
public interface HAStrategy<T> {

    void setUrl(URL url);

    Response call(Request request, LoadBalance<T> loadBalance);

}
