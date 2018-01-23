package com.zero.easyrpc.monitor;

import com.zero.easyrpc.common.rpc.MetricsReporter;
import com.zero.easyrpc.common.rpc.RegisterMeta;
import com.zero.easyrpc.netty4.model.ChannelInactiveProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.ConcurrentSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * 
 * @description
 *              当某个服务提供者宕机的时候，会与monitor端之间的链接inactive，此时需要将这个服务器提供的所有的服务调用的次数的信息全部持久化到硬盘
 *              中，防止数据丢失
 */
public class DefaultMonitorChannelInactiveProcessor implements ChannelInactiveProcessor {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMonitorChannelInactiveProcessor.class);

	private DefaultMonitor defaultMonitor;

	public DefaultMonitorChannelInactiveProcessor(DefaultMonitor defaultMonitor) {
		this.defaultMonitor = defaultMonitor;
	}

	@Override
	public void processChannelInactive(ChannelHandlerContext ctx) {

		ConcurrentSet<RegisterMeta.Address> addresses = defaultMonitor.getGlobalProviderReporter().get(ctx.channel());
		if (null == addresses || addresses.isEmpty()) {
			logger.warn("channel [{}] provider no service", ctx.channel());
			return;
		}
		Collection<ConcurrentMap<RegisterMeta.Address, MetricsReporter>> value = defaultMonitor.getGlobalMetricsReporter().values();

		if (value != null && !value.isEmpty()) {

			for (ConcurrentMap<RegisterMeta.Address, MetricsReporter> eachMap : value) {
				
				for(RegisterMeta.Address address : addresses){
					
					MetricsReporter metricsReporter = eachMap.get(address);
					
					if(null != metricsReporter){ //将其更新到history map中去
						
						ConcurrentMap<RegisterMeta.Address, MetricsReporter>  historyMetrics = defaultMonitor.getHistoryGlobalMetricsReporter().get(metricsReporter.getServiceName());
						
						if(null == historyMetrics){
							
							historyMetrics = new ConcurrentHashMap<RegisterMeta.Address, MetricsReporter>();
							historyMetrics.put(address, metricsReporter);
							defaultMonitor.getHistoryGlobalMetricsReporter().put(metricsReporter.getServiceName(), historyMetrics);
						}else{
							MetricsReporter historyMetricsReporter= historyMetrics.get(address);
							
							if(null == historyMetricsReporter){
								
								historyMetricsReporter = metricsReporter;
								
							}else{
								historyMetricsReporter.setCallCount(historyMetricsReporter.getCallCount() + metricsReporter.getCallCount());
								historyMetricsReporter.setFailCount(historyMetricsReporter.getFailCount() + metricsReporter.getFailCount());
								historyMetricsReporter.setTotalReuqestTime(historyMetricsReporter.getTotalReuqestTime() + metricsReporter.getTotalReuqestTime());
							}
						}
						
						
						if(this.defaultMonitor.getMonitorConfig().isChangedPersistRightnow()){
							try {
								this.defaultMonitor.persistMetricsToDisk();
							} catch (IOException e) {
								logger.error("persist disk error exception [{}]",e.getMessage());
							}
						}
						metricsReporter.setCallCount(0l);
						metricsReporter.setFailCount(0l);
						metricsReporter.setTotalReuqestTime(0l);
					}
				}
			}
		}
	}

}
