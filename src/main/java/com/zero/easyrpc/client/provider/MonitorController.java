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
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * provider 端连接monitor端的控制端
 * Created by jianjia1 on 17/12/04.
 */
public class MonitorController {
    private static final Logger logger = LoggerFactory.getLogger(MonitorController.class);

    private DefaultProvider defaultProvider;

    private Channel monitorChannel;    // 连接monitor端的channel

    public MonitorController(DefaultProvider defaultProvider) {
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

        if (defaultProvider.getGlobalPublishService() == null) {
            logger.warn("publish info is empty");
            return;
        }

        if (monitorChannelActive()) {
            logger.warn("channel is not active");
            return;
        }

        Map<String, Meter> metricsMap = ServiceMeterManager.getGlobalMeterManager();
        if (metricsMap != null) {
            List<MetricsReporter> reporters = new ArrayList<>();
            List<Meter> meters = new ArrayList<>();
            meters.addAll(metricsMap.values());

            if (!meters.isEmpty()) {

                for (int i = 0; i < meters.size(); i++) {
                    MetricsReporter metricsReporter = new MetricsReporter();

                    String serviceName = meters.get(i).getServiceName();
                    PublishServiceCustomBody body = defaultProvider.getGlobalPublishService().get(serviceName);

                    if (body == null) {
                        logger.warn("servicename [{}] has no publishInfo ", serviceName);
                        continue;
                    }

                    metricsReporter.setServiceName(serviceName);
                    metricsReporter.setHost(body.getHost());
                    metricsReporter.setPort(body.isVIPService() ? (body.getPort() - 2) : body.getPort());
                    metricsReporter.setCallCount(meters.get(i).getCallCount().get());
                    metricsReporter.setFailCount(meters.get(i).getFailedCount().get());
                    metricsReporter.setTotalReuqestTime(meters.get(i).getTotalCallTime().get());
                    metricsReporter.setRequestSize(meters.get(i).getTotalRequestSize().get());
                    reporters.add(metricsReporter);
                }

                ProviderMetricsCustomBody body = new ProviderMetricsCustomBody();
                body.setMetricsReporter(reporters);
                Transporter remotingTransporter = Transporter.createRequestTransporter(Protocol.MERTRICS_SERVICE, body);

                if (monitorChannel != null && monitorChannel.isActive() && monitorChannel.isWritable()) {
                    monitorChannel.writeAndFlush(remotingTransporter);
                }
            }
        }

    }

    public boolean monitorChannelActive() {
        if (defaultProvider.getMonitorAddress() == null) {
            return false;
        }

        if (monitorChannel == null || !monitorChannel.isActive()) {
            try {
                monitorChannel = defaultProvider.getNettyClient().createChannel(defaultProvider.getMonitorAddress());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (monitorChannel != null && monitorChannel.isActive()) {
            return true;
        }
        return false;
    }

}