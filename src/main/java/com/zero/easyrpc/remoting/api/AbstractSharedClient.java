package com.zero.easyrpc.remoting.api;

/**
 * Created by zero on 2018/1/13.
 *  一个client对应一个channel
 *  但client的某些属性，比如nettyClient的bootstrap 是可以复用的，
 *  只需要根据nettyClient的bootstrap即可connect出一个新的client，相当于克隆出一个参数一样的该client
 *  而不需要重新创造出一个bootstrap
 *  这样依旧保证一个client对应一个channel
 */
public abstract class AbstractSharedClient extends AbstractClient {
    protected SharedObjectFactory factory;


    protected abstract SharedObjectFactory createSharedFactory();

}
