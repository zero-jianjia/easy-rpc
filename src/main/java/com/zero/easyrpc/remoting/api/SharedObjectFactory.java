package com.zero.easyrpc.remoting.api;

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