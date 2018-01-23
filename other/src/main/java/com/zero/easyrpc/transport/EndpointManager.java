package com.zero.easyrpc.transport;

import com.zero.easyrpc.transport.api.Endpoint;

public interface EndpointManager {

    void init();

    void destroy();

    void addEndpoint(Endpoint endpoint);

    void removeEndpoint(Endpoint endpoint);

}
