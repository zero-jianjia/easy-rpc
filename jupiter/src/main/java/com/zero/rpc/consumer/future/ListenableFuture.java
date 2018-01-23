
package com.zero.rpc.consumer.future;

import com.zero.rpc.Listener;

/**
 * A future that accepts completion listeners.
 */
public interface ListenableFuture<V> {

    ListenableFuture<V> addListener(Listener<V> listener);

    ListenableFuture<V> addListeners(Listener<V>... listeners);

    ListenableFuture<V> removeListener(Listener<V> listener);

    ListenableFuture<V> removeListeners(Listener<V>... listeners);
}
