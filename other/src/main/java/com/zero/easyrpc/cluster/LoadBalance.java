
package com.zero.easyrpc.cluster;


import com.zero.easyrpc.rpc.Referer;
import com.zero.easyrpc.rpc.Request;

import java.util.List;

/**
 * 
 * Loadbalance for select referer
 */
public interface LoadBalance<T> {

    void onRefresh(List<Referer<T>> referers);

    Referer<T> select(Request request);

    void selectToHolder(Request request, List<Referer<T>> refersHolder);

    void setWeightString(String weightString);

}
