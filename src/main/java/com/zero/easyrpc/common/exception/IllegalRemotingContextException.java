package com.zero.easyrpc.common.exception;

/**
 * Created by jianjia1 on 17/12/05.
 */
public class IllegalRemotingContextException extends RemotingException {

    private static final long serialVersionUID = -302074448022156348L;

    public IllegalRemotingContextException(String message) {
        super(message, null);
    }

    public IllegalRemotingContextException(String message, Throwable cause) {
        super("Illegal context : " + message, cause);
    }

}
