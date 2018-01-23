package com.zero.easyrpc.rpc.protocol;


import com.zero.easyrpc.transport.NetUtils;
import com.zero.easyrpc.rpc.*;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AbstractInvoker.
 */
public abstract class AbstractInvoker<T> implements Invoker<T> {

    private final Class<T> type;

    private final Map<String, String> attachment;

    private volatile boolean available = true;

    private AtomicBoolean destroyed = new AtomicBoolean(false);

//    public AbstractInvoker(Class<T> type, URL url) {
//        this(type, url, (Map<String, String>) null);
//    }
//
//    public AbstractInvoker(Class<T> type, URL url, String[] keys) {
//        this(type, url, convertAttachment(url, keys));
//    }

    public AbstractInvoker(Class<T> type, Map<String, String> attachment) {
        if (type == null)
            throw new IllegalArgumentException("service type == null");
//        if (url == null)
//            throw new IllegalArgumentException("service url == null");
        this.type = type;
//        this.url = url;
        this.attachment = attachment == null ? null : Collections.unmodifiableMap(attachment);
    }

//    private static Map<String, String> convertAttachment(URL url, String[] keys) {
//        if (keys == null || keys.length == 0) {
//            return null;
//        }
//        Map<String, String> attachment = new HashMap<String, String>();
//        for (String key : keys) {
//            String value = url.getParameter(key);
//            if (value != null && value.length() > 0) {
//                attachment.put(key, value);
//            }
//        }
//        return attachment;
//    }

    public Class<T> getInterface() {
        return type;
    }

//    public URL getUrl() {
//        return url;
//    }

    public boolean isAvailable() {
        return available;
    }

    protected void setAvailable(boolean available) {
        this.available = available;
    }

    public void destroy() {
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }
        setAvailable(false);
    }

    public boolean isDestroyed() {
        return destroyed.get();
    }

//    public String toString() {
//        return getInterface() + " -> " + (getUrl() == null ? "" : getUrl().toString());
//    }

    public Response_001 invoke(Invocation inv) throws RpcException {
        if (destroyed.get()) {
            throw new RpcException("Rpc invoker for service " + this + " on consumer " + NetUtils.getLocalHost()
                    + " use dubbo version is DESTROYED, can not be invoked any more!");
        }
        RpcInvocation invocation = (RpcInvocation) inv;
        invocation.setInvoker(this);
        if (attachment != null && attachment.size() > 0) {
            invocation.addAttachmentsIfAbsent(attachment);
        }
//        Map<String, String> context = RpcContext.getContext().getAttachments();
//        if (context != null) {
//            invocation.addAttachmentsIfAbsent(context);
//        }
//        if (getUrl().getMethodParameter(invocation.getMethodName(), Constants.ASYNC_KEY, false)) {
//            invocation.setAttachment(Constants.ASYNC_KEY, Boolean.TRUE.toString());
//        }
//        RpcUtils.attachInvocationIdIfAsync(getUrl(), invocation);


        try {
            return doInvoke(invocation);
        } catch (Throwable e) { // biz exception

        }
        return null;
    }

    protected abstract Response_001 doInvoke(Invocation invocation) throws Throwable;

}