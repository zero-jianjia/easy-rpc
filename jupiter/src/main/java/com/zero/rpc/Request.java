package com.zero.rpc;

import com.zero.rpc.model.MessageWrapper;
import com.zero.rpc.tracing.TracingUtil;
import com.zero.transport.api.RequestBytes;

import java.util.Collections;
import java.util.Map;

public class Request {

    private final RequestBytes requestBytes;   // 请求bytes
    private MessageWrapper message;            // 请求对象

    public Request() {
        this(new RequestBytes());
    }

    public Request(RequestBytes requestBytes) {
        this.requestBytes = requestBytes;
    }

    public RequestBytes requestBytes() {
        return requestBytes;
    }

    public long invokeId() {
        return requestBytes.invokeId();
    }

    public long timestamp() {
        return requestBytes.timestamp();
    }

    public byte serializerCode() {
        return 0;
    }

    public void bytes(byte[] bytes) {
        requestBytes.setBytes(bytes);
    }

    public MessageWrapper message() {
        return message;
    }

    public void message(MessageWrapper message) {
        this.message = message;
    }

    public String getTraceId() {
        if (message == null) {
            return null;
        }
        return TracingUtil.safeGetTraceId(message.getTraceId()).asText();
    }

    public Map<String, String> getAttachments() {
        Map<String, String> attachments =
                message != null ? message.getAttachments() : null;
        return attachments != null ? attachments : Collections.<String, String>emptyMap();
    }

    public void putAttachment(String key, String value) {
        if (message != null) {
            message.putAttachment(key, value);
        }
    }

    @Override
    public String toString() {
        return "Request{" +
                "invokeId=" + invokeId() +
                ", timestamp=" + timestamp() +
                ", serializerCode=" + serializerCode() +
                ", message=" + message +
                '}';
    }
}
