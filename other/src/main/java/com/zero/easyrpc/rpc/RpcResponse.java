package com.zero.easyrpc.rpc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RpcResponse implements Response, Serializable {

    private static final long serialVersionUID = -6925924956850004727L;

    private Object result;

    private Throwable exception;

    private Map<String, String> attachments = new HashMap<String, String>();

    public RpcResponse() {
    }

    public RpcResponse(Object result) {
        this.result = result;
    }

    public RpcResponse(Throwable exception) {
        this.exception = exception;
    }

    public Object recreate() throws Throwable {
        if (exception != null) {
            throw exception;
        }
        return result;
    }

    @Deprecated
    public Object getResult() {
        return getValue();
    }

    @Deprecated
    public void setResult(Object result) {
        setValue(result);
    }

    public Object getValue() {
        return result;
    }

    public void setValue(Object value) {
        this.result = value;
    }

//    public Throwable getException() {
//        return exception;
//    }


    @Override
    public Exception getException() {
        return null;
    }

    public void setException(Throwable e) {
        this.exception = e;
    }

    public boolean hasException() {
        return exception != null;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, String> map) {
        if (map != null && map.size() > 0) {
            attachments.putAll(map);
        }
    }

    public String getAttachment(String key) {
        return attachments.get(key);
    }

    public String getAttachment(String key, String defaultValue) {
        String result = attachments.get(key);
        if (result == null || result.length() == 0) {
            result = defaultValue;
        }
        return result;
    }

    public void setAttachment(String key, String value) {
        attachments.put(key, value);
    }

    @Override
    public String toString() {
        return "RpcResponse [result=" + result + ", exception=" + exception + "]";
    }

    @Override
    public long getRequestId() {
        return 0;
    }

    @Override
    public long getProcessTime() {
        return 0;
    }

    @Override
    public void setProcessTime(long time) {

    }

    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    public void setRpcProtocolVersion(byte rpcProtocolVersion) {

    }

    @Override
    public byte getRpcProtocolVersion() {
        return 0;
    }
}