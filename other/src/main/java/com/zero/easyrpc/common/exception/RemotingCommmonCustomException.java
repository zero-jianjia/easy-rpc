package com.zero.easyrpc.common.exception;

/**
 * Created by jianjia1 on 17/12/04.
 */
public class RemotingCommmonCustomException extends Exception{
    private static final long serialVersionUID = 1546308581637799641L;

    public RemotingCommmonCustomException(String message) {
        super(message, null);
    }

    public RemotingCommmonCustomException(String message, Throwable cause) {
        super(message, cause);
    }
}
