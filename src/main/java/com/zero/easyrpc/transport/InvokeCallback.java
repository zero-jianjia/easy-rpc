package com.zero.easyrpc.transport;

import com.zero.easyrpc.transport.model.RemotingResponse;

/**
 * 远程调用之后的回调函数
 * Created by jianjia1 on 17/12/04.
 */
public interface InvokeCallback {

    void operationComplete(final RemotingResponse remotingResponse);

}