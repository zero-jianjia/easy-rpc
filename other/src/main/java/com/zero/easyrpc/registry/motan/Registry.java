package com.zero.easyrpc.registry.motan;


import com.zero.easyrpc.rpc.URL;

/**
 * 
 * Used to register and discover.
 *
 */
public interface Registry extends RegistryService, DiscoveryService {

    URL getUrl();
}
