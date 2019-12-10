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
import com.sia.scheduler.context.SpringContext;
import com.sia.scheduler.http.enums.AsyncResponse;
import com.sia.scheduler.http.enums.FailoverEnum;
import com.sia.scheduler.http.enums.ResponseStatus;
import com.sia.scheduler.http.route.ExecutorRouteSharding;
import com.sia.scheduler.http.route.RouteStrategyEnum;
import com.sia.scheduler.http.route.RouteStrategyHandler;
import com.sia.scheduler.log.enums.LogStatusEnum;
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

        LOGGER.info(Constants.LOG_PREFIX + " vFailure [{}]", onlineTask);

        boolean isCountDown = onFailure(onlineTask, ex);
        if (isCountDown) {
            isExceptionCountDown(onlineTask, true, " onFailure is Exception : " + ex.getMessage());
        }
    }

    @Override
    protected void vSuccess(T result) {

        LOGGER.info(Constants.LOG_PREFIX + " vSuccess [{}]", result);

        String resultBody = result.getBody();
        onSuccess(onlineTask, resultBody);
    }

    private void onSuccess(JobMTask onlineTask, String resultBody) {

        Map<String, String> responseTarget;
        try {
            // 1解析返回的数据
            responseTarget = JSONHelper.toObject(resultBody, Map.class);
            onlineTask.setOutParam(resultBody);

        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " Parsing the returned object with an exception. responseEntityBody Exception : ", e);
            SpringContext.getTaskLogService().recordTaskLog(onlineTask, LogStatusEnum.LOG_TASK_CALLBACKERROR, resultBody);
            isExceptionCountDown(onlineTask, true, "  Parsing the returned object with an exception.  responseEntityBody Exception : " + e.getMessage());
            return;
        }

        //2 TODO 记录日志 需要优化
        if (responseTarget != null && responseTarget.containsKey(AsyncResponse.STATUS)) {
            if (ResponseStatus.SUCCESS.toString().equals(responseTarget.get(AsyncResponse.STATUS).toUpperCase())) {
                reset(onlineTask);
                LOGGER.info(Constants.LOG_PREFIX + " responseTarget Task is {} execution completed, the result is {}", onlineTask.getTaskKey(), resultBody);
                SpringContext.getTaskLogService().recordTaskLog(onlineTask, LogStatusEnum.LOG_TASK_FINISHED, resultBody);
                commitSuccessTask(onlineTask);
            } else {
                toFailure(onlineTask, resultBody);
            }
        } else {
            toFailure(onlineTask, resultBody);
        }
    }


    private void toFailure(JobMTask onlineTask, String resultBody) {
        LOGGER.error(" toFailure >>> Task execution is completed, an exception is returned during the processing of an abnormal result : [{}], [{}]", onlineTask, resultBody);
        try {
            boolean isCountDown = onFailure(onlineTask, null);
            if (isCountDown) {
                isExceptionCountDown(onlineTask, true, " Task execution is completed and the result is ：" + resultBody);
            }
            LOGGER.info(Constants.LOG_PREFIX + " toFailure >>> Task execution completes and returns an abnormal result:{}", resultBody);
        } catch (Exception e) {
            LOGGER.error(" toFailure >>> Task execution is completed, an exception occurs during the processing of the returned abnormal result: ", e);
            isExceptionCountDown(onlineTask, true, " Task execution completes and returns abnormal results >>> The method of toFailure has an exception : " + e.getMessage());
        }
    }


    private boolean onFailure(JobMTask onlineTask, Throwable throwable) {
        boolean isCountDown;
        LOGGER.info(Constants.LOG_PREFIX + " Task [{}] >>> onFailure : [{}]", onlineTask, throwable);
        switch (FailoverEnum.getByValue(onlineTask.getFailover().toLowerCase())) {
            case IGNORE:
                isCountDown = SpringContext.getAsyncBackLog().onIgnore(onlineTask, throwable);
                commitSuccessTask(onlineTask);
                break;
            case TRANSFER:
                isCountDown = SpringContext.getAsyncBackLog().onTransfer(onlineTask, throwable);
                break;
            case MULTI_CALLS_TRANSFER:
                isCountDown = SpringContext.getAsyncBackLog().onMultiCallsAndTransfer(onlineTask, throwable);
                break;
            case STOP:
                isCountDown = SpringContext.getAsyncBackLog().onStop(onlineTask, throwable);
                break;
            default:
                isCountDown = SpringContext.getAsyncBackLog().onStop(onlineTask, throwable);
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
