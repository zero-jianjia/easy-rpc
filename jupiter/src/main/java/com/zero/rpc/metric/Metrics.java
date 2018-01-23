package com.zero.rpc.metric;

import com.codahale.metrics.*;
import org.zero.common.util.ClassUtil;
import org.zero.common.util.JConstants;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.zero.common.util.Preconditions.checkNotNull;

/**
 * 指标度量
 */
public class Metrics {

    private static final MetricRegistry metricRegistry = new MetricRegistry();
    private static final ScheduledReporter scheduledReporter;

    static {
        // 检查是否存在slf4j, 使用Metrics必须显式引入slf4j依赖
        ClassUtil.classCheck("org.slf4j.Logger");

        if (JConstants.METRIC_CSV_REPORTER) {
            scheduledReporter = CsvReporter
                    .forRegistry(metricRegistry)
                    .build(new File(JConstants.METRIC_CSV_REPORTER_DIRECTORY));
        } else {
            ScheduledReporter _reporter;
            try {
                _reporter = Slf4jReporter
                        .forRegistry(metricRegistry)
                        .withLoggingLevel(Slf4jReporter.LoggingLevel.WARN)
                        .build();
            } catch (NoClassDefFoundError e) {
                // No Slf4j
                _reporter = ConsoleReporter.forRegistry(metricRegistry).build();
            }
            scheduledReporter = _reporter;
        }
        scheduledReporter.start(JConstants.METRIC_REPORT_PERIOD, TimeUnit.MINUTES);
    }

    /**
     * Return the global registry of metric instances.
     */
    public static MetricRegistry metricRegistry() {
        return metricRegistry;
    }

    /**
     * Return the {@link Meter} registered under this name; or create and registry
     * a new {@link Meter} if none is registered.
     */
    public static Meter meter(String name) {
        return metricRegistry.meter(checkNotNull(name, "name"));
    }

    /**
     * Return the {@link Meter} registered under this name; or create and registry
     * a new {@link Meter} if none is registered.
     */
    public static Meter meter(Class<?> clazz, String... names) {
        return metricRegistry.meter(MetricRegistry.name(clazz, names));
    }

    /**
     * Return the {@link Timer} registered under this name; or create and registry
     * a new {@link Timer} if none is registered.
     */
    public static Timer timer(String name) {
        return metricRegistry.timer(checkNotNull(name, "name"));
    }

    /**
     * Return the {@link Timer} registered under this name; or create and registry
     * a new {@link Timer} if none is registered.
     */
    public static Timer timer(Class<?> clazz, String... names) {
        return metricRegistry.timer(MetricRegistry.name(clazz, names));
    }

    /**
     * Return the {@link Counter} registered under this name; or create and registry
     * a new {@link Counter} if none is registered.
     */
    public static Counter counter(String name) {
        return metricRegistry.counter(checkNotNull(name, "name"));
    }

    /**
     * Return the {@link Counter} registered under this name; or create and registry
     * a new {@link Counter} if none is registered.
     */
    public static Counter counter(Class<?> clazz, String... names) {
        return metricRegistry.counter(MetricRegistry.name(clazz, names));
    }

    /**
     * Return the {@link Histogram} registered under this name; or create and registry
     * a new {@link Histogram} if none is registered.
     */
    public static Histogram histogram(String name) {
        return metricRegistry.histogram(checkNotNull(name, "name"));
    }

    /**
     * Return the {@link Histogram} registered under this name; or create and registry
     * a new {@link Histogram} if none is registered.
     */
    public static Histogram histogram(Class<?> clazz, String... names) {
        return metricRegistry.histogram(MetricRegistry.name(clazz, names));
    }

    private Metrics() {
    }
}
