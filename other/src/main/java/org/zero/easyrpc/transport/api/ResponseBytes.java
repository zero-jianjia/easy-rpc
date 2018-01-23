package org.zero.easyrpc.transport.api;

/**
 * 响应的消息体bytes载体, 避免在IO线程中序列化/反序列化
 */
public class ResponseBytes extends BytesHolder {

    private final long invokeId; // request.invokeId
    private byte status;

    public ResponseBytes(long id) {
        this.invokeId = id;
    }

    public long invokeId() {
        return invokeId;
    }

    public byte status() {
        return status;
    }

    public void status(byte status) {
        this.status = status;
    }
}
