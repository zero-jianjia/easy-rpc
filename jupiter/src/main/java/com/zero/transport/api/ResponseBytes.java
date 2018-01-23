package com.zero.transport.api;

import com.zero.transport.BytesHolder;

/**
 * 响应的消息体bytes载体, 应避免在IO线程中序列化/反序列化
 */
public class ResponseBytes extends BytesHolder {

    private final long id; // request.invokeId
    private byte status;   // 状态码，区别响应结果是正常还是异常， must

    public ResponseBytes(long id) {
        this.id = id;
    }

    public long id() {
        return id;
    }

    public byte status() {
        return status;
    }

    public void status(byte status) {
        this.status = status;
    }
}
