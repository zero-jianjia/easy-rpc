package com.zero.easyrpc.netty4;

/**
 * Created by jianjia1 on 17/12/04.
 */
public class BytesHolder {

    private transient byte[] bytes;

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public int size() {
        return bytes == null ? 0 : bytes.length;
    }
}
