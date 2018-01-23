package com.zero.rpc.tracing;

import com.zero.rpc.tracing.TracingUtil;
import org.junit.Test;

/**
 * Created by zero on 2018/1/18.
 */
public class TracingUtilTest {

    @Test
    public void testGenerateTraceId() {
        String traceId = TracingUtil.generateTraceId();
        System.out.println(traceId);

        System.out.println(TracingUtil.getCurrent());
    }
}
