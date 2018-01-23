package com.zero.rpc.provider;


import com.zero.rpc.model.ServiceWrapper;
import com.zero.transport.Directory;

public interface LookupService {

    /**
     * Lookup the service by {@link Directory}.
     */
    ServiceWrapper lookupService(Directory directory);
}
