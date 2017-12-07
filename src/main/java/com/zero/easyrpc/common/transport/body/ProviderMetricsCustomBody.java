package com.zero.easyrpc.common.transport.body;

import com.zero.easyrpc.common.exception.RemotingCommmonCustomException;
import com.zero.easyrpc.common.rpc.MetricsReporter;

import java.util.List;

/**
 *  管理员发送给监控中心的信息
 * Created by jianjia1 on 17/12/04.
 */
public class ProviderMetricsCustomBody implements CommonCustomBody {

    private List<MetricsReporter> metricsReporter;

    @Override
    public void checkFields() throws RemotingCommmonCustomException {
    }

    public List<MetricsReporter> getMetricsReporter() {
        return metricsReporter;
    }

    public void setMetricsReporter(List<MetricsReporter> metricsReporter) {
        this.metricsReporter = metricsReporter;
    }


}
