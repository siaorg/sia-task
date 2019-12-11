package com.sia.scheduler.log.worker;

import com.sia.core.entity.JobMTask;

/**
 * <p>
 * An LogTraceIdGenerator is responsible for generating the clusterwide
 * unique trace id for a <code>JobMTask</code>.
 * </p>
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2019-11-07 17:28
 * @see
 **/
public interface LogTraceIdGenerator {

    /**
     * Generate the trace id for a <code>JobMTask</code>
     *
     * @param jobMTask
     * @return The clusterwide unique trace id.
     */
    String generateTraceId(JobMTask jobMTask);
}
