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

package com.sia.scheduler.service;


import com.sia.core.curator.Curator4Scheduler;
import com.sia.core.entity.BasicJob;
import com.sia.core.entity.JobMTask;
import com.sia.core.helper.StringHelper;
import com.sia.core.mapper.BasicJobMapper;
import com.sia.core.status.JobStatus;
import com.sia.scheduler.context.SpringContext;
import com.sia.scheduler.quartz.impl.OnlineJob;
import com.sia.scheduler.quartz.impl.OnlineScheduler;
import com.sia.scheduler.service.common.CommonService;
import com.sia.scheduler.thread.execute.TaskCommit;
import com.sia.scheduler.util.constant.Constants;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BasicJobService
 *
 * @see
 * @author maozhengwei
 * @date 2018-04-18 10:22
 * @version V1.0.0
 **/
@Service
public class BasicJobService {
    public static final Logger LOGGER = LoggerFactory.getLogger(BasicJobService.class);

    @Autowired
    private BasicJobMapper basicJobMapper;

    @Autowired
    private Curator4Scheduler curator4Scheduler;

    @Autowired
    private JobLogService jobLogService;


    /**
     * 获取Job
     * @param jobGroup
     * @param jobKey
     * @return
     */
    @Cacheable(value = "basicJob", key = "#jobKey + #jobGroup")
    public BasicJob getJob(String jobGroup, String jobKey) {
        LOGGER.info(Constants.LOG_PREFIX + " load BasicJob data from database, jobKey={}", jobKey);
        Map<String, String> param = new HashMap<>(2);
        param.put("jobKey", jobKey);
        param.put("jobGroup", jobGroup);
        BasicJob basicJob = basicJobMapper.selectByJobKeyAndJobGroup(param);
        basicJob.setJobChild(basicJobMapper.selectChilds(jobKey));
        return basicJob;
    }

    /**
     * 清除缓存
     * @param jobGroup
     * @param jobKey
     */
    @CacheEvict(value = "basicJob", key = "#jobKey + #jobGroup")
    public void cleanJobCache(String jobGroup, String jobKey) {
        LOGGER.info(Constants.LOG_PREFIX + " cleanJobCache : clean data from Cache, jobKey={}", jobKey);
    }

    /**
     * run Once
     * 任务单次执行
     * @param jobGroupName
     * @param jobKey
     * @return
     * @throws SchedulerException
     */
    public boolean runOnceJobKey(String jobGroupName, String jobKey) throws SchedulerException {

        if (!StringHelper.isEmpty(jobGroupName) || !StringHelper.isEmpty(jobKey)) {
            try {
                boolean flag = curator4Scheduler.casJobStatus4Scheduler(jobGroupName, jobKey, Constants.LOCALHOST,
                        JobStatus.READY.toString(), JobStatus.RUNNING.toString());
                if (flag) {
                    List<JobMTask> onlineTaskList = new CommonService().analyticalJob(jobGroupName, jobKey);
                    if (onlineTaskList == null) {
                        LOGGER.info(Constants.LOG_PREFIX
                                        + " 运行一次 : Current job is not running; jobGroupName is {} , jobKey is {}, because Job owner tasks is null",
                                jobGroupName, jobKey);
                        return false;
                    }
                    LOGGER.info(Constants.LOG_PREFIX + " 运行一次 : 任务执行开始， 当前任务为：{}", jobGroupName);

                    try {
                        // 插入JOB-TASK-LOG
                        jobLogService.insertJobLogAndTaskLog(jobGroupName, jobKey, onlineTaskList);
                    } catch (Exception e) {
                        LOGGER.error(Constants.LOG_PREFIX + "运行一次操作 日志初始化出现异常", e);
                    }

                    CountDownLatch countDownLatch = new CountDownLatch(1);
                    ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();
                    for (JobMTask task : onlineTaskList) {
                        task.setCountDownLatch(countDownLatch);
                        TaskCommit.commit(task);
                    }
                    try {
                        countDownLatch.await();
                        // 资源回收
                        CommonService.shutdownExecutorService(threadPoolExecutor);
                        LOGGER.info(Constants.LOG_PREFIX + "运行一次操作 JOB执行结束 ： jobGroupName is {},jobKey is {}",
                                jobGroupName, jobKey);
                    } catch (InterruptedException e) {
                        LOGGER.error(Constants.LOG_PREFIX + "运行一次操作 JOB执行完成，资源回收出现异常 ：", e);
                    }
                    return true;
                }
                LOGGER.error(Constants.LOG_PREFIX + " 运行一次操作执行失败 : jobGroupName is {}, jobKey is {}",
                        jobGroupName, jobKey);
            } catch (Exception e) {
                LOGGER.error(Constants.LOG_PREFIX + "运行一次操作执行异常", e);
            }
        }
        return false;
    }

}
