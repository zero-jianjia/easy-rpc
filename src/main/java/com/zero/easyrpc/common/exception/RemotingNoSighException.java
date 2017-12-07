package com.zero.easyrpc.common.exception;

/**
 * Created by jianjia1 on 17/12/04.
 */
public class RemotingNoSighException extends RemotingException {

    private static final long serialVersionUID = -1661779813708564404L;


    public RemotingNoSighException(String message) {
        super(message, null);
    }


    public RemotingNoSighException(String message, Throwable cause) {
        super(message, cause);
    }

}
