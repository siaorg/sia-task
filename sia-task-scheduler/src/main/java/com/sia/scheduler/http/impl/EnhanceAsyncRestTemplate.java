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

import com.sia.scheduler.http.AbstractRestTemplate;
import com.sia.scheduler.http.Request;
import com.sia.scheduler.http.async.AsyncRestTemplateImpl;
import com.sia.scheduler.util.constant.Constants;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;

/**
 *
 * Used for AsyncRestTemplateImpl enhancement processing, this example has no other processing logic by default.
 *
 * @description
 * @see
 * @author maozhengwei
 * @date 2018-09-28 10:18
 * @version V1.0.0
 **/
public class EnhanceAsyncRestTemplate extends AbstractRestTemplate {

    AsyncRestTemplateImpl restTemplate;

    public EnhanceAsyncRestTemplate(AsyncRestTemplateImpl restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    protected void setAsyncRestTemplate(org.springframework.web.client.AsyncRestTemplate asyncRestTemplate) {

    }

    @Override
    public <T> ListenableFuture<ResponseEntity<T>> postAsyncForEntity(Request request, Class<T> responseType, Object... uriVariables) {
        //增强处理
        LOGGER.info(Constants.LOG_PREFIX + " task asynchronous scheduling request starts [{}]", request.buildUrl());
        return restTemplate.postAsyncForEntity(request, responseType, uriVariables);

    }
}
