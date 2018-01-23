package org.zero.common.concurrent;


import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.zero.common.util.JConstants;
import org.zero.common.util.JvmTools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.zero.common.util.StackTraceUtil.stackTrace;

/**
 * Jupiter
 * org.jupiter.common.concurrent
 *
 * @author jiachun.fjc
 */
public abstract class AbstractRejectedExecutionHandler implements RejectedExecutionHandler {

    protected static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractRejectedExecutionHandler.class);

    private static final ExecutorService dumpExecutor = Executors.newSingleThreadExecutor();

    protected final String threadPoolName;
    private final AtomicBoolean dumpNeeded;
    private final String dumpPrefixName;

    public AbstractRejectedExecutionHandler(String threadPoolName, boolean dumpNeeded, String dumpPrefixName) {
        this.threadPoolName = threadPoolName;
        this.dumpNeeded = new AtomicBoolean(dumpNeeded);
        this.dumpPrefixName = dumpPrefixName;
    }

    public void dumpJvmInfo() {
        if (dumpNeeded.getAndSet(false)) {
            dumpExecutor.execute(new Runnable() {

                @Override
                public void run() {
                    String now = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                    String name = threadPoolName + "_" + now;
                    FileOutputStream fileOutput = null;
                    try {
                        fileOutput = new FileOutputStream(new File(dumpPrefixName + "_dump_" + name + ".log"));

                        List<String> stacks = JvmTools.jStack();
                        for (String s : stacks) {
                            fileOutput.write(s.getBytes(JConstants.UTF8));
                        }

                        List<String> memoryUsages = JvmTools.memoryUsage();
                        for (String m : memoryUsages) {
                            fileOutput.write(m.getBytes(JConstants.UTF8));
                        }

                        if (JvmTools.memoryUsed() > 0.9) {
                            JvmTools.jMap(dumpPrefixName + "_dump_" + name + ".bin", false);
                        }
                    } catch (Throwable t) {
                        logger.error("Dump jvm info error: {}.", stackTrace(t));
                    } finally {
                        if (fileOutput != null) {
                            try {
                                fileOutput.close();
                            } catch (IOException ignored) {}
                        }
                    }
                }
            });
        }
    }
}
