package com.zero.easyrpc.client.provider;

import com.zero.easyrpc.client.metrics.ServiceMeterManager;
import com.zero.easyrpc.client.provider.flow.control.ServiceFlowControllerManager;
import com.zero.easyrpc.client.provider.model.ServiceState;
import com.zero.easyrpc.client.provider.model.ServiceWrapper;
import com.zero.easyrpc.common.exception.RemotingException;
import com.zero.easyrpc.common.serialization.SerializerFactory;
import com.zero.easyrpc.common.transport.body.AckCustomBody;
import com.zero.easyrpc.common.utils.Pair;
import com.zero.easyrpc.common.utils.SystemClock;
import com.zero.easyrpc.netty4.Transporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * provider端 连接registry的管理控制对象
 * 用来处理provider与registry的交互
 */
public class RegistryController {

    private static final Logger logger = LoggerFactory.getLogger(RegistryController.class);

    private DefaultProvider defaultProvider;

    private final Map<Long, MessageNonAck> messageNonAckMap = new ConcurrentHashMap<>();

    private LocalServerWrapperManager localServerWrapperManager;
    private ServiceContainer serviceContainer = new ServiceContainer();
    private ServiceFlowControllerManager serviceFlowControllerManager = new ServiceFlowControllerManager();


    public RegistryController(DefaultProvider defaultProvider) {
        this.defaultProvider = defaultProvider;
        localServerWrapperManager = new LocalServerWrapperManager(this);
    }

    /**
     * registry的地址，多个地址格式是host1:port1,host2:port2
     * @throws InterruptedException
     * @throws RemotingException
     */
    public void publishedAndStartProvider() throws InterruptedException, RemotingException {

        List<Transporter> transporters = defaultProvider.getPublishedServiceList();

        if (transporters == null || transporters.isEmpty()) {
            logger.warn("Service is empty, please call DefaultProvider #publishService method.");
            return;
        }

        String address = defaultProvider.getRegistryAddress();

        if (address == null) {
            logger.warn("Registry center address is empty.");
            return;
        }

        String[] addresses = address.split(",");
        if (addresses.length > 0) {
            for (String eachAddress : addresses) {
                for (Transporter service : transporters) {
                    pushPublishServiceToRegistry(service, eachAddress);
                }
            }
        }
    }

    private void pushPublishServiceToRegistry(Transporter service, String registerAddress) throws InterruptedException, RemotingException {
        messageNonAckMap.put(service.getRequestId(), new MessageNonAck(service, registerAddress));

        Transporter result = defaultProvider.getNettyClient().invokeSync(registerAddress, service, 3000);
        if (result != null) {
            AckCustomBody ackCustomBody = SerializerFactory.serializerImpl().readObject(result.getBytes(), AckCustomBody.class);

            logger.info("Received ack info [{}]", ackCustomBody);
            if (ackCustomBody.isSuccess()) {
                messageNonAckMap.remove(ackCustomBody.getRequestId());
            }
            logger.info("Publish service {} to Registry.", service);
        } else {
            logger.warn("registry center handler timeout");
        }
    }

    public void checkPublishFailMessage() throws InterruptedException, RemotingException {
        if (messageNonAckMap.keySet().size() > 0) {
            logger.warn("have [{}] message send failed,send again", messageNonAckMap.keySet().size());
            for (MessageNonAck ack : messageNonAckMap.values()) {
                pushPublishServiceToRegistry(ack.getMsg(), ack.getAddress());
            }
        }
    }

    /**
     * 检查符合自动降级的服务
     */
    public void checkAutoDegrade() {

        //获取到所有需要降级的服务名
        List<Pair<String, ServiceState>> needDegradeServices = serviceContainer.getNeedAutoDegradeService();

        //如果当前实例需要降级的服务列表不为空的情况下，循环每个列表
        if (!needDegradeServices.isEmpty()) {

            for (Pair<String, ServiceState> pair : needDegradeServices) {

                //服务名
                String serviceName = pair.getKey();
                //最低成功率
                Integer minSuccessRate = pair.getValue().getMinSuccecssRate();
                //调用的实际成功率
                Integer realSuccessRate = ServiceMeterManager.calcServiceSuccessRate(serviceName);

                if (minSuccessRate > realSuccessRate) {

                    final Pair<ServiceState, ServiceWrapper> servicePair = defaultProvider.getRegistryController()
                            .getServiceContainer().lookupService(serviceName);

                    ServiceState serviceState = servicePair.getKey();
                    if (!serviceState.getDegrade().get()) {
                        serviceState.getDegrade().set(true);
                    }
                }
            }
        }
    }


    public LocalServerWrapperManager getLocalServerWrapperManager() {
        return localServerWrapperManager;
    }

    public ServiceContainer getServiceContainer() {
        return serviceContainer;
    }

    public ServiceFlowControllerManager getServiceFlowControllerManager() {
        return serviceFlowControllerManager;
    }


    static class MessageNonAck {

        private final long id;
        private final Transporter msg;
        private final String address;

        private final long timestamp = SystemClock.millisClock().now();

        public MessageNonAck(Transporter msg, String address) {
            this.msg = msg;
            this.address = address;

            id = msg.getRequestId();
        }

        public long getId() {
            return id;
        }

        public Transporter getMsg() {
            return msg;
        }

        public String getAddress() {
            return address;
        }

        public long getTimestamp() {
            return timestamp;
        }

    }
}
