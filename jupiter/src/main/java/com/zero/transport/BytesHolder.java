package com.zero.transport;

/**
 * 消息体bytes载体, 应避免在IO线程中序列化/反序列化
 */
public class BytesHolder {

    private byte serializerCode; //client端在request中设置该值，后续根据该值完成序列化/反序列化
    private byte[] bytes;

    public byte[] getBytes() {
        return bytes;
    }

    public BytesHolder setBytes(byte[] bytes) {
        this.bytes = bytes;
        return this;
    }

    public int size() {
        return bytes == null ? 0 : bytes.length;
    }

    public void nullBytes() {
        bytes = null; // help gc
    }

}
