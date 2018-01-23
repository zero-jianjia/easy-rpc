package com.zero.easyrpc.common.serialization;

/**
 * Created by jianjia1 on 17/12/04.
 */
public abstract class Serializer {

    public abstract byte code();

    public abstract <T> byte[] writeObject(T obj);

    public abstract <T> T readObject(byte[] bytes, int offset, int length, Class<T> clazz);

    public <T> T readObject(byte[] bytes, Class<T> clazz) {
        return readObject(bytes, 0, bytes.length, clazz);
    }
}
