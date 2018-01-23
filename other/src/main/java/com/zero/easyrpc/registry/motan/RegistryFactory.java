
package com.zero.easyrpc.registry.motan;


import com.zero.easyrpc.rpc.URL;

/**
 * 
 * To create registry
 *
 */
public interface RegistryFactory {

    Registry getRegistry(URL url);
}
