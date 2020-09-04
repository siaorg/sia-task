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

package com.sia.task.quartz.exmple.main;

import com.sia.task.quartz.core.Scheduler;
import com.sia.task.quartz.core.SchedulerFactory;
import com.sia.task.quartz.core.SimpleScheduleBuilder;
import com.sia.task.quartz.core.StdSchedulerFactory;
import com.sia.task.quartz.exception.SchedulerException;
import com.sia.task.quartz.job.JobBuilder;
import com.sia.task.quartz.job.JobDetail;
import com.sia.task.quartz.job.trigger.Trigger;
import com.sia.task.quartz.job.trigger.TriggerBuilder;

import java.util.Date;

/**
 *
 * Get the amount of time (in ms) to wait when accessing this jobStore  repeatedly fails.
 * @description
 * @see
 * @author maozhengwei
 * @data 2019-05-16 14:15
 * @version V1.0.0
 **/
public class QuartzScheduler {

    /**
     * 定义 全局 Scheduler 方便以下方法使用
     */
    private static Scheduler scheduler;

    /**Scheduler
     * 初始化
     */
    static {
        try {
            scheduler = getSechduler();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取Scheduler instance
     * 实际生产中不建议这么做：最好采用资源隔离的方式；避免任务过多进行资源抢占导致任务misfire
     * @return
     * @throws SchedulerException
     */
    public static Scheduler getSechduler() throws SchedulerException {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        if (scheduler == null) {
            synchronized (QuartzScheduler.class) {
                if (scheduler == null) {
                    scheduler = schedulerFactory.getScheduler();
                }
            }
        }
        return scheduler;
    }

    /**
     * add Job
     * 这里可以作为通用型，可以删除定制的job以及不同Job不同的启动参数
     * @param jobGroup
     * @param jobName
     * @param jobClass
     * @throws SchedulerException
     */
    public void addJob(String jobGroup, String jobName, Class jobClass) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroup).build();

        // 创建触发器
        // withIntervalInSeconds(2)表示每隔2s执行任务
        Date triggerDate = new Date();
        SimpleScheduleBuilder schedBuilder = SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInSeconds(5)
                .repeatForever();
        TriggerBuilder<Trigger> triggerBuilder  = TriggerBuilder
                .newTrigger()
                .withIdentity(jobName, jobGroup);
        Trigger trigger = triggerBuilder
                .startAt(triggerDate)
                .withSchedule(schedBuilder)
                .build();
        // 将job trigger放入scheduler
        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * start scheduler
     * @throws SchedulerException
     */
    public void start() throws SchedulerException {
        if (scheduler.isStarted()){
            return;
        }
        scheduler.start();
    }

    public void stop() throws SchedulerException {
        if (!scheduler.isStarted()){
            return;
        }
        scheduler.shutdown();
    }
}
