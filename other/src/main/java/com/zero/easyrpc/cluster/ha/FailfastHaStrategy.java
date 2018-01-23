
package com.zero.easyrpc.cluster.ha;


import com.zero.easyrpc.cluster.LoadBalance;
import com.zero.easyrpc.rpc.Referer;
import com.zero.easyrpc.rpc.Request;
import com.zero.easyrpc.rpc.Response;

/**
 * 
 * Fail fast ha strategy.
 *
 */
public class FailfastHaStrategy<T> extends AbstractHaStrategy<T> {

    @Override
    public Response call(Request request, LoadBalance<T> loadBalance) {
        Referer<T> refer = loadBalance.select(request);
        return refer.call(request);
    }
}
