package com.zero.easyrpc.client.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jianjia1 on 17/12/04.
 */
public class ServiceMeterManager {
    //key是serviceName
    private static Map<String, Meter> meterMap = new ConcurrentHashMap<>();

    /**
     * 计算某个服务的调用成功率，四舍五入到个位数
     * @param serviceName
     * @return
     */
    public static Integer calcServiceSuccessRate(String serviceName) {

        Meter meter = meterMap.get(serviceName);

        if (meter == null) {
            return 0;
        }

        int callCount = meter.getCallCount().intValue();
        int failCount = meter.getFailedCount().intValue();

        //如果调用的此时是0.默认成功率是100%
        if (callCount == 0) {
            return 100;
        }

        return (100 * (callCount - failCount) / callCount);
    }


    /**
     * 增加一次调用次数
     * @param serviceName
     */
    public static void incrementCallTimes(String serviceName) {
        Meter meter = meterMap.computeIfAbsent(serviceName, key -> new Meter(serviceName));
        meter.getCallCount().incrementAndGet();
    }

    /**
     * 增加一次调用失败次数
     * @param serviceName
     */
    public static void incrementFailTimes(String serviceName) {
        Meter meter = meterMap.computeIfAbsent(serviceName, key -> new Meter(serviceName));
        meter.getFailedCount().incrementAndGet();
    }

    /**
     * 累加某个服务的调用时间
     * @param serviceName
     */
    public static void incrementTotalTime(String serviceName, Long timecost) {
        Meter meter = meterMap.computeIfAbsent(serviceName, key -> new Meter(serviceName));
        meter.getTotalCallTime().addAndGet(timecost);
    }

    /**
     * 累加某个服务的请求入参的大小
     * @param serviceName
     * @param byteSize
     */
    public static void incrementRequestSize(String serviceName, int byteSize) {
        Meter meter = meterMap.computeIfAbsent(serviceName, key -> new Meter(serviceName));
        meter.getTotalRequestSize().addAndGet(byteSize);
    }


    public static void scheduledSendReport() {
    }

    public static Map<String, Meter> getMeterMap() {
        return meterMap;
    }
}
