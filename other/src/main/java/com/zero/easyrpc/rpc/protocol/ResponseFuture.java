package com.zero.easyrpc.rpc.protocol;

public interface ResponseFuture {

    Object get() throws Exception;

    Object get(int timeoutInMillis) throws Exception;

    void setCallback(ResponseCallback callback);

    boolean isDone();

}