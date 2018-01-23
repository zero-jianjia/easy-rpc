/*
 *
 *   Copyright 2009-2016 Weibo, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.zero.easyrpc.rpc;

import com.zero.easyrpc.transport.api.Channel;
import com.zero.easyrpc.transport.api.RemotingException;
import com.zero.easyrpc.rpc.protocol.*;
import com.zero.easyrpc.rpc.protocol.Response_001;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zhanglei28 on 2017/9/11.
 */
public class DefaultResponseFuture implements ResponseFuture {

    private static final ConcurrentMap<Long, DefaultResponseFuture> FUTURES = new ConcurrentHashMap<>();


    protected volatile FutureState state = FutureState.DOING;


    protected Object result = null;
    protected Exception exception = null;

    protected long createTime = System.currentTimeMillis();
    protected int timeout = 5000;
    protected long processTime = 0;

    protected Request_001 request;
    Response_001 response;

    public DefaultResponseFuture(Request_001 request) {
        this.request = request;
        FUTURES.put(request.getRequestId(), this);
    }

    public DefaultResponseFuture(Request_001 requestObj, int timeout) {
        this.request = requestObj;
        this.timeout = timeout;
        FUTURES.put(request.getRequestId(), this);
    }

    public Object getValue() {
        synchronized (lock) {
            if (!isDoing()) {
                return response;
            }

            if (timeout <= 0) {
                try {
                    lock.wait();
                } catch (Exception e) {
                    cancel(new Exception(this.getClass().getName() + " getValue InterruptedException : "));
                }

                return response;
            } else {
                long waitTime = timeout - (System.currentTimeMillis() - createTime);

                if (waitTime > 0) {
                    for (; ; ) {
                        try {
                            lock.wait(waitTime);
                        } catch (InterruptedException e) {
                        }

                        if (!isDoing()) {
                            break;
                        } else {
                            waitTime = timeout - (System.currentTimeMillis() - createTime);
                            if (waitTime <= 0) {
                                break;
                            }
                        }
                    }
                }

                if (isDoing()) {
                    timeoutSoCancel();
                }
            }
            return response;
        }
    }

    public Exception getException() {
        return exception;
    }

    public boolean cancel() {
        Exception e =
                new Exception(this.getClass().getName() + " task cancel: serverPort");
        return cancel(e);
    }

    protected boolean cancel(Exception e) {
        synchronized (lock) {
            if (!isDoing()) {
                return false;
            }

            state = FutureState.CANCELLED;
            exception = e;
            lock.notifyAll();
        }

        return true;
    }

    public boolean isCancelled() {
        return state.isCancelledState();
    }

    public boolean isDone() {
        return state.isDoneState();
    }

    public boolean isSuccess() {
        return isDone() && (exception == null);
    }


    public long getCreateTime() {
        return createTime;
    }


    public Object getRequestObj() {
        return request;
    }

    public FutureState getState() {
        return state;
    }

    private void timeoutSoCancel() {
        this.processTime = System.currentTimeMillis() - createTime;

        synchronized (lock) {
            if (!isDoing()) {
                return;
            }

            state = FutureState.CANCELLED;
            exception =
                    new Exception(this.getClass().getName() + " request timeout: serverPort=");

            lock.notifyAll();
        }

    }


    private boolean isDoing() {
        return state.isDoingState();
    }

    protected boolean done() {
        synchronized (lock) {
            if (!isDoing()) {
                return false;
            }

            state = FutureState.DONE;
            lock.notifyAll();
        }

        return true;
    }

    public long getRequestId() {
        return this.request.getRequestId();
    }


    public long getProcessTime() {
        return processTime;
    }

    public void setProcessTime(long time) {
        this.processTime = time;
    }

    public int getTimeout() {
        return timeout;
    }


    public Object get() throws RemotingException {
        return get(timeout);
    }

    public Object get(int timeout) throws RemotingException {
        if (timeout <= 0) {
            timeout = 1000;
        }
        if (!isDone()) {
            long start = System.currentTimeMillis();
            lock.lock();
            try {
                while (!isDone()) {
                    done.await(timeout, TimeUnit.MILLISECONDS);
                    if (isDone() || System.currentTimeMillis() - start > timeout) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
            if (!isDone()) {
                throw new RemotingException("");
            }
        }
        return returnFromResponse();
    }


    private Object returnFromResponse() throws RemotingException {
        Response_001 res = response;
//        if (res == null) {
//            throw new IllegalStateException("response cannot be null");
//        }
//        if (res.getStatus() == Response_001.OK) {
//            return res.getResult();
//        }
//        if (res.getStatus() == Response_001.CLIENT_TIMEOUT || res.getStatus() == Response_001.SERVER_TIMEOUT) {
//            throw new TimeoutException(res.getStatus() == Response_001.SERVER_TIMEOUT, channel, res.getErrorMessage());
//        }
//        throw new RemotingException(channel, res.getErrorMessage());
        return res;
    }

    public static void received(Channel channel, Response_001 response) {
        DefaultResponseFuture future = FUTURES.remove(response.getRequestId());
        if (future != null) {
            future.doReceived((Response_001) response);
        } else {
            System.out.println("The timeout response finally returned at "
                    + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()))
                    + ", response " + response
                    + (channel == null ? "" : ", channel: " + channel.getLocalAddress()
                    + " -> " + channel.getRemoteAddress()));
        }
    }

    private final Lock lock = new ReentrantLock();
    private final Condition done = lock.newCondition();

    private void doReceived(Response_001 res) {
        System.out.println("doReceived");
        lock.lock();
        try {
            response = res;
            if (done != null) {
                state = FutureState.DONE;
                done.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setCallback(ResponseCallback callback) {

    }


    private final long start = System.currentTimeMillis();

    public long getStart() {
        return start;
    }

    private static class RemotingInvocationTimeoutScan implements Runnable {

        public void run() {
            while (true) {
                try {
                    for (DefaultResponseFuture future : FUTURES.values()) {
                        if (future == null || future.isDone()) {
                            continue;
                        }
                        if (System.currentTimeMillis() - future.getStart() > future.getTimeout()) {
                            // create exception response.
                            Response_001 timeoutResponse = new Response_001();
                            timeoutResponse.setRequestId(future.getRequestId());
                            // set timeout status.
                            timeoutResponse.setVersion("");
                            timeoutResponse.setException(new TimeoutException());
                            // handle response.
                            DefaultResponseFuture.received(null, timeoutResponse);
                        }
                    }
                    Thread.sleep(30);
                } catch (Throwable e) {
                }
            }
        }
    }

}
