package com.zero.easyrpc.netty4;

import com.zero.easyrpc.netty4.model.Response;

/**
 * 远程调用之后的回调函数
 * Created by jianjia1 on 17/12/04.
 */
public interface Callback {

    void operationComplete(final Response response);

}