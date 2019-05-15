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

package com.sia.scheduler.http.impl;

import com.sia.core.entity.JobMTask;
import com.sia.core.helper.StringHelper;
import com.sia.scheduler.http.enums.FailoverEnum;
import com.sia.scheduler.http.failover.strategy.FailoverMaximumCompensation;
import com.sia.scheduler.http.failover.strategy.FailoverRound;
import com.sia.scheduler.http.route.ExecutorRouteSharding;
import com.sia.scheduler.http.route.RouteStrategyHandler;
import com.sia.scheduler.log.annotations.LogAnnotation;
import com.sia.scheduler.service.common.CommonService;
import com.sia.scheduler.thread.execute.TaskCommit;
import com.sia.scheduler.util.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 * Different logical processing for different failure strategies,
 * you need to pay attention to using the methods in this class to enhance the aspect, log and persist to the database,
 * so do not easily modify the method name of the method in this class, so as to avoid the failure of the aspect function.
 *
 * The Throwable in the method is not used in this class, but it will be used in the aspect processing class. Please do not remove it.
 *
 * @see
 * @author maozhengwei
 * @date 2018-09-28 16:03
 * @version V1.0.0
 **/
@Component
public class HttpCallbackFail {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpCallbackFail.class);


    /**
     * Failure strategy - fragmentation processing logic
     * @param onlineTask
     * @param throwable
     * @return
     */
    @LogAnnotation
    public boolean sharding(JobMTask onlineTask, Throwable throwable) {
        //是否可以继续执行
        LOGGER.info(Constants.LOG_PREFIX + " Execution task failed >>> Failure strategy：sharding ->->->  task is {}, jobKey is {}", onlineTask.getTaskKey(), onlineTask.getJobKey());
        ExecutorRouteSharding.release(onlineTask,false);
        if (ExecutorRouteSharding.maxExecuteCount(onlineTask)>=0){
            return true;
        }

        TaskCommit.commit(onlineTask);

        return false;
    }

    /**
     * Failure strategy -- STOP
     * Stop JOB and log
     * Do not modify the method name easily
     *
     * @see FailoverEnum
     * @param onlineTask
     */
    @LogAnnotation
    public boolean onStop(JobMTask onlineTask,Throwable throwable) {
        LOGGER.info(Constants.LOG_PREFIX + " Execution task failed >>> Failure strategy：stop ->->-> Manual processing. task is {}, jobKey is {}", onlineTask.getTaskKey(), onlineTask.getJobKey());
        return true;
    }

    /**
     * Failure strategy -- Ignore
     * @see FailoverEnum
     * Do not modify the method name easily
     *
     * @param onlineTask
     * @return true：Indicates that it is necessary isCountDown
     */
    @LogAnnotation
    public boolean onIgnore(JobMTask onlineTask,Throwable throwable) {
        LOGGER.info(Constants.LOG_PREFIX + " Execution task failed >>> Failure strategy：Ignore ->->-> Ignore and continue running. task is {}, jobKey is {}", onlineTask.getTaskKey(), onlineTask.getJobKey());
        return false;
    }

    /**
     * Failure strategy -- transfer
     * Do not modify the method name easily
     *
     * @see FailoverRound
     * @param onlineTask
     * @return true：Indicates that it is necessary isCountDown
     */
    @LogAnnotation
    public boolean onTransfer(JobMTask onlineTask, Throwable throwable) {
        LOGGER.info(Constants.LOG_PREFIX + " Execution task failed >>> Failure strategy：TRANSFER ->->-> Transfer scheduling. task is {}, jobKey is {}", onlineTask.getTaskKey(), onlineTask.getJobKey());
        return handler(onlineTask);
    }

    /**
     * ailure strategy -- multiCallsAndTransfer
     * Do not modify the method name easily
     *
     * @see FailoverMaximumCompensation
     * @param onlineTask
     * @return true：Indicates that it is necessary isCountDown
     */
    @LogAnnotation
    public boolean onMultiCallsAndTransfer(JobMTask onlineTask, Throwable throwable) {
        LOGGER.info(Constants.LOG_PREFIX
                + " Execution task failed >>> Failure strategy： MULTI_CALLS_TRANSFER ->->-> try continue to call {}. : task is {}, jobKey is {}", onlineTask.getCurrentHandler(), onlineTask.getTaskKey(), onlineTask.getJobKey());
        return handler(onlineTask);
    }

    private boolean handler(JobMTask onlineTask){
        onlineTask.setOutParam(null);
        List<String> executorInstance = CommonService.getExecutorInstance(onlineTask);
        String routeInstance = RouteStrategyHandler.failHandle(onlineTask, executorInstance);
        if (StringHelper.isEmpty(routeInstance)) {
            return true;
        }
        onlineTask.setCurrentHandler(routeInstance);
        new TaskHttpClient().async(onlineTask);
        return false;
    }
}
