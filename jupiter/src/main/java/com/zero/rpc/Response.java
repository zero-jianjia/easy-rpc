package com.zero.rpc;


import com.zero.transport.api.ResponseBytes;
import com.zero.rpc.model.ResultWrapper;
import com.zero.transport.Status;

/**
 * Provider's response data.
 * <p>
 * 响应信息载体.
 */
public class Response {

    private final ResponseBytes responseBytes; // 响应bytes
    private ResultWrapper result;              // 响应对象

    public Response(long id) {
        responseBytes = new ResponseBytes(id);
    }

    public Response(ResponseBytes responseBytes) {
        this.responseBytes = responseBytes;
    }

    public ResponseBytes responseBytes() {
        return responseBytes;
    }

    public long id() {
        return responseBytes.id();
    }

    public byte status() {
        return responseBytes.status();
    }

    public void status(byte status) {
        responseBytes.status(status);
    }

    public void status(Status status) {
        responseBytes.status(status.value());
    }

    public void bytes(byte[] bytes) {
        responseBytes.setBytes(bytes);
    }

    public ResultWrapper result() {
        return result;
    }

    public void result(ResultWrapper result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Response{" +
                "status=" + Status.parse(status()) +
                ", id=" + id() +
                ", result=" + result +
                '}';
    }
}
