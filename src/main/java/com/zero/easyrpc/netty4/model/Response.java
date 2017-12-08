package com.zero.easyrpc.netty4.model;

import com.zero.easyrpc.netty4.Callback;
import com.zero.easyrpc.netty4.Transporter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 请求返回的对象包装类
 * Created by jianjia1 on 17/12/04.
 */
public class Response {

    private final long id;// 请求ID
    private final long timeoutMillis; // 请求超时时间
    private final Callback callback;// 回调函数

    private volatile boolean sendRequestOK = true; //发送端是否发送成功
    private volatile Transporter transporter; // 远程端返回的结果

    private volatile Throwable cause; //该请求抛出的异常，如果存在的话

    private final long beginTimestamp = System.currentTimeMillis();
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    public Response(long id, long timeoutMillis, Callback callback) {
        this.id = id;
        this.timeoutMillis = timeoutMillis;
        this.callback = callback;
    }

    public void executeInvokeCallback() {
        if (callback != null) {
            callback.operationComplete(this);
        }
    }

    public boolean isSendRequestOK() {
        return sendRequestOK;
    }

    public void setSendRequestOK(boolean sendRequestOK) {
        this.sendRequestOK = sendRequestOK;
    }

    public long getId() {
        return id;
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

    public Transporter waitResponse() throws InterruptedException {
        this.countDownLatch.await(this.timeoutMillis, TimeUnit.MILLISECONDS);
        return this.transporter;
    }

    /**
     * 当远程端返回结果的时候，TCP的长连接的上层载体channel 的handler会将其放入与requestId
     * 对应的Response中去
     * @param transporter
     */
    public void putResponse(final Transporter transporter) {
        this.transporter = transporter;
        //接收到对应的消息之后需要countDown
        this.countDownLatch.countDown();
    }
}
