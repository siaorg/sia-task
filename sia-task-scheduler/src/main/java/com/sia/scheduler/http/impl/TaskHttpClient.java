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
import com.sia.core.helper.StringHelper;
import com.sia.scheduler.http.enums.AsyncRequest;
import com.sia.scheduler.http.Request;
import com.sia.scheduler.http.async.AsyncRestTemplateImpl;
import com.sia.scheduler.http.route.RouteStrategyEnum;
import com.sia.scheduler.service.common.CommonService;
import com.sia.scheduler.util.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * TaskHttpClient
 *
 * @see
 * @author maozhengwei
 * @date 2018-09-28 17:13
 * @version V1.0.0
 **/
public class TaskHttpClient extends CommonService{
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskHttpClient.class);

    /**
     * Http请求发送入口
     *
     * @param onlineTask
     */
    public void async(JobMTask onlineTask) {
        try {
            Integer readTimeout = onlineTask.getReadTimeout() == null ? -1 : onlineTask.getReadTimeout() * 1000;
            AsyncRestTemplateImpl restTemplate = AsyncRestTemplateImpl.getRestTemplateInstance(readTimeout);
            String url = Constants.HTTP_PREFIX + onlineTask.getCurrentHandler() + onlineTask.getTaskKey().split(Constants.REGEX_COLON)[1];
            HttpEntity<Object> objectHttpEntity = paramWapper(onlineTask);
            Request request = new AsyncRequest(url, objectHttpEntity);
            EnhanceAsyncRestTemplate enhanceAsyncRestTemplate = new EnhanceAsyncRestTemplate(restTemplate);
            ListenableFuture<ResponseEntity<String>> responseEntityListenableFuture = enhanceAsyncRestTemplate.postAsyncForEntity(request, String.class);
            responseEntityListenableFuture.addCallback(new OnlineListenableFutureCallback<>(onlineTask));
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_EX_PREFIX + " Exception occurred while sending an asynchronous request：", e);
            new CommonService().isExceptionCountDown(onlineTask, true,  "Exception occurred while sending an asynchronous request：" + e.getMessage());
        }
    }


    private HttpEntity<Object> paramWapper(JobMTask onlineTask) {

        HttpEntity<Object> httpEntity;
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        String inputParam;
        List<String> inputParams = new ArrayList<>();
        if (onlineTask.getInputType().equals(Constants.FROM_TASK)&& !RouteStrategyEnum.ROUTE_TYPE_SHARDING.getRouteType().equals(onlineTask.getRouteStrategy())) {
            List<JobMTask> preTask = onlineTask.getPreTask();
            preTask.forEach(jobMTask -> {
                if (onlineTask.getInputValue().contains(jobMTask.getTaskKey())) {
                    Map map = JSONHelper.toObject(jobMTask.getOutParam(), Map.class);
                    inputParams.add(map.get("result") == null ? null : map.get("result").toString());
                }
            });
        } else {
            inputParams.add(StringHelper.isEmpty(onlineTask.getInputValue()) ? "{}" : onlineTask.getInputValue());
        }
        inputParam = inputParams.size() == 1 ? inputParams.get(0) : JSONHelper.toString(inputParams);
        httpEntity = new HttpEntity<>(inputParam, headers);
        return httpEntity;
    }
}
