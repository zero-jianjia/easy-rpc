package com.zero.easyrpc.rpc;

/**
 *
 */
public abstract class AbstractNode implements Node {

    protected URL url;

    protected volatile boolean init = false;
    protected volatile boolean available = false;

    public AbstractNode(URL url) {
        this.url = url;
    }

    @Override
    public synchronized void init() {
        if (init) {
            return;
        }

        boolean result = doInit();

        if (!result) {
            throw new RuntimeException(this.getClass().getSimpleName() + " node init Error: " + desc());
        } else {

            init = true;
            available = true;
        }
    }

    protected abstract boolean doInit();

    @Override
    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public URL getUrl() {
        return url;
    }
}
