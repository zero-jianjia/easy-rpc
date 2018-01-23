package com.zero.easyrpc.rpc;


public interface Caller<T> extends Node {

    Class<T> getInterface();

    Response call(Request request);
}
