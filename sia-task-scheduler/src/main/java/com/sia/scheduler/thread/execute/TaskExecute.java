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

package com.sia.scheduler.thread.execute;

import com.sia.core.curator.Curator4Scheduler;
import com.sia.core.entity.JobMTask;
import com.sia.core.status.JobStatus;
import com.sia.scheduler.context.SpringContext;
import com.sia.scheduler.log.enums.JobLogEnum;
import com.sia.scheduler.log.enums.TaskLogEnum;
import com.sia.scheduler.http.impl.TaskHttpClient;
import com.sia.scheduler.http.route.ExecutorRouteSharding;
import com.sia.scheduler.http.route.RouteStrategyEnum;
import com.sia.scheduler.http.route.RouteStrategyHandler;
import com.sia.scheduler.service.common.CommonService;
import com.sia.scheduler.thread.Execute;
import com.sia.scheduler.util.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

/**
 *
 * TaskExecute
 * @see
 * @author maozhengwei
 * @date 2018-09-28 20:10
 * @version V1.0.0
 **/
public class TaskExecute extends CommonService implements Execute<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecute.class);

    private JobMTask onlineTask;

    public TaskExecute(JobMTask onlineTask) {
        this.onlineTask = onlineTask;
    }

    @Override
    public Boolean run() {
        return run(onlineTask);
    }

    private Boolean run(JobMTask onlineTask) {
        if (Constants.ENDTASK.equals(onlineTask.getTaskKey())) {
            return runEndTask(onlineTask);
        }
        return runTask(onlineTask);
    }

    /**
     * 业务task
     *
     * @param onlineTask
     * @return
     */
    private Boolean runTask(JobMTask onlineTask) {
        String jobGroup = onlineTask.getJobGroup();
        String jobKey = onlineTask.getJobKey();
        String taskKey = onlineTask.getTaskKey();
        try {
            Curator4Scheduler curator4Scheduler= SpringContext.getCurator4Scheduler();
            String jobStatus = curator4Scheduler.getJobStatus(jobGroup, jobKey);
            List<String> jobScheduler = curator4Scheduler.getJobScheduler(jobGroup, jobKey);
            // 验证合法性
            if (jobStatus.equals(JobStatus.RUNNING.toString()) && jobScheduler.contains(Constants.LOCALHOST)) {

                runAsync(onlineTask);

                return Boolean.TRUE;
            } else {
                // 停止 TODO 此处逻辑是人为触发 不进行预警
                LOGGER.info(
                        Constants.LOG_PREFIX
                                + "The original status of the task has changed and the status change operation cannot be performed. jobGroup is {},jobKey is {},schedulerIPAndPort is {},job status is {}",
                        jobGroup, jobKey, Constants.LOCALHOST, jobStatus);
                isExceptionCountDown(onlineTask, false, null);
                return Boolean.FALSE;
            }
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_EX_PREFIX + "Task 执行进行提交线程失败 : jobGroup is {},jobKey is {} taskKey is {}", jobGroup, jobKey, taskKey);
            LOGGER.error(Constants.LOG_EX_PREFIX + "Task 执行异常 :", e);
            isExceptionCountDown(onlineTask, true, "Task 执行进行提交线程失败" + e.getMessage());
        }
        return Boolean.FALSE;
    }

    /**
     * 判断是否是末节点任务以及前置任务是否都已完成
     *
     * @param onlineTask
     * @return
     */
    private Boolean runEndTask(JobMTask onlineTask) {
        String jobGroup = onlineTask.getJobGroup();
        String jobKey = onlineTask.getJobKey();
        try {
            if (onlineTask.getPreTaskCounter().get() < onlineTask.getPreTask().size()) {
                return Boolean.FALSE;
            }
            Curator4Scheduler curator4Scheduler= SpringContext.getCurator4Scheduler();
            String jobStatus = curator4Scheduler.getJobStatus(jobGroup, jobKey);
            if (jobStatus.equals(JobStatus.RUNNING.toString())) {
                // 修改状态值(本轮结束) : RUNNING >>> READY
                boolean casJobStatus = curator4Scheduler.casJobStatus4Scheduler(jobGroup, jobKey, Constants.LOCALHOST, jobStatus, JobStatus.READY.toString());
                if (casJobStatus) {
                    LOGGER.info(Constants.LOG_PREFIX
                                    + "The execution of this task is completed and the status is successfully modified.jobKey ：{},schedulerIPAndPort is {},old status is {},expected status is {}",
                            jobKey, Constants.LOCALHOST, jobStatus, JobStatus.READY.toString());
                    return Boolean.TRUE;
                }
            }
            // TODO 是否预警
            LOGGER.info(Constants.LOG_PREFIX
                            + "The execution of this task is completed, and the modification status fails. : jobKey {},schedulerIPAndPort  {},old status is {},expected status is {}",
                    jobKey, Constants.LOCALHOST, jobStatus, JobStatus.READY.toString());
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_EX_PREFIX + "running endTask Exception :", e);
        } finally {
            try {
                // 日志
                SpringContext.getTaskLogService().recordTaskLog(onlineTask, TaskLogEnum.LOG_ENDTASK_FINISHED.toString(), null);
                SpringContext.getJobLogService().updateJobLog(onlineTask, JobLogEnum.LOG_ENDTASK_FINISHED.toString());
            } catch (Exception e) {
                LOGGER.error(Constants.LOG_EX_PREFIX + "Log insertion exception", e);
            }
            isExceptionCountDown(onlineTask, false, null);
        }
        return Boolean.FALSE;
    }

    private void runAsync(JobMTask onlineTask) {
        // 获取执行器实例
        List<String> addressList = getExecutorInstance(onlineTask);
        //分片逻辑
        if (RouteStrategyEnum.ROUTE_TYPE_SHARDING.getRouteType().equals(onlineTask.getRouteStrategy())) {

            PriorityBlockingQueue<String> instances = ExecutorRouteSharding.getInstance(onlineTask, addressList);
            instances.forEach(inst -> {
                if (instances.remove(inst)) {
                    String sharding = ExecutorRouteSharding.getSharding(onlineTask);
                    if (null != sharding) {
                        JobMTask clone = onlineTask.deepClone();
                        clone.setCurrentHandler(inst);
                        clone.setInputValue(sharding);
                        new TaskHttpClient().async(clone);
                    }
                }
            });

        } else {
            //非分片
            if (addressList != null && addressList.size() > 0) {
                String ipPort = String.join(Constants.REGEX_COMMA, addressList);
                onlineTask.setIpAndPortList(ipPort);
            }

            String instance = RouteStrategyHandler.handle(onlineTask, addressList);
            onlineTask.setCurrentHandler(instance);
            new TaskHttpClient().async(onlineTask);
        }
    }

}
