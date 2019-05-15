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
import com.sia.core.helper.JSONHelper;
import com.sia.scheduler.http.enums.FailoverEnum;
import com.sia.scheduler.log.enums.TaskLogEnum;
import com.sia.scheduler.http.enums.AsyncResponse;
import com.sia.scheduler.http.enums.ResponseStatus;
import com.sia.scheduler.http.route.ExecutorRouteSharding;
import com.sia.scheduler.http.route.RouteStrategyEnum;
import com.sia.scheduler.http.route.RouteStrategyHandler;
import com.sia.scheduler.thread.execute.TaskCommit;
import com.sia.scheduler.util.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 *
 * Asynchronous callback implementation class that actually handles asynchronous callback logic.
 *
 * @see
 * @author maozhengwei
 * @date 2018-09-28 11:24
 * @version V1.0.0
 **/
public class OnlineListenableFutureCallback<T extends ResponseEntity<String>> extends AbstractFutureCallback<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnlineListenableFutureCallback.class);

    private JobMTask onlineTask;

    public OnlineListenableFutureCallback(JobMTask onlineTask) {
        this.onlineTask = onlineTask;
    }

    @Override
    protected void vFailure(Throwable ex) {
        boolean isCountDown = onFailure(onlineTask, ex);
        if (isCountDown) {
            isExceptionCountDown(onlineTask, true, " onFailure is Exception : " + ex.getMessage());
        }
    }

    @Override
    protected void vSuccess(T result) {
        String resultBody = result.getBody();
        onSuccess(onlineTask, resultBody);
    }

    private void onSuccess(JobMTask onlineTask, String resultBody) {

        String responseString = null;
        Map<String, String> responseTarget;
        AsyncResponse asyncResponse = null;
        try {
            // 1解析返回的数据
            responseTarget = JSONHelper.toObject(resultBody, Map.class);
            if (responseTarget == null) {
                asyncResponse = JSONHelper.toObject(resultBody, AsyncResponse.class);
                responseTarget = asyncResponse.getTarget();
            }
            responseString = JSONHelper.toString(responseTarget);
            onlineTask.setOutParam(responseString);

        } catch (Exception e) {
            taskLogService.recordTaskLog(onlineTask, TaskLogEnum.LOG_TASK_CALLBACKERROR.toString(), responseString);
            LOGGER.error(Constants.LOG_PREFIX + " Parsing the returned object with an exception. responseEntityBody Exception : ", e);
            isExceptionCountDown(onlineTask, true, " Parsing the returned object with an exception. responseEntityBody Exception : " + e.getMessage());
            return;
        }

        //2 TODO 记录日志 需要优化
        if (asyncResponse != null && asyncResponse.getStatus().equals(ResponseStatus.SUCCESS)) {
            //复位Task
            reset(onlineTask);
            LOGGER.info(Constants.LOG_PREFIX + " Task is {} execution completed, the result is ：{}", onlineTask.getTaskKey(), responseString);
            taskLogService.recordTaskLog(onlineTask, TaskLogEnum.LOG_TASK_FINISHED.toString(), responseString);
            commitSuccessTask(onlineTask);
        } else if (responseTarget != null && responseTarget.get(AsyncResponse.STATUS).toUpperCase().equals(ResponseStatus.SUCCESS.toString())) {
            //复位Task
            reset(onlineTask);
            responseTarget.get(responseTarget.get("result"));
            LOGGER.info(Constants.LOG_PREFIX + " Task is {} execution completed, the result is ：{}", onlineTask.getTaskKey(), responseString);
            taskLogService.recordTaskLog(onlineTask, TaskLogEnum.LOG_TASK_FINISHED.toString(), responseString);
            commitSuccessTask(onlineTask);
        } else {
            try {
                boolean isCountDown = onFailure(onlineTask, null);
                if (isCountDown) {
                    isExceptionCountDown(onlineTask, true, " Task execution completed, the result is ：" + responseString);
                }
                LOGGER.info(Constants.LOG_PREFIX + " Task execution completed, Return abnormal result : {}", responseString);
            } catch (Exception e) {
                LOGGER.error(" Task Task execution completed, an exception occurred during the process of processing the failed result : ", e);
                isExceptionCountDown(onlineTask, true, " Task Task execution completed, an exception occurred during the process of processing the failed resul : " + e.getMessage());
            }
        }
    }


    private boolean onFailure(JobMTask onlineTask, Throwable throwable) {
        boolean isCountDown;
        switch (FailoverEnum.getByValue(onlineTask.getFailover().toLowerCase())) {
            case IGNORE:
                isCountDown = httpCallbackLog.onIgnore(onlineTask, throwable);
                commitSuccessTask(onlineTask);
                break;
            case TRANSFER:
                isCountDown = httpCallbackLog.onTransfer(onlineTask, throwable);
                break;
            case MULTI_CALLS_TRANSFER:
                isCountDown = httpCallbackLog.onMultiCallsAndTransfer(onlineTask, throwable);
                break;
            case STOP:
                isCountDown = httpCallbackLog.onStop(onlineTask, throwable);
                break;
            default:
                isCountDown = httpCallbackLog.sharding(onlineTask, throwable);
        }
        return isCountDown;
    }

    private void reset(JobMTask onlineTask) {
        RouteStrategyHandler.clear(onlineTask);
    }

    /**
     * 提交任务
     *
     * @param onlineTask
     */
    private void commitSuccessTask(JobMTask onlineTask) {

        if (RouteStrategyEnum.ROUTE_TYPE_SHARDING.getRouteType().equals(onlineTask.getRouteStrategy())){
            if (ExecutorRouteSharding.finishedShardingCount(onlineTask) > 0){
                //进行执行器释放
                LOGGER.info(Constants.LOG_PREFIX + "sharding tasks are not all completed, Continue to process the sharding logic {}", onlineTask.toString());
                ExecutorRouteSharding.release(onlineTask,true);
                onlineTask.setOutParam(null);
                TaskCommit.commit(onlineTask);
            }else {
                LOGGER.info(Constants.LOG_PREFIX + " The sharding task is completed, and the post logic is continued. {}" + onlineTask.toString());
                commit(onlineTask);
            }
        }else {
            commit(onlineTask);
        }

    }

    private void commit(JobMTask onlineTask){
        for (JobMTask otask : onlineTask.getPostTask()) {
            otask.setCountDownLatch(onlineTask.getCountDownLatch());
            otask.getPreTaskCounter().getAndIncrement();
            if (otask.getPreTaskCounter().get() < otask.getPreTask().size()) {
                LOGGER.info(Constants.LOG_PREFIX + " The pre-tasks are not all completed, and the task is not started. {}", onlineTask.getTaskKey());
            } else {
                // Reset
                onlineTask.getPreTaskCounter().set(0);
                TaskCommit.commit(otask);
            }
        }
    }
}
