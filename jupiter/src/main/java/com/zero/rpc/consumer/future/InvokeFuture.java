package com.zero.rpc.consumer.future;

public interface InvokeFuture<V> extends ListenableFuture<V> {

    Class<V> returnType();

    /**
     * Waits for this future to be completed and get the result.
     */
    V getResult() throws Throwable;
}
