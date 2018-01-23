package com.zero.easyrpc.transport.api;

public interface SharedObjectFactory<T> {

    /**
     * 创建对象
     * @return
     */
    T makeObject();

    /**
     * 重建对象
     */
    boolean rebuildObject(T obj);

}