/*-
 * <<
 * task
 * ==
 * Copyright (C) 2019 sia
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

package com.sia.scheduler.quartz.impl;

import com.sia.scheduler.quartz.listeners.OnlineJobListeners;
import com.sia.scheduler.quartz.listeners.OnlineTriggerListener;
import com.sia.scheduler.util.constant.Constants;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OnlineSchedulerFactory
 *
 * This is a factory class used to generate a Scheduler instance, providing a static method {@Link getSchedulerInstance()}.
 *
 * where another static method generates an OnlineScheduler instance.
 * In principle, this method does not need to be synchronized, and there is no security problem with multithreading.
 *
 * @description
 * @see
 * @author maozhengwei
 * @date 2018-06-28 17:59
 * @version V1.0.0
 **/
public class OnlineSchedulerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnlineSchedulerFactory.class);

    private static Scheduler scheduler;

    private static OnlineScheduler onlineScheduler;

    public OnlineSchedulerFactory() {
        super();
    }


    public static Scheduler getSchedulerInstance() {

        if (scheduler == null) {
            try {
                synchronized (OnlineSchedulerFactory.class) {
                    if (scheduler == null) {
                        LOGGER.info(Constants.LOG_PREFIX + "****************************** init OnlineScheduler  ******************************");
                        scheduler = new StdSchedulerFactory().getScheduler();
                        scheduler.getListenerManager().addJobListener(new OnlineJobListeners());
                        scheduler.getListenerManager().addTriggerListener(new OnlineTriggerListener());
                    }
                }
            } catch (SchedulerException e) {
                LOGGER.error(Constants.LOG_PREFIX + "init scheduler fail:", e);
            }
        }
        return scheduler;
    }


    public static OnlineScheduler getOnlineScheduler() {

        if (onlineScheduler == null) {
            try {
                synchronized (OnlineSchedulerFactory.class) {
                    onlineScheduler = new OnlineScheduler();
                }
            } catch (Exception e) {
                LOGGER.error(Constants.LOG_PREFIX + "init OnlineScheduler fail:", e);
            }
        }
        return onlineScheduler;
    }
}
