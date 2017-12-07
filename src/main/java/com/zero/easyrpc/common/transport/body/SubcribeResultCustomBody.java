package com.zero.easyrpc.common.transport.body;

import com.zero.easyrpc.common.exception.RemotingCommmonCustomException;
import com.zero.easyrpc.common.loadbalance.LoadBalanceStrategy;
import com.zero.easyrpc.common.rpc.RegisterMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * 注册中心向consumer反馈的服务信息
 * Created by jianjia1 on 17/12/07.
 */
public class SubcribeResultCustomBody implements CommonCustomBody {

    private String serviceName;

    private LoadBalanceStrategy loadBalanceStrategy;

    private List<RegisterMeta> registerMeta = new ArrayList<RegisterMeta>();

    @Override
    public void checkFields() throws RemotingCommmonCustomException {
    }

    public List<RegisterMeta> getRegisterMeta() {
        return registerMeta;
    }

    public void setRegisterMeta(List<RegisterMeta> registerMeta) {
        this.registerMeta = registerMeta;
    }

    public LoadBalanceStrategy getLoadBalanceStrategy() {
        return loadBalanceStrategy;
    }

    public void setLoadBalanceStrategy(LoadBalanceStrategy loadBalanceStrategy) {
        this.loadBalanceStrategy = loadBalanceStrategy;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

}
