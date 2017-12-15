package com.zero.easyrpc.registry;

import com.zero.easyrpc.common.loadbalance.LoadBalanceStrategy;
import com.zero.easyrpc.common.rpc.ServiceReviewState;

import java.io.File;

/**
 * 注册中心的一些基本配置文件
 * Created by jianjia1 on 17/12/07.
 */
public class RegistryConfig {
    //持久化保存的位置
    private String storePathRootDir = System.getProperty("user.home") + File.separator + "test" + File.separator + "serviceInfo.json";

    private int persistInterval = 30; //每个多久时间刷盘到硬盘上，默认30s
    private LoadBalanceStrategy defaultLoadBalanceStrategy = LoadBalanceStrategy.WEIGHTINGRANDOM;
    private ServiceReviewState defaultReviewState = ServiceReviewState.HAS_NOT_REVIEWED; //默认的审核状态，默认是未审核

    public String getStorePathRootDir() {
        return storePathRootDir;
    }

    public void setStorePathRootDir(String storePathRootDir) {
        this.storePathRootDir = storePathRootDir;
    }

    public int getPersistInterval() {
        return persistInterval;
    }

    public void setPersistInterval(int persistInterval) {
        this.persistInterval = persistInterval;
    }

    public LoadBalanceStrategy getDefaultLoadBalanceStrategy() {
        return defaultLoadBalanceStrategy;
    }

    public void setDefaultLoadBalanceStrategy(LoadBalanceStrategy defaultLoadBalanceStrategy) {
        this.defaultLoadBalanceStrategy = defaultLoadBalanceStrategy;
    }

    public ServiceReviewState getDefaultReviewState() {
        return defaultReviewState;
    }

    public void setDefaultReviewState(ServiceReviewState defaultReviewState) {
        this.defaultReviewState = defaultReviewState;
    }


}
