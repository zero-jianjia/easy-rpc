package com.zero.easyrpc.rpc;

import java.util.Map;

/**
 * Created by zero on 2018/1/15.
 */
public interface Response {

    /**
     * 如果 request 正常处理，那么会返回 Object value，而如果 request 处理有异常，那么 getValue 会抛出异常
     */
    Object getValue();

    /**
     * 如果request处理有异常，那么调用该方法return exception 如果request还没处理完或者request处理正常，那么return null
     */
    Exception getException();

    long getRequestId();

    /**
     * 业务处理时间
     */
    long getProcessTime();

    /**
     * 业务处理时间
     */
    void setProcessTime(long time);

    int getTimeout();

    Map<String, String> getAttachments();

    void setAttachment(String key, String value);

    // 获取rpc协议版本，可以依据协议版本做返回值兼容
    void setRpcProtocolVersion(byte rpcProtocolVersion);

    byte getRpcProtocolVersion();
}