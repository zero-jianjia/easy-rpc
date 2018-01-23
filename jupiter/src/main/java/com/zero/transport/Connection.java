package com.zero.transport;

public abstract class Connection {

    private final UnresolvedAddress address;

    public Connection(UnresolvedAddress address) {
        this.address = address;
    }

    public UnresolvedAddress getAddress() {
        return address;
    }

    public void operationComplete(@SuppressWarnings("unused") Runnable callback) {
        // the default implementation does nothing
    }

    public abstract void setReconnect(boolean reconnect);
}
