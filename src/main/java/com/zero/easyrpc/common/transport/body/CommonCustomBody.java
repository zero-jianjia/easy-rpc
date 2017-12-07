package com.zero.easyrpc.common.transport.body;

import com.zero.easyrpc.common.exception.RemotingCommmonCustomException;

/**
 * Created by jianjia1 on 17/12/04.
 */
public interface CommonCustomBody {

    void checkFields() throws RemotingCommmonCustomException;
}
