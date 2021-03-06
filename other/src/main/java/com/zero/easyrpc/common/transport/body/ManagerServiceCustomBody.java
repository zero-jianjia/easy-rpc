package com.zero.easyrpc.common.transport.body;

import com.zero.easyrpc.common.exception.RemotingCommmonCustomException;
import com.zero.easyrpc.common.loadbalance.LoadBalanceStrategy;
import com.zero.easyrpc.common.rpc.ManagerServiceRequestType;
import com.zero.easyrpc.common.rpc.RegisterMeta;
import com.zero.easyrpc.common.rpc.ServiceReviewState;

/**
 * 管理者发送给注册中心的修改服务的信息的主体信息
 * Created by jianjia1 on 17/12/04.
 */
public class ManagerServiceCustomBody implements ContentBody {

    private ManagerServiceRequestType managerServiceRequestType;//管理的类型
    private String serivceName;									//服务名
    private RegisterMeta.Address address;									//该服务提供的地址
    private ServiceReviewState serviceReviewState;				//是否审核通过
    private boolean isDegradeService;							//该服务是否降级
    private int weightVal;										//该服务的权重
    private LoadBalanceStrategy loadBalanceStrategy;			//服务访问策略

    @Override
    public void checkFields() throws RemotingCommmonCustomException {
    }

    public String getSerivceName() {
        return serivceName;
    }

    public void setSerivceName(String serivceName) {
        this.serivceName = serivceName;
    }

    public ServiceReviewState getServiceReviewState() {
        return serviceReviewState;
    }

    public void setServiceReviewState(ServiceReviewState serviceReviewState) {
        this.serviceReviewState = serviceReviewState;
    }

    public RegisterMeta.Address getAddress() {
        return address;
    }

    public void setAddress(RegisterMeta.Address address) {
        this.address = address;
    }

    public boolean isDegradeService() {
        return isDegradeService;
    }

    public void setDegradeService(boolean isDegradeService) {
        this.isDegradeService = isDegradeService;
    }

    public int getWeightVal() {
        return weightVal;
    }

    public void setWeightVal(int weightVal) {
        this.weightVal = weightVal;
    }

    public ManagerServiceRequestType getManagerServiceRequestType() {
        return managerServiceRequestType;
    }

    public void setManagerServiceRequestType(ManagerServiceRequestType managerServiceRequestType) {
        this.managerServiceRequestType = managerServiceRequestType;
    }

    public LoadBalanceStrategy getLoadBalanceStrategy() {
        return loadBalanceStrategy;
    }

    public void setLoadBalanceStrategy(LoadBalanceStrategy loadBalanceStrategy) {
        this.loadBalanceStrategy = loadBalanceStrategy;
    }

}
