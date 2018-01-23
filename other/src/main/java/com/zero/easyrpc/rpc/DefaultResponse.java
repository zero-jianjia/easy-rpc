
package com.zero.easyrpc.rpc;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Response_001 received via rpc.
 * 
 * @author fishermen
 * @version V1.0 created at: 2013-5-16
 */
public class DefaultResponse implements Response, Serializable {
    private static final long serialVersionUID = 4281186647291615871L;

    private Object value;
    private Exception exception;
    private long requestId;
    private long processTime;
    private int timeout;

    private Map<String, String> attachments;// rpc协议版本兼容时可以回传一些额外的信息

    private byte rpcProtocolVersion;

    public DefaultResponse() {}

    public DefaultResponse(long requestId) {
        this.requestId = requestId;
    }

    public DefaultResponse(Response response) {
        this.value = response.getValue();
        this.exception = response.getException();
        this.requestId = response.getRequestId();
        this.processTime = response.getProcessTime();
        this.timeout = response.getTimeout();
    }

    public DefaultResponse(Object value) {
        this.value = value;
    }

    public DefaultResponse(Object value, long requestId) {
        this.value = value;
    }

    public Object getValue() {
        if (exception != null) {
            throw (exception instanceof RuntimeException) ? (RuntimeException) exception : new RuntimeException(
                    exception.getMessage(), exception);
        }

        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    @Override
    public long getProcessTime() {
        return processTime;
    }

    @Override
    public void setProcessTime(long time) {
        this.processTime = time;
    }

    public int getTimeout() {
        return this.timeout;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getAttachments() {
        return attachments != null ? attachments : Collections.EMPTY_MAP;
    }

    @Override
    public void setAttachment(String key, String value) {
        if (this.attachments == null) {
            this.attachments = new HashMap<String, String>();
        }

        this.attachments.put(key, value);
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments;
    }

    @Override
    public byte getRpcProtocolVersion() {
        return rpcProtocolVersion;
    }

    public void setRpcProtocolVersion(byte rpcProtocolVersion) {
        this.rpcProtocolVersion = rpcProtocolVersion;
    }

}
