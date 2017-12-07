package com.zero.easyrpc.transport.model;

/**
 * Created by jianjia1 on 17/12/04.
 */
public class BytesHolder {

    private transient byte[] bytes;

    public byte[] bytes() {
        return bytes;
    }

    public void bytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public int size() {
        return bytes == null ? 0 : bytes.length;
    }
}
