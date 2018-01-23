package com.zero.transport.api.channel;

import java.util.EventListener;

/**
 * Created by zero on 2018/1/18.
 */

public interface FutureListener<C> extends EventListener {

    void onSuccess(C c) throws Exception;

    void onFailure(C c, Throwable cause) throws Exception;
}
