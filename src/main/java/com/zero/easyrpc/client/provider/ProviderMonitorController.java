package com.zero.easyrpc.client.provider;

import com.zero.easyrpc.client.metrics.Meter;
import com.zero.easyrpc.client.metrics.ServiceMeterManager;
import com.zero.easyrpc.common.protocal.Protocol;
import com.zero.easyrpc.common.rpc.MetricsReporter;
import com.zero.easyrpc.common.transport.body.ProviderMetricsCustomBody;
import com.zero.easyrpc.common.transport.body.PublishServiceCustomBody;
import com.zero.easyrpc.netty4.Transporter;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 *  provider 端连接monitor端的控制端
 * Created by jianjia1 on 17/12/04.
 */
public class ProviderMonitorController {
    private static final Logger logger = LoggerFactory.getLogger(ProviderMonitorController.class);

    private DefaultProvider defaultProvider;

    public ProviderMonitorController(DefaultProvider defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    /**
     * 定时发送信息到
     */
    public void sendMetricsInfo() {

        logger.info("scheduled sendMetricsInfos");

        if (defaultProvider.getMonitorAddress() == null) {
            logger.warn("monitor address is empty");
            return;
        }

        if(defaultProvider.getGlobalPublishService() == null){
            logger.warn("publish info is empty");
            return;
        }

        ConcurrentMap<String, Meter> metricsMap = ServiceMeterManager.getGlobalMeterManager();
        if (metricsMap != null && metricsMap.keySet() != null && metricsMap.values() != null) {

            List<MetricsReporter> reporters = new ArrayList<MetricsReporter>();

            List<Meter> meters = new ArrayList<Meter>();
            meters.addAll(metricsMap.values());

            if(!meters.isEmpty()){

                for(int i = 0;i<meters.size();i++){

                    MetricsReporter metricsReporter = new MetricsReporter();

                    String serviceName = meters.get(i).getServiceName();
                    PublishServiceCustomBody body = defaultProvider.getGlobalPublishService().get(serviceName);

                    if(body == null){
                        logger.warn("servicename [{}] has no publishInfo ",serviceName);
                        continue;
                    }

                    metricsReporter.setServiceName(serviceName);
                    metricsReporter.setHost(body.getHost());
                    metricsReporter.setPort(body.isVIPService() ? (body.getPort() -2):body.getPort());
                    metricsReporter.setCallCount(meters.get(i).getCallCount().get());
                    metricsReporter.setFailCount(meters.get(i).getFailedCount().get());
                    metricsReporter.setTotalReuqestTime(meters.get(i).getTotalCallTime().get());
                    metricsReporter.setRequestSize(meters.get(i).getTotalRequestSize().get());
                    reporters.add(metricsReporter);
                }
                ProviderMetricsCustomBody body = new ProviderMetricsCustomBody();
                body.setMetricsReporter(reporters);
                Transporter remotingTransporter = Transporter.createRequestTransporter(Protocol.MERTRICS_SERVICE, body);
                Channel channel = defaultProvider.getMonitorChannel();

                if (null != channel && channel.isActive() && channel.isWritable()) {
                    channel.writeAndFlush(remotingTransporter);
                }
            }
        }

    }

    public void checkMonitorChannel() throws InterruptedException {
        Channel channel = defaultProvider.getMonitorChannel();

        if ((null == channel || !channel.isActive()) && defaultProvider.getMonitorAddress() != null) {
            defaultProvider.initMonitorChannel();
        }
    }

}