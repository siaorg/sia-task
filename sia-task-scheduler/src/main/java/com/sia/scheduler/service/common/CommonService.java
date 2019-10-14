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

package com.sia.scheduler.service.common;

import com.sia.core.curator.Curator4Scheduler;
import com.sia.core.dag.DAGHelper;
import com.sia.core.entity.BasicJob;
import com.sia.core.entity.JobMTask;
import com.sia.core.helper.JSONHelper;
import com.sia.core.helper.ListHelper;
import com.sia.core.helper.StringHelper;
import com.sia.core.status.JobStatus;
import com.sia.scheduler.context.SpringContext;
import com.sia.scheduler.http.route.ExecutorRouteSharding;
import com.sia.scheduler.quartz.impl.OnlineJob;
import com.sia.scheduler.quartz.impl.OnlineSchedulerFactory;
import com.sia.scheduler.service.BasicJobService;
import com.sia.scheduler.util.constant.Constants;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CommonService
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2018-09-27 11:27
 * @see
 **/
public class CommonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonService.class);

    /**
     * get an executor instance
     */
    public static List<String> getExecutorInstance(JobMTask onlineTask) {
        List<String> executorsFromDB = null;
        List<String> executorsFromZk = SpringContext.getCurator4Scheduler().getExecutors(onlineTask.getTaskKey());
        if (!StringHelper.isEmpty(onlineTask.getIpAndPortList())) {
            executorsFromDB = Arrays.asList(onlineTask.getIpAndPortList().split(Constants.REGEX_COMMA));
        }
        return ListHelper.mergeList(executorsFromZk, executorsFromDB);
    }


    /**
     * load task information
     *
     * @param jobGroupName
     * @param jobKey
     * @return list 起始任务
     * @throws Exception
     */
    public List<JobMTask> analyticalJob(String jobGroupName, String jobKey) {
        List<JobMTask> jobMTasks = SpringContext.getJobMTaskService().selectTaskMJobAndIPListByJobGroupAndKey(jobGroupName, jobKey);
        List<JobMTask> jobMTasksBk = new ArrayList<>();
        jobMTasks.forEach(jobMTask -> {
            JobMTask jobMTaskClone = jobMTask.deepClone();
            jobMTasksBk.add(jobMTaskClone);
        });

        return analyticalTask(jobMTasksBk);
    }

    /**
     * 任务编排
     *
     * @param jobMTaskList
     * @return
     */
    private static List<JobMTask> analyticalTask(List<JobMTask> jobMTaskList) {
        Map<String, JobMTask> onlineTasksMap = new HashMap<>(jobMTaskList.size());
        List<JobMTask> startTaskLists = new ArrayList<>();
        for (JobMTask onlineTask : jobMTaskList) {
            onlineTasksMap.put(onlineTask.getTaskKey(), onlineTask);
        }
        //任务编排
        for (JobMTask currentTask : jobMTaskList) {
            List<String> preTaskKeyList = StringHelper.isEmpty(currentTask.getPreTaskKey()) ? Collections.emptyList() : Arrays.asList(currentTask.getPreTaskKey().split(","));
            if (preTaskKeyList.size() == 0) {
                startTaskLists.add(currentTask);
                continue;
            }
            for (String preTaskKey : preTaskKeyList) {
                JobMTask onlineTask = onlineTasksMap.get(preTaskKey);
                currentTask.getPreTask().add(onlineTask);
                onlineTask.getPostTask().add(onlineTasksMap.get(currentTask.getTaskKey()));
            }
        }
        //设置虚拟末节点
        JobMTask endTask = new JobMTask();
        endTask.setTaskKey(Constants.ENDTASK);
        endTask.setPreTaskCounter(new AtomicInteger(0));
        endTask.setJobKey(jobMTaskList.get(0).getJobKey());
        endTask.setJobGroup(jobMTaskList.get(0).getJobGroup());
        List<JobMTask> preTask = new ArrayList<JobMTask>();
        endTask.setPreTask(preTask);
        for (JobMTask currentTask : jobMTaskList) {
            if (currentTask.getPostTask().size() == 0) {
                currentTask.getPostTask().add(endTask);
                endTask.getPreTask().add(currentTask);
            }
        }
        //任务校验
        jobMTaskList.add(endTask);
        List<String> check = doDAGCheck(jobMTaskList);
        if (check != null) {
            LOGGER.error("Job 的task存在环路 不执行 {}", check);
            return null;
        }
        //获取起始任务
        return startTaskLists;
    }

    /**
     * Check if there is a loop in the Task referenced by the job.
     *
     * @param jobMTaskList
     * @return
     */
    public static List<String> doDAGCheck(List<JobMTask> jobMTaskList) {
        Map<String, List<String>> relyMap = new HashMap<>(4);
        for (JobMTask jobMTask : jobMTaskList) {
            List<JobMTask> postTask = jobMTask.getPostTask();
            List<String> tmp = new ArrayList<>();
            for (JobMTask jobMTask1 : postTask) {
                tmp.add(jobMTask1.getTaskKey());
            }
            relyMap.put(jobMTask.getTaskKey(), tmp == null ? Collections.emptyList() : tmp);
        }
        return DAGHelper.findACycle(relyMap);
    }


    /**
     * CountDown 进行线程通信，如果是Exception 则进行状态变化为异常停止，如果正常运行完成 则直接进行CountDown
     *
     * @param onlineTask
     * @param isException
     */
    public void isExceptionCountDown(JobMTask onlineTask, boolean isException, String message) {
        String jobGroup = onlineTask.getJobGroup();
        String jobKey = onlineTask.getJobKey();
        try {
            if (isException) {

                boolean casJobStatus4Scheduler = SpringContext.getCurator4Scheduler().casJobStatus4Scheduler(jobGroup, jobKey,
                        Constants.LOCALHOST, JobStatus.RUNNING.toString(), JobStatus.STOP.toString());
                if (!casJobStatus4Scheduler) {
                    LOGGER.error(Constants.LOG_PREFIX + "task 运行异常，停止 JOB {} : jobGroup is {}, jobKey is {}",
                            casJobStatus4Scheduler, jobGroup, jobKey);
                }
                //预警
                message = onlineTask.toString() + Constants.REGEX_ARROW + " Exception : " + message;
                SpringContext.getEmailService().sendEmail(onlineTask.getJobAlarmEmail(), message, "JOB 运行异常");
                return;
            }
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + "isExceptionCountDown", e);
        } finally {
            CountDownLatch countDownLatch = onlineTask.getCountDownLatch();
            countDownLatch.countDown();
        }
    }

    /**
     * TIMEOUT the maximum time to wait
     */
    private static final long TIMEOUT = 60;

    /**
     * Turn off thread pool resources，function for performing once
     * critical
     *
     * @param pool
     */
    public static void shutdownExecutorService(ExecutorService pool) {

        if (pool == null) {
            return;
        }
        // 释放不用的线程池
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(TIMEOUT, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks

            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();

        }
    }

    /**
     * checkExists
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     * @throws SchedulerException
     */
    public boolean checkExists(String jobGroupName, String jobKey) throws SchedulerException {
        return OnlineSchedulerFactory.getOnlineScheduler().checkExists(jobKey, jobGroupName);
    }

    /**
     * checkExists
     * 检查任务是否存在
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     * @throws SchedulerException
     */
    public boolean checkJobExists(String jobGroupName, String jobKey) throws SchedulerException {
        //增加判断逻辑
        BasicJob job = SpringContext.getRunningJob().get(jobKey);
        if (job != null && Constants.LOCALHOST.equals(job.getTriggerInstance())) {
            return true;
        }
        return OnlineSchedulerFactory.getOnlineScheduler().checkExists(jobKey, jobGroupName);
    }

    /**
     * pauseJob Deprecated
     * 任务暂停
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     * @throws SchedulerException
     */
    @Deprecated
    public boolean pauseJob(String jobGroupName, String jobKey) throws SchedulerException {

        boolean result = false;
        if (StringHelper.isEmpty(jobGroupName) || StringHelper.isEmpty(jobKey)) {
            return result;
        }
        result = OnlineSchedulerFactory.getOnlineScheduler().pauseJob(jobGroupName, jobKey);
        return result;
    }

    /**
     * resumeJob Deprecated
     * 任务恢复
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     * @throws SchedulerException
     */
    @Deprecated
    public boolean resumeJob(String jobGroupName, String jobKey) throws SchedulerException {

        boolean result = false;
        if (StringHelper.isEmpty(jobGroupName) || StringHelper.isEmpty(jobKey)) {
            return result;
        }
        result = OnlineSchedulerFactory.getOnlineScheduler().resumeJob(jobGroupName, jobKey);
        return result;
    }

    /**
     * remove Job
     * 移除任务
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     * @throws SchedulerException
     */
    public boolean removeJob(String jobGroupName, String jobKey) throws SchedulerException {

        //释放分片缓存数据
        ExecutorRouteSharding.clean(jobKey);

        SpringContext.getRunningJob().remove(jobKey);
        boolean result = false;
        if (StringHelper.isEmpty(jobGroupName) || StringHelper.isEmpty(jobKey)) {
            return result;
        }
        result = OnlineSchedulerFactory.getOnlineScheduler().removeJob(jobGroupName, jobKey);
        return result;
    }


    /**
     * 执行Job，只能通过事件触发调用
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     * @throws Exception
     */
    public boolean runJob(String jobGroupName, String jobKey) {

        if (StringHelper.isEmpty(jobGroupName) || StringHelper.isEmpty(jobKey)) {
            LOGGER.error(
                    Constants.LOG_PREFIX
                            + " runJob jobGroupName or jobKey is null, jobGroupName is{}, jobKey is {}",
                    jobGroupName, jobKey);
        }
        BasicJobService jobService = SpringContext.getBasicJobService();
        jobService.cleanJobCache(jobGroupName, jobKey);
        BasicJob basicJob = jobService.getJob(jobGroupName, jobKey);
        if (basicJob == null) {
            LOGGER.error(Constants.LOG_PREFIX + " 根据 {} And {} 从DB获取JOB信息为空, 请查看是否存在该JOB", jobGroupName, jobKey);
            throw new NullPointerException(
                    "SELECT JOB FROM DB WHERE  jobGroupName IS " + jobGroupName + " AND  jobKey is " + jobKey);
        }
        try {
            SpringContext.getRunningJob().put(jobKey, basicJob);
            OnlineSchedulerFactory.getOnlineScheduler().loadJob(basicJob.getJobKey(), basicJob.getJobGroup(), basicJob.getJobTrigerType(), basicJob.getJobTrigerValue(), OnlineJob.class);
            return true;
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + "loadJob Exception ", e);
        }

        return false;
    }


    /**
     * stop Job
     * 停止Job
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    public String stopJob(String jobGroupName, String jobKey) {

        if (StringHelper.isEmpty(jobGroupName) || StringHelper.isEmpty(jobKey)) {
            LOGGER.info(
                    Constants.LOG_PREFIX
                            + " stop JOB IS FAIL : jobGroupName or jobKey , jobGroupName is{}, jobKey is {}",
                    jobGroupName, jobKey);
            return JSONHelper.toString(Constants.FAIL);
        }
        boolean jobStatus4User = false;
        try {
            /**
             * 从ZK上删除jobKey，会触发移除JOB的执行逻辑
             */
            jobStatus4User = SpringContext.getCurator4Scheduler().deleteJobKey(jobGroupName, jobKey);
            if (!jobStatus4User) {
                LOGGER.info(Constants.LOG_PREFIX + " REMOVE JOB FROM FAIL. jobGroupName is {} jobKey is {}",
                        jobGroupName, jobKey);
                return JSONHelper.toString(Constants.FAIL);
            }

            /**
             * TODO：移除JOB的执行逻辑，补偿操作（不影响正确性）
             */

        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " STOP JOB Exception : ", e);
        }
        LOGGER.info(Constants.LOG_PREFIX + " UPDATE JOB STATUS IS {},jobGroupName is{}, jobKey is {}",
                jobStatus4User, jobKey, jobGroupName);
        return JSONHelper.toString(jobStatus4User ? Constants.SUCCESS : Constants.FAIL);
    }


    /**
     * 启动后置子任务
     *
     * @param basicJob
     * @return
     */
    public boolean shouldStartPostTask(BasicJob basicJob) {
        BasicJob childJob = basicJob.getJobChild();
        boolean startStatus = false;
        if (childJob != null) {
            Curator4Scheduler curator4Scheduler=SpringContext.getCurator4Scheduler();
            if (!JobStatus.STOP.toString().equals(curator4Scheduler.getJobStatus(childJob.getJobGroup(), childJob.getJobKey()))) {
                boolean flag = curator4Scheduler.createJobKey(childJob.getJobGroup(), childJob.getJobKey());
                if (flag) {
                    startStatus = curator4Scheduler.casJobStatus4User(childJob.getJobGroup(), childJob.getJobKey(), JobStatus.STOP.toString(), JobStatus.READY.toString());
                }
            }
        }
        return startStatus;
    }

    /**
     * 后置任务理应运行完本次自动结束
     *
     * @param basicJob
     * @return
     */
    public boolean shouldStopPostTask(BasicJob basicJob) {
        if (!StringHelper.isEmpty(basicJob.getJobParentKey())) {
            return SpringContext.getCurator4Scheduler().deleteJobKey(basicJob.getJobGroup(), basicJob.getJobKey());
        }
        return false;
    }
}
