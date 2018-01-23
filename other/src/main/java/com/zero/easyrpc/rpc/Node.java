
package com.zero.easyrpc.rpc;

public interface Node {

    void init();

    void destroy();

    boolean isAvailable();

    String desc();

    URL getUrl();
}
