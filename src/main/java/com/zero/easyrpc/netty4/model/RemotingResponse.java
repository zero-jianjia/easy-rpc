package com.zero.easyrpc.netty4.model;

import com.zero.easyrpc.netty4.InvokeCallback;
import com.zero.easyrpc.netty4.Transporter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 请求返回的对象包装类
 * Created by jianjia1 on 17/12/04.
 */
public class RemotingResponse {

    // 远程端返回的结果集
    private volatile Transporter transporter;

    // 该请求抛出的异常，如果存在的话
    private volatile Throwable cause;
    // 发送端是否发送成功
    private volatile boolean sendRequestOK = true;

    // 请求的opaque
    private final long opaque;

    // 默认的回调函数
    private final InvokeCallback invokeCallback;

    // 请求的默认超时时间
    private final long timeoutMillis;

    private final long beginTimestamp = System.currentTimeMillis();
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    public RemotingResponse(long opaque, long timeoutMillis, InvokeCallback invokeCallback) {
        this.invokeCallback = invokeCallback;
        this.opaque = opaque;
        this.timeoutMillis = timeoutMillis;
    }

    public void executeInvokeCallback() {
        if (invokeCallback != null) {
            invokeCallback.operationComplete(this);
        }
    }

    public boolean isSendRequestOK() {
        return sendRequestOK;
    }

    public void setSendRequestOK(boolean sendRequestOK) {
        this.sendRequestOK = sendRequestOK;
    }

    public long getOpaque() {
        return opaque;
    }

    public Transporter getTransporter() {
        return transporter;
    }

    public void setTransporter(Transporter transporter) {
        this.transporter = transporter;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public long getBeginTimestamp() {
        return beginTimestamp;
    }

    public Transporter waitResponse() throws InterruptedException{
        this.countDownLatch.await(this.timeoutMillis, TimeUnit.MILLISECONDS);
        return this.transporter;
    }

    /**
     * 当远程端返回结果的时候，TCP的长连接的上层载体channel 的handler会将其放入与requestId
     * 对应的Response中去
     * @param remotingTransporter
     */
    public void putResponse(final Transporter remotingTransporter){
        this.transporter = remotingTransporter;
        //接收到对应的消息之后需要countDown
        this.countDownLatch.countDown();
    }
}
