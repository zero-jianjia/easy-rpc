package com.zero.easyrpc.common.exception;

/**
 * Created by jianjia1 on 17/12/04.
 */
public class RemotingTimeoutException extends RemotingException {

	private static final long serialVersionUID = 8752267201986569541L;

	public RemotingTimeoutException(String message) {
        super(message);
    }


    public RemotingTimeoutException(String addr, long timeoutMillis) {
        this(addr, timeoutMillis, null);
    }


    public RemotingTimeoutException(String addr, long timeoutMillis, Throwable cause) {
        super("wait response on the channel <" + addr + "> timeout, " + timeoutMillis + "(ms)", cause);
    }
}
