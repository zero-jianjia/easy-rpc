
package com.zero.easyrpc.registry.motan;


import com.zero.easyrpc.rpc.URL;

import java.util.List;

/**
 * 
 * Deicovery service.
 *
 */

public interface DiscoveryService {

    void subscribe(URL url, NotifyListener listener);

    void unsubscribe(URL url, NotifyListener listener);

    List<URL> discover(URL url);
}
