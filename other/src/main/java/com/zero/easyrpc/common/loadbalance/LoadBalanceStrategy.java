package com.zero.easyrpc.common.loadbalance;

/**
 * Created by jianjia1 on 17/12/04.
 */
public enum LoadBalanceStrategy {

    RANDOM, //随机
    WEIGHTINGRANDOM, //加权随机
    ROUNDROBIN, //轮询

}
