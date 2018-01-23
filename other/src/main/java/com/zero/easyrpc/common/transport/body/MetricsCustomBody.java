package com.zero.easyrpc.common.transport.body;

import com.zero.easyrpc.common.exception.RemotingCommmonCustomException;
import com.zero.easyrpc.common.metrics.ServiceMetrics;

import java.util.List;

/**
 * Created by jianjia1 on 17/12/07.
 */
public class MetricsCustomBody implements ContentBody {

    private List<ServiceMetrics> serviceMetricses;

    @Override
    public void checkFields() throws RemotingCommmonCustomException {
    }

    public List<ServiceMetrics> getServiceMetricses() {
        return serviceMetricses;
    }

    public void setServiceMetricses(List<ServiceMetrics> serviceMetricses) {
        this.serviceMetricses = serviceMetricses;
    }




}