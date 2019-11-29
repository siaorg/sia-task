package com.sia.scheduler.log.worker;


import com.sia.core.entity.JobMTask;

import java.util.UUID;

/**
 * <p>
 * An LogTraceIdGenerator is responsible for generating the clusterwide
 * unique trace id for a <code>JobMTask</code>.
 * </p>
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2019-11-07 17:39
 * @see
 **/
public class LogTraceIdUUIDGenerator implements LogTraceIdGenerator {

    protected LogTraceIdUUIDGenerator(){
        super();
    }

    /**
     * Generate the trace id for a <code>JobMTask</code>
     *
     * @param mTask
     * @return The clusterwide unique trace id.
     */
    @Override
    public String generateTraceId(JobMTask mTask) {
        UUID randomUUID = UUID.randomUUID();
        return randomUUID.toString();
    }
}
