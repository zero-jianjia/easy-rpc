
package com.zero.easyrpc.cluster.ha;


import com.zero.easyrpc.cluster.HAStrategy;
import com.zero.easyrpc.rpc.URL;

/**
 * 
 * Abstract ha strategy.
 *
 */

public abstract class AbstractHaStrategy<T> implements HAStrategy<T> {

    protected URL url;

    @Override
    public void setUrl(URL url) {
        this.url = url;
    }

}
