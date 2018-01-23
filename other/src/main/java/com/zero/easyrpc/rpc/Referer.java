
package com.zero.easyrpc.rpc;


/**
 * 
 * 服务消费者
 * 
 */
public interface Referer<T> extends Caller<T>, Node {

    /**
     * 当前使用该referer的调用数
     * 
     * @return
     */
    int activeRefererCount();

    /**
     * 获取referer的原始service url
     * 
     * @return
     */
    URL getServiceUrl();
}
