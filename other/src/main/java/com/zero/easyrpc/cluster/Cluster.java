package com.zero.easyrpc.cluster;

import com.zero.easyrpc.rpc.Caller;
import com.zero.easyrpc.rpc.Referer;
import com.zero.easyrpc.rpc.URL;

import java.util.List;

/**
 * Created by zero on 2018/1/17.
 * Cluster.call()用于保证高可用，并能够进行软负载均衡。
 * Cluster的本质是Referer对象的容器
 */
public interface Cluster<T> extends Caller<T> {

    @Override
    void init();

    void setUrl(URL url);

    void setLoadBalance(LoadBalance<T> loadBalance);

    void setHaStrategy(HAStrategy<T> haStrategy);

    void onRefresh(List<Referer<T>> referers);

    List<Referer<T>> getReferers();

    LoadBalance<T> getLoadBalance();
}
