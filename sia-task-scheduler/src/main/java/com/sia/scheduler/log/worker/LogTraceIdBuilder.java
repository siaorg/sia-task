package com.sia.scheduler.log.worker;

import com.sia.core.entity.JobMTask;

/**
 * @author maozhengwei
 * @version V1.0.0
 * @data 2019-11-07 17:58
 * @see
 **/
public class LogTraceIdBuilder {

    private static LogTraceIdGenerator logTraceIdGenerator;

    static {
        logTraceIdGenerator = new LogTraceIdUUIDGenerator();
    }

    public static LogTraceIdGenerator build(LogTraceIdGenerator logTraceIdGenerator) {
        return logTraceIdGenerator;
    }

    public static String buildLogTraceId(JobMTask mTask) {
        return logTraceIdGenerator.generateTraceId(mTask);
    }
}
