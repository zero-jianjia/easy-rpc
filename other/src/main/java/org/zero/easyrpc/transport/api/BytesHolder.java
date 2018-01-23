package org.zero.easyrpc.transport.api;

/**
 * 消息体bytes载体, 应避免在IO线程中序列化/反序列化
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
