package com.zero.easyrpc.common.exception;

/**
 * Created by jianjia1 on 17/12/04.
 */
public class RemotingException extends Exception {
    private static final long serialVersionUID = -298481855025395391L;

    public RemotingException(String message) {
        super(message);
    }

    public RemotingException(String message, Throwable cause) {
        super(message, cause);
    }
}