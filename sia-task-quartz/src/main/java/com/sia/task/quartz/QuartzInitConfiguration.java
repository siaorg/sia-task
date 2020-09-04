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
 * QuartzInitConfiguration
 *
 * @description
 * @see
 * @author @see Quartz
 * @data 2019-06-25 21:24
 * @version V1.0.0
 **/
public class QuartzInitConfiguration {
    /**
     * key-name-of-the-system-configuration
     */
    public static final String PROPERTIES_FILE = "com.sia.task.quartz.properties";

    /**
     * scheduler_node_name
     */
    public static final String MQUARTZ_INSTANCE_NAME = "com.sia.task.quartz.scheduler.instanceName";

    public static final String MQUARTZ_THREAD_NAME = "com.sia.task.quartz.scheduler.threadName";

    public static final String MQUARTZ_THREAD_USE_JOBGROUP = "com.sia.task.quartz.scheduler.threadName.useJobGroup";

    public static final String MQUARTZ_BATCH_TIME_WINDOW = "com.sia.task.quartz.scheduler.batchTriggerAcquisitionFireAheadTimeWindow";

    public static final String MQUARTZ_MAX_BATCH_SIZE = "com.sia.task.quartz.scheduler.batchTriggerAcquisitionMaxCount";

    public static final String MQUARTZ_IDLE_WAIT_TIME = "com.sia.task.quartz.scheduler.idleWaitTime";

    public static final String MQUARTZ_MAKE_SCHEDULER_THREAD_DAEMON = "com.sia.task.quartz.scheduler.makeSchedulerThreadDaemon";

    public static final String MQUARTZ_SCHEDULER_THREADS_INHERIT_CONTEXT_CLASS_LOADER_OF_INITIALIZING_THREAD = "com.sia.task.quartz.scheduler.threadsInheritContextClassLoaderOfInitializer";

    public static final String MQUARTZ_CLASS_LOAD_HELPER_CLASS = "com.sia.task.quartz.scheduler.classLoadHelper.class";

    public static final String MQUARTZ_JOB_FACTORY_CLASS = "com.sia.task.quartz.scheduler.jobFactory.class";

    public static final String MQUARTZ_JOB_FACTORY_PREFIX = "com.sia.task.quartz.scheduler.jobFactory";

    /**
     * interruptJobsOnShutdown
     * interruptJobsOnShutdownWithWait
     * Used in conjunction with waitForJobsToComplete, you cannot use waitForJobsToComplete alone.
     */
    public static final String MQUARTZ_INTERRUPT_JOBS_ON_SHUTDOWN = "com.sia.task.quartz.scheduler.interruptJobsOnShutdown";

    public static final String MQUARTZ_INTERRUPT_JOBS_ON_SHUTDOWN_WITH_WAIT = "com.sia.task.quartz.scheduler.interruptJobsOnShutdownWithWait";

    /**
     * @see Scheduler#getContext()
     */
    public static final String MQUARTZ_CONTEXT_PREFIX = "com.sia.task.quartz.context.key";

    public static final String MQUARTZ_THREAD_POOL_PREFIX = "com.sia.task.quartz.threadPool";

    public static final String MQUARTZ_THREAD_POOL_CLASS = "com.sia.task.quartz.threadPool.class";

    public static final String MQUARTZ_JOB_STORE_PREFIX = "com.sia.task.quartz.jobStore";

    public static final String MQUARTZ_JOB_STORE_CLASS = "com.sia.task.quartz.jobStore.class";

    public static final String MQUARTZ_PLUGIN_PREFIX = "com.sia.task.quartz.plugin";

    public static final String MQUARTZ_PLUGIN_CLASS = "class";

    public static final String MQUARTZ_JOB_LISTENER_PREFIX = "com.sia.task.quartz.jobListener";

    public static final String MQUARTZ_TRIGGER_LISTENER_PREFIX = "com.sia.task.quartz.triggerListener";

    public static final String MQUARTZ_LISTENER_CLASS = "class";

    public static final String MQUARTZ_THREAD_EXECUTOR = "com.sia.task.quartz.threadExecutor";

    public static final String MQUARTZ_THREAD_EXECUTOR_CLASS = "com.sia.task.quartz.threadExecutor.class";

}
