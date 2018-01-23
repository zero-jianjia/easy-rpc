package com.zero.easyrpc.rpc;


public interface Exporter<T> extends Node {

    Provider<T> getProvider();

    void unexport();

}