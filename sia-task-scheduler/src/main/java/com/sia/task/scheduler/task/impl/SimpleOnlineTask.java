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

package com.sia.task.scheduler.task.impl;

import com.sia.task.core.http.RestTemplateFactroy;
import com.sia.task.core.http.SiaHttpResponse;
import com.sia.task.core.task.DagTask;
import com.sia.task.core.util.Constant;
import com.sia.task.core.util.JsonHelper;
import com.sia.task.core.util.StringHelper;
import com.sia.task.scheduler.task.OnlineTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * SimpleOnlineTask
 *
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-10-12 14:28
 * @see
 **/
@Slf4j
public class SimpleOnlineTask implements OnlineTask {

    @Override
    public ListenableFuture<ResponseEntity<SiaHttpResponse>> run(DagTask dagTask) {

        AsyncRestTemplate asyncRestTemplate = null;
        String url = null;
        try {
            int readTimeout = dagTask.getReadTimeout() == null ? 0 : dagTask.getReadTimeout();
            asyncRestTemplate = RestTemplateFactroy.getAsyncRestTemplate(readTimeout * 1000);
            url = Constant.HTTP_PREFIX + dagTask.getCurrentHandler() + dagTask.getTaskKey().split(":")[1];
        } catch (Exception e) {
            log.error(Constant.LOG_EX_PREFIX + " 拼装[{}]请求path出错", dagTask, e);
        }
        log.info(Constant.LOG_PREFIX + " start remote scheduling... - Job:[{}]-Task:[{}]-url:[{}]", dagTask.getJobKey(), dagTask.getTaskKey(), url);
        return asyncRestTemplate.postForEntity(url, paramWapper(dagTask), SiaHttpResponse.class);
    }

    private HttpEntity<Object> paramWapper(DagTask dagTask) {

        HttpEntity<Object> httpEntity;
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        String inputParam;
        List<String> inputParams = new ArrayList<>();
        if (Constant.FROM_TASK.equals(dagTask.getInputType().toUpperCase())) {
            List<DagTask> preTask = dagTask.getPreTask();
            preTask.forEach(jobMTask -> {
                if (dagTask.getInputValue().contains(jobMTask.getTaskKey())) {
                    SiaHttpResponse response = JsonHelper.toObject(jobMTask.getOutParam(), SiaHttpResponse.class);
                    inputParams.add((String) response.getResult());
                }
            });
        } else {
            inputParams.add(StringHelper.isEmpty(dagTask.getInputValue()) ? "{}" : dagTask.getInputValue());
        }
        inputParam = inputParams.size() == 1 ? inputParams.get(0) : JsonHelper.toString(inputParams);
        log.info(Constant.LOG_PREFIX + " paramWapper - Job:[{}]-Task:[{}]-inputParam:[{}]", dagTask.getJobKey(), dagTask.getTaskKey(), inputParam);
        httpEntity = new HttpEntity<>(inputParam, headers);
        return httpEntity;
    }
}
