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

package com.sia.task.scheduler.job;

import com.sia.task.core.util.Constant;
import com.sia.task.quartz.QuartzInitConfiguration;
import com.sia.task.quartz.core.Scheduler;
import com.sia.task.quartz.core.StdSchedulerFactory;
import com.sia.task.quartz.exception.SchedulerException;
import com.sia.task.quartz.job.JobDetail;
import com.sia.task.quartz.job.JobKey;
import com.sia.task.quartz.job.matchers.GroupMatcher;
import com.sia.task.quartz.job.trigger.Trigger;
import com.sia.task.quartz.listeners.ListenerManager;
import com.sia.task.quartz.plugin.LoggingJobHistoryPlugin;
import com.sia.task.scheduler.impl.InnerJobListener;
import com.sia.task.scheduler.impl.InnerSchedulerListener;
import com.sia.task.scheduler.impl.InnerTriggerListener;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * SiaSchedulerFactory
 * Example for producing <code>{@Link org.quartz.Scheduler}</code>
 * <p>
 * V1.0.1 redefine OnlineSchedulerFactory the design of the get <code>{@Link org.quartz.Scheduler}</code> instance，
 * Modifying <code>{@Link org.quartz.Scheduler} is no longer a singleton mode, but a native one.
 * Support for multiple <code>{@Link org.quartz.Scheduler} instances coexisting，
 * This is more conducive to resource isolation between different grouped schedulers, and no longer share the same scheduling thread pool.
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2018-06-23 10:32
 * @see
 **/
@Slf4j
public class SiaTaskSchedulerFactory {

    private static Scheduler defaultScheduler;

    private static StdSchedulerFactory schedulerFactory;

    private static SiaTaskSchedulerFactory siaTaskSchedulerFactory;

    private static Lock lock = new ReentrantLock();

    private static Lock defaultLock = new ReentrantLock();

    private static int MaxThreadCount = 5;

    private static final int defaultSchedulerMaxThreadCount = 1;
    // 100 - 20
    private static final int defaultSchedulerMaxJobCount = 3;

    private static volatile LinkedList<String> moveQueue = new LinkedList();


    static {
        siaTaskSchedulerFactory = new SiaTaskSchedulerFactory();
    }

    public SiaTaskSchedulerFactory() {
        schedulerFactory = new StdSchedulerFactory();
    }

    /**
     * Return a scheduler instance, schedler is quartz native。<code>{@see org.quartz.Scheduler}<code/>.
     * <p>
     * Query a qualified scheduler instance according to the specified `schedName`.
     * If it exists, it returns. If it does not exist, create a new scheduler instance according to the specific configuration information
     * </p>
     *
     * @param schedulerName Used to query or set the name of the scheduler instance
     * @return Scheduler instance
     * @throws Exception in case of any kind of processing failure
     */
    public static Scheduler getScheduler(String schedulerName) {
        return siaTaskSchedulerFactory.createSchedulerBySchedulerName(schedulerName);
    }

    /**
     * Return a scheduler instance, scheduler is quartz native。<code>{@link Scheduler}<code/>.
     * <p>
     * Query a qualified scheduler instance according to the specified `schedName`.
     * If it exists, it returns. If it does not exist, return null
     * </p>
     *
     * @param schedulerName
     * @return <code>{@link Scheduler}</code>
     */
    private static Scheduler getSchedulerIsExists(String schedulerName) {
        return schedulerFactory.getScheduler(schedulerName);
    }

    /**
     * @return
     * @see StdSchedulerFactory#getAllSchedulers()
     */
    private Collection<Scheduler> getAllSchedulers() {
        Collection<Scheduler> allSchedulers;
        allSchedulers = schedulerFactory.getAllSchedulers();

        return allSchedulers;
    }

    private Scheduler matchScheduler(String schedName, boolean isInit) throws SchedulerException {

        if (schedName == null) {
            return getDefaultScheduler();
        }

        Collection<Scheduler> allSchedulers = getAllSchedulers();
        Optional<Scheduler> optional = allSchedulers.stream().filter(sched -> {
            boolean flag = false;
            try {
                flag = sched.getSchedulerName().equals(schedName);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
            return flag;
        }).findFirst();
        if (optional.isPresent()) {
            return optional.get();
        }

        if (isInit) {
            return initScheduler(schedName);
        }

        return null;
    }

    /**
     * 默认使用第一分组，只有当数量达到上限时，进行分组
     * createGroupSchedulerIfNeeded
     *
     * @param schedulerName
     * @return
     */
    private Scheduler createSchedulerBySchedulerName(String schedulerName) {

        //get
        Scheduler scheduler = null;
        try {
            scheduler = schedulerFactory.getScheduler(schedulerName);
            scheduler = scheduler != null ? scheduler : createScheduler4GroupIfNecessary(schedulerName, false);
        } catch (Exception e) {
            log.error(Constant.LOG_EX_PREFIX + "moveJobCreateScheduler", e);
        }
        return scheduler;
    }

    /**
     * create Scheduler by schedulerName if necessary
     *
     * @param schedulerName
     * @return
     * @throws SchedulerException
     */
    private Scheduler createScheduler4GroupIfNecessary(String schedulerName, boolean ifNecessary) throws SchedulerException {
        Scheduler scheduler4GroupName;
        Scheduler defaultScheduler = getDefaultScheduler();
        if (defaultScheduler.isShutdown()) {
            defaultScheduler.start();
        }
        Set<JobKey> jobKeys = defaultScheduler.getJobKeys(GroupMatcher.anyGroup());
        // If the number of tasks exceeds the threshold of the defaultScheduler, create a new scheduler
        if (defaultSchedulerMaxJobCount < jobKeys.size() && ifNecessary) {
            Map.Entry<String, Long> entry = jobKeys.stream()
                    .collect(Collectors.groupingBy(JobKey::getGroup, Collectors.counting()))
                    .entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .findFirst()
                    .get();

            String groupName = entry.getKey();
            scheduler4GroupName = getSchedulerIsExists(groupName);
            if (scheduler4GroupName == null) {
                scheduler4GroupName = initScheduler(groupName);
                log.info(Constant.LOG_PREFIX + "=========================================================");
                log.info(Constant.LOG_PREFIX + "createScheduler4GroupIfNecessary Information:");
                log.info(Constant.LOG_PREFIX + "schedulerName : {}", schedulerName);
                log.info(Constant.LOG_PREFIX + "Scheduler Information:  {}", scheduler4GroupName.getSchedulerName());
                log.info(Constant.LOG_PREFIX + "=========================================================");
            }

            if (groupName.equals(schedulerName)) {
                return scheduler4GroupName;
            }
            return scheduler4GroupName;
        }
        return defaultScheduler;
    }


    /**
     * @param jobKey
     * @return
     * @throws SchedulerException
     */
    public static boolean switchHomeSchedulerIfNecessary(JobKey jobKey) throws SchedulerException {
        boolean switchHomeScheduler = false;
        Scheduler scheduler4GroupName = getSchedulerIsExists(jobKey.getGroup());
        Scheduler defaultScheduler = getDefaultScheduler();
        if (defaultScheduler != null) {
            Set<JobKey> jobKeys2 = defaultScheduler.getJobKeys(GroupMatcher.anyGroup());
            log.info(Constant.LOG_PREFIX + "=========================================================");
            log.info(Constant.LOG_PREFIX + "{}, {}", defaultScheduler.getSchedulerName(), jobKeys2);
            log.info(Constant.LOG_PREFIX + "=========================================================");

        }

        if (scheduler4GroupName != null) {
            Set<JobKey> jobKeys1 = scheduler4GroupName.getJobKeys(GroupMatcher.anyGroup());
            if (jobKeys1.contains(jobKey)) {
                log.info(Constant.LOG_PREFIX + "=========================================================");
                log.info(Constant.LOG_PREFIX + "{}, {}", scheduler4GroupName.getSchedulerName(), jobKey);
                log.info(Constant.LOG_PREFIX + "=========================================================");
            }
        }

        if (scheduler4GroupName != null) {
            Set<JobKey> jobKeys = SiaTaskSchedulerFactory.defaultScheduler.getJobKeys(GroupMatcher.anyGroup());
            JobDetail jobDetail = SiaTaskSchedulerFactory.defaultScheduler.getJobDetail(jobKey);

            List<? extends Trigger> triggersOfJob = SiaTaskSchedulerFactory.defaultScheduler.getTriggersOfJob(jobKey);
            Trigger trigger = triggersOfJob.get(0);
            if (jobKeys.contains(jobKey) && SiaTaskSchedulerFactory.defaultScheduler.deleteJob(jobKey)) {
                if (scheduler4GroupName.isShutdown()) {
                    scheduler4GroupName.start();
                }
                scheduler4GroupName.scheduleJob(jobDetail, trigger);
                switchHomeScheduler = true;
            }
        }
        return switchHomeScheduler;
    }

    /**
     * <p>
     * get the default scheduler instance，
     * the method is thread safe，The default scheduler instance is unique throughout the jvm lifecycle，
     * if the job does not specify a dedicated scheduler, the default scheduler will be used for job scheduling.
     * </p>
     *
     * @return Scheduler
     */
    public static Scheduler getDefaultScheduler() {

        if (defaultScheduler == null) {
            try {
                defaultLock.lock();
                if (defaultScheduler == null) {
                    defaultScheduler = schedulerFactory.getScheduler();
                }
                initListeners(defaultScheduler);
            } catch (Exception e) {
                log.error(Constant.LOG_PREFIX + " get DefaultScheduler fail:", e);
            } finally {
                defaultLock.unlock();
            }
        }
        return defaultScheduler;
    }

    private static final String[] BANNER = {
            "",
            "_________ _______  _______  _              _______           _______  _______ _________ _______",
            "\\__   __/(  ___  )(  ____ \\| \\    /\\      (  ___  )|\\     /|(  ___  )(  ____ )\\__   __// ___   )",
            "   ) (   | (   ) || (    \\/|  \\  / /      | (   ) || )   ( || (   ) || (    )|   ) (   \\/   )  |",
            "   | |   | (___) || (_____ |  (_/ /       | |   | || |   | || (___) || (____)|   | |       /   )",
            "   | |   |  ___  |(_____  )|   _ (        | |   | || |   | ||  ___  ||     __)   | |      /   /",
            "   | |   | (   ) |      ) ||  ( \\ \\       | | /\\| || |   | || (   ) || (\\ (      | |     /   /",
            "   | |   | )   ( |/\\____) ||  /  \\ \\      | (_\\ \\ || (___) || )   ( || ) \\ \\__   | |    /   (_/\\",
            "   )_(   |/     \\|\\_______)|_/    \\/      (____\\/_)(_______)|/     \\||/   \\__/   )_(   (_______/",
            ""
    };

    /**
     * <p>
     * Returns a handle to the Scheduler with the given name, if it exists (if it has already been instantiated).
     * </p>
     * <p>
     * <p>
     * The best solution is to dynamically specify some parameters that <code>StdSchedulerFactory</code> uses for initialization.
     * On the one hand, it can avoid the inconvenience caused by hard coding.
     * On the other hand, these initialization parameter values ​​can be dynamically adjusted to adapt to the change of Job.
     * </p>
     *
     * @param schedName
     * @return Scheduler
     */
    private Scheduler initScheduler(String schedName) throws SchedulerException {

        Scheduler scheduler = null;

        try {
            for (String line : BANNER) {
                System.out.println(line);
            }
            log.info(Constant.LOG_PREFIX + " init Scheduler -> {}", schedName);

            Properties properties = new Properties();
            MaxThreadCount = 1;
            int processors = Runtime.getRuntime().availableProcessors();
            int MaxBatchSize = MaxThreadCount < 2 * processors ? MaxThreadCount / 2 : processors;

            properties.put(QuartzInitConfiguration.MQUARTZ_INSTANCE_NAME, schedName);
            properties.put(QuartzInitConfiguration.MQUARTZ_THREAD_POOL_CLASS, "com.sia.mquartz.core.ext.TaskExecutorThreadPool");
            properties.put(QuartzInitConfiguration.MQUARTZ_THREAD_POOL_PREFIX + ".threadCount", String.valueOf(MaxThreadCount));
            properties.put(QuartzInitConfiguration.MQUARTZ_THREAD_POOL_PREFIX + ".threadPriority", String.valueOf(Thread.NORM_PRIORITY));
            properties.put(QuartzInitConfiguration.MQUARTZ_THREAD_POOL_PREFIX + ".threadNamePrefix", schedName + "");
            properties.put(QuartzInitConfiguration.MQUARTZ_MAX_BATCH_SIZE, String.valueOf(MaxBatchSize));
            properties.put(QuartzInitConfiguration.MQUARTZ_SCHEDULER_THREADS_INHERIT_CONTEXT_CLASS_LOADER_OF_INITIALIZING_THREAD, true);

            properties.put(QuartzInitConfiguration.MQUARTZ_JOB_STORE_CLASS, "com.sia.mquartz.core.simpl.RAMJobStore");
            properties.put(QuartzInitConfiguration.MQUARTZ_JOB_STORE_PREFIX + ".misfireThreshold", "1000");

            if (log.isDebugEnabled()) {
                properties.put(QuartzInitConfiguration.MQUARTZ_PLUGIN_PREFIX + ".tiggerHistory.class", LoggingJobHistoryPlugin.class.getName());
            }
            //properties.put(QuartzInitConfiguration.MQUARTZ_PLUGIN_PREFIX + ".MultiSerialPlanJob.class", MultiSerialJobListener.class.getName());

            schedulerFactory.initializeFromProp(properties);
            try {
                lock.lock();
                scheduler = schedulerFactory.getScheduler();
                System.setProperty(schedName, "false");
                initListeners(scheduler);
            } catch (Exception ex) {
                log.error(Constant.LOG_PREFIX + " init scheduler fail:", ex);
            } finally {
                lock.unlock();
            }
        } catch (Exception e) {
            log.error(Constant.LOG_PREFIX + " init scheduler fail:", e);
        }
        log.info(Constant.LOG_PREFIX + " init Scheduler ok -> {}", scheduler);
        scheduler.start();
        return scheduler;
    }

    /**
     * 初始化监听器
     *
     * @param scheduler
     * @throws SchedulerException
     */
    private static void initListeners(Scheduler scheduler) throws SchedulerException {

        ListenerManager listenerManager = scheduler.getListenerManager();

        //add Job Listener
        //CronJobListener cronJobListener = new CronJobListener();
        //SimpleJobListener simpleJobListener = new SimpleJobListener();
        //listenerManager.addJobListener(cronJobListener, cronJobListener.matcher());
        //listenerManager.addJobListener(simpleJobListener, simpleJobListener.matcher());
        listenerManager.addJobListener(new InnerJobListener());
        //add Trigger Listener
        listenerManager.addTriggerListener(new InnerTriggerListener());
        // add Scheduler Listener
        listenerManager.addSchedulerListener(new InnerSchedulerListener());
    }

    /**
     * Get the keys of all the <code>{@link JobDetail}s</code>
     * in the matching groups.
     *
     * @param matcher Matcher to evaluate against known groups
     * @return
     */
    public Set<JobKey> getAllJobKey(GroupMatcher<JobKey> matcher) {
        Set<JobKey> jobKeys = new LinkedHashSet<>();
        Collection<Scheduler> allSchedulers = getAllSchedulers();
        allSchedulers.forEach(scheduler -> {
            try {
                jobKeys.addAll(scheduler.getJobKeys(matcher));
            } catch (SchedulerException e) {
                log.error(Constant.LOG_PREFIX + " init get AllJobKey fail:", e);
            }
        });
        return jobKeys;
    }

    /**
     * Determine whether the scheduler has registered tasks
     *
     * @param schedulerName
     * @return If no <code>SiaJob</code> is registered on the specified scheduler, return true, otherwise return false
     * @throws SchedulerException
     */
    public static boolean isEmpty4Scheduler(String schedulerName) throws SchedulerException {
        boolean present = false;

        Scheduler scheduler4GroupName = getSchedulerIsExists(schedulerName);
        if (scheduler4GroupName != null) {
            present = scheduler4GroupName.getJobKeys(GroupMatcher.anyGroup()).isEmpty();
        }

        return present;
    }
}
