package com.zero.transport.api;

import com.zero.transport.UnresolvedAddress;

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
