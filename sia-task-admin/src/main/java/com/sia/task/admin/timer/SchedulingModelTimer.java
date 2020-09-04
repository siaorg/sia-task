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

package com.sia.task.admin.timer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sia.task.admin.service.Index4managerService;
import com.sia.task.core.entity.BasicJob;
import com.sia.task.core.util.Constant;
import com.sia.task.mapper.BasicJobMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


/**
 * SchedulingModelTimer implements CommandLineRunner，Inherit the method<code>{@link CommandLineRunner#run(String...)}</code>，
 * And create a thread pool in the run method as the worker thread of the computing scheduling model。
 * <p>
 * In order to avoid the blocking problem caused by synchronization, asynchronous parallel computing is used here.
 * Although this solves the problem of calculation delay, it inevitably introduces a consistency problem from another perspective.
 * There may be short-term data inconsistencies when obtaining the calculation results, but this is tolerable.
 * For us, for a certain <code>{@link org.quartz.Scheduler}</code>, its calculations are synchronized, so there will be no data inconsistencies.
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/7/25 7:07 下午
 * @see
 **/
@Component
@Slf4j
public class SchedulingModelTimer implements CommandLineRunner {

    @Resource
    protected Index4managerService index4managerService;

    @Resource
    BasicJobMapper jobMapper;


    private void buildSchedulingModel() throws Exception {
        HashMap<String, List<String>> jobLoadInfo = index4managerService.jobLoadInfo();
        Set<String> schedulers = jobLoadInfo.keySet();
        log.info(Constant.LOG_PREFIX + " buildSchedulingModel - schedulers : [{}]", schedulers);
        for (String scheduler : schedulers) {
            List<String> jobKeys = jobLoadInfo.get(scheduler);
            List<String> crons = new ArrayList<>();
            if (jobKeys.size() > 0) {
                List<BasicJob> basicJobs = jobMapper.selectByJobKeyList(jobKeys);
                basicJobs.forEach(job -> {
                    if ("TRIGGER_TYPE_CRON".equals(job.getJobTrigerType())) {
                        crons.add(job.getJobTrigerValue().replaceAll("[\\s]{2,}"," ").trim());
                    }
                });
                log.info(Constant.LOG_PREFIX + " buildSchedulingModel - scheduler : [{}], cron - [{}]", scheduler, crons);
                SchedulingModel model = SchedulingModel.build(scheduler).computerTimers(crons);
                SchedulingModelStore.getModels().put(scheduler, model);
            }
        }
    }

    /**
     * Callback used to run the bean.
     * <p>
     * Start the computing thread of the scheduling model
     *
     * @param args incoming main method arguments
     */
    @Override
    public void run(String... args) {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("computerTimers" + "-pool-thread-%d").build();
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(
                1,
                threadFactory
        );
        executorService.scheduleAtFixedRate(() -> {
            //do computerTimers
            try {
                log.info(Constant.LOG_PREFIX + " SchedulingModelTimer computerTimers start...");
                buildSchedulingModel();
                log.info(Constant.LOG_PREFIX + " SchedulingModelTimer computerTimers completed");
            } catch (Exception e) {
                log.info(Constant.LOG_PREFIX + " An exception occurs  SchedulingModelTimer computerTimers...", e);
            }
        }, 60, 300, TimeUnit.SECONDS);
    }
}
