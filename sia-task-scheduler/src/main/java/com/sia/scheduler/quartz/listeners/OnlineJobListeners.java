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

package com.sia.scheduler.quartz.listeners;

import com.sia.scheduler.context.SpringContext;
import com.sia.scheduler.service.common.CommonService;
import com.sia.scheduler.util.constant.Constants;
import com.sia.scheduler.zk.monitor.LoadBalanceHelper;
import org.quartz.*;

/**
 *
 * OnlineJobListeners
 *
 * @see
 * @author maozhengwei
 * @date 2018-10-10 19:39
 * @version V1.0.0
 **/
public class OnlineJobListeners extends CommonService implements AbstractJobListeners {

    /**
     * 从JobDataMap中获取计数器的关键字，不同JobKey获取的计数器不同
     */
    private static final String NUM_EXECUTIONS = "NumExecutions";


    @Override
    public String getName() {
        return OnlineJobListeners.class.getSimpleName();
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        LOGGER.info("jobToBeExecuted " + context.getFireTime());
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        LOGGER.info("jobExecutionVetoed " + context.getFireTime());
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        LOGGER.info("jobWasExecuted " + context.getFireTime());
        String jobGroup = context.getTrigger().getJobKey().getGroup();
        String jobKey = context.getTrigger().getJobKey().getName();
        // 移除JOB 释放资源 针对非Cron Job
        Trigger trigger = context.getTrigger();
        if (trigger instanceof SimpleTrigger) {
            int repeatCount = ((SimpleTrigger) trigger).getRepeatCount();
            // 判断是否是Forever状态
            if (repeatCount > 0) {
                /**
                 * 从JobDataMap获取计数器，对Job执行次数计数
                 */
                JobDataMap map = context.getJobDetail().getJobDataMap();
                //计数器初始化为0
                int executeCount = 0;
                //如果计数器已存在，则获取
                if (map.containsKey(NUM_EXECUTIONS)) {
                    executeCount = map.getInt(NUM_EXECUTIONS);
                }
                //自增1，并写回
                executeCount++;
                map.put(NUM_EXECUTIONS, executeCount);

                // 如果执行次数已完成 则停止JOB
                if (executeCount >= repeatCount) {
                    // 删除ZK上的JobKey，进而删除quartz中的调度
                    stopJob(jobGroup, jobKey);
                }
            }
        }
        LOGGER.info(Constants.LOG_PREFIX + "JOB执行结束 ： jobGroupName is {},jobKey is {}", jobGroup, jobKey);
        /**
         * Job执行结束后，检查开关的状态，如果已关，表示本调度器要下线，则本调度器不再获取新的Job（这部分在JobMonitor中实现）。
         * <p>
         * 对于已获取的Job，待执行完后，释放Job。
         * <p>
         * 以下是释放逻辑
         */
        if (shouldIRelease()) {
            try {
                boolean checkExists = checkExists(jobGroup, jobKey);
                if (checkExists) {

                    if (removeJob(jobGroup, jobKey)) {
                        // 释放JOB，将JobKey下的临时节点删除，触发别的调度器抢占
                        sleep(1000);
                        SpringContext.getCurator4Scheduler().releaseJob(jobGroup, jobKey, Constants.LOCALHOST);
                        // 获取JOB数减1
                        LoadBalanceHelper.updateScheduler(-1);

                    }
                }
            } catch (SchedulerException e) {
                LOGGER.error(Constants.LOG_EX_PREFIX + "removeJob An exception occurs ", e);
            }
        }
    }


    /**
     * 每次Job执行完后，除了检查是否下线，还需检查超出阈值情况。如果超出阈值，自动释放，直到正常。
     *
     * @return
     */
    private boolean shouldIRelease() {
        //需要下线
        if (LoadBalanceHelper.isOffline()) {
            return true;
        }
        //超出阈值
        if (LoadBalanceHelper.getMyJobNum() > LoadBalanceHelper.getJobThreshold()) {
            return true;
        }
        return false;

    }

    private void sleep(long mills) {
        try {
            Thread.sleep(mills);
            LOGGER.info(Constants.LOG_PREFIX + " Release the Job delay for one second");
        } catch (InterruptedException e) {
            LOGGER.error(Constants.LOG_PREFIX + " Release the Job delay for one second, An exception occurs {}", e);
        }
    }
}
