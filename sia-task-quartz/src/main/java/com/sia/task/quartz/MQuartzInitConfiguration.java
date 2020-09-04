/*-
 * <<
 * task
 * ==
 * Copyright (C) 2019 - 2020 sia
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * >>
 */

package com.sia.task.quartz;

import com.sia.task.quartz.core.Scheduler;

/**
 * MQuartzInitConfiguration
 *
 * @description
 * @see
 * @author @see Quartz
 * @data 2019-06-25 21:24
 * @version V1.0.0
 **/
public class MQuartzInitConfiguration {
    /**
     * key-name-of-the-system-configuration
     */
    public static final String PROPERTIES_FILE = "com.mquartz.properties";

    /**
     * scheduler_node_name
     */
    public static final String MQUARTZ_INSTANCE_NAME = "com.mquartz.scheduler.instanceName";

    public static final String MQUARTZ_THREAD_NAME = "com.mquartz.scheduler.threadName";

    public static final String MQUARTZ_THREAD_USE_JOBGROUP = "com.mquartz.scheduler.threadName.useJobGroup";

    public static final String MQUARTZ_BATCH_TIME_WINDOW = "com.mquartz.scheduler.batchTriggerAcquisitionFireAheadTimeWindow";

    public static final String MQUARTZ_MAX_BATCH_SIZE = "com.mquartz.scheduler.batchTriggerAcquisitionMaxCount";

    public static final String MQUARTZ_IDLE_WAIT_TIME = "com.mquartz.scheduler.idleWaitTime";

    public static final String MQUARTZ_MAKE_SCHEDULER_THREAD_DAEMON = "com.mquartz.scheduler.makeSchedulerThreadDaemon";

    public static final String MQUARTZ_SCHEDULER_THREADS_INHERIT_CONTEXT_CLASS_LOADER_OF_INITIALIZING_THREAD = "com.mquartz.scheduler.threadsInheritContextClassLoaderOfInitializer";

    public static final String MQUARTZ_CLASS_LOAD_HELPER_CLASS = "com.mquartz.scheduler.classLoadHelper.class";

    public static final String MQUARTZ_JOB_FACTORY_CLASS = "com.mquartz.scheduler.jobFactory.class";

    public static final String MQUARTZ_JOB_FACTORY_PREFIX = "com.mquartz.scheduler.jobFactory";

    /**
     * interruptJobsOnShutdown
     * interruptJobsOnShutdownWithWait
     * Used in conjunction with waitForJobsToComplete, you cannot use waitForJobsToComplete alone.
     */
    public static final String MQUARTZ_INTERRUPT_JOBS_ON_SHUTDOWN = "com.mquartz.scheduler.interruptJobsOnShutdown";

    public static final String MQUARTZ_INTERRUPT_JOBS_ON_SHUTDOWN_WITH_WAIT = "com.mquartz.scheduler.interruptJobsOnShutdownWithWait";

    /**
     * @see Scheduler#getContext()
     */
    public static final String MQUARTZ_CONTEXT_PREFIX = "com.mquartz.context.key";

    public static final String MQUARTZ_THREAD_POOL_PREFIX = "com.mquartz.threadPool";

    public static final String MQUARTZ_THREAD_POOL_CLASS = "com.mquartz.threadPool.class";

    public static final String MQUARTZ_JOB_STORE_PREFIX = "com.mquartz.jobStore";

    public static final String MQUARTZ_JOB_STORE_CLASS = "com.mquartz.jobStore.class";

    public static final String MQUARTZ_PLUGIN_PREFIX = "com.mquartz.plugin";

    public static final String MQUARTZ_PLUGIN_CLASS = "class";

    public static final String MQUARTZ_JOB_LISTENER_PREFIX = "com.mquartz.jobListener";

    public static final String MQUARTZ_TRIGGER_LISTENER_PREFIX = "com.mquartz.triggerListener";

    public static final String MQUARTZ_LISTENER_CLASS = "class";

    public static final String MQUARTZ_THREAD_EXECUTOR = "com.mquartz.threadExecutor";

    public static final String MQUARTZ_THREAD_EXECUTOR_CLASS = "com.mquartz.threadExecutor.class";

}
