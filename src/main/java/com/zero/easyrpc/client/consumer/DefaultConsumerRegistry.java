package com.zero.easyrpc.client.consumer;

import com.zero.easyrpc.common.exception.RemotingSendRequestException;
import com.zero.easyrpc.common.exception.RemotingTimeoutException;
import com.zero.easyrpc.common.protocal.Protocol;
import com.zero.easyrpc.common.transport.body.SubscribeRequestCustomBody;
import com.zero.easyrpc.netty4.Transporter;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消费者的注册处理功能
 * Created by jianjia1 on 17/12/07.
 */
public class DefaultConsumerRegistry {
    private static final Logger logger = LoggerFactory.getLogger(DefaultConsumerRegistry.class);

    private DefaultConsumer defaultConsumer;

    private Map<String, NotifyListener> serviceMatchedNotifyListener = new ConcurrentHashMap<>();

    private long timeout;

    public DefaultConsumerRegistry(DefaultConsumer defaultConsumer) {
        this.defaultConsumer = defaultConsumer;
        this.timeout = this.defaultConsumer.getConsumerConfig().getRegistryTimeout();
    }

    public void subcribeService(String serviceName, NotifyListener listener) {
        if (listener != null) {
            serviceMatchedNotifyListener.put(serviceName, listener);
        }

        if (defaultConsumer.getRegistyChannel() == null) {
           defaultConsumer.getOrUpdateHealthyChannel();
        }

        if (defaultConsumer.getRegistyChannel() != null) {

            logger.info("registry center channel is [{}]", defaultConsumer.getRegistyChannel());

            SubscribeRequestCustomBody body = new SubscribeRequestCustomBody();
            body.setServiceName(serviceName);

            Transporter remotingTransporter = Transporter.createRequestTransporter(Protocol.SUBSCRIBE_SERVICE, body);
            try {

                Transporter request = sendKernelSubscribeInfo(this.defaultConsumer.getRegistyChannel(), remotingTransporter, timeout);
                Transporter ackTransporter = this.defaultConsumer.getConsumerManager().handlerSubcribeResult(request,
                        this.defaultConsumer.getRegistyChannel());
                this.defaultConsumer.getRegistyChannel().writeAndFlush(ackTransporter);
            } catch (Exception e) {
                logger.warn("registry failed [{}]", e.getMessage());
            }

        } else {
            logger.warn("sorry can not connection to registry address [{}],please check your registry address", this.defaultConsumer.getRegistryClientConfig()
                    .getDefaultAddress());
        }

    }

    private Transporter sendKernelSubscribeInfo(Channel registyChannel, Transporter remotingTransporter, long timeout)
            throws RemotingTimeoutException, RemotingSendRequestException, InterruptedException {
        return this.defaultConsumer.getRegistryNettyClient().invokeSyncImpl(this.defaultConsumer.getRegistyChannel(), remotingTransporter, timeout);
    }

    public Map<String, NotifyListener> getServiceMatchedNotifyListener() {
        return serviceMatchedNotifyListener;
    }

    public void setServiceMatchedNotifyListener(ConcurrentHashMap<String, NotifyListener> serviceMatchedNotifyListener) {
        this.serviceMatchedNotifyListener = serviceMatchedNotifyListener;
    }


}
