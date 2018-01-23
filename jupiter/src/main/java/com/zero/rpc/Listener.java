package com.zero.rpc;

import java.util.EventListener;

public interface Listener<V> extends EventListener {

    /**
     * Returns result when the call succeeds.
     */
    void complete(V result);

    /**
     * Returns an exception message when call fails.
     */
    void failure(Throwable cause);
}
