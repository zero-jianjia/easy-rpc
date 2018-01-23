package org.zero.rpc.exception;


import com.zero.transport.Status;

import java.net.SocketAddress;

/**
 * Call timeout, usually thrown by client.
 *
 * jupiter
 * org.jupiter.rpc.exception
 *
 * @author jiachun.fjc
 */
public class JupiterTimeoutException extends JupiterRemoteException {

    private static final long serialVersionUID = 8768621104391094458L;

    private final Status status;

    public JupiterTimeoutException(SocketAddress remoteAddress, Status status) {
        super(remoteAddress);
        this.status = status;
    }

    public JupiterTimeoutException(Throwable cause, SocketAddress remoteAddress, Status status) {
        super(cause, remoteAddress);
        this.status = status;
    }

    public JupiterTimeoutException(String message, SocketAddress remoteAddress, Status status) {
        super(message, remoteAddress);
        this.status = status;
    }

    public JupiterTimeoutException(String message, Throwable cause, SocketAddress remoteAddress, Status status) {
        super(message, cause, remoteAddress);
        this.status = status;
    }

    public Status status() {
        return status;
    }

    @Override
    public String toString() {
        return "TimeoutException{" +
                "status=" + status +
                '}';
    }
}
