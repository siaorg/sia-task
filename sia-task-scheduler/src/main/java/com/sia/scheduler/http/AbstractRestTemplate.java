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

package com.sia.scheduler.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

/**
 *
 * Implement the abstract class of the Rest Template class
 * Implemented the Rest Template#post Async For Entity method
 * Finally, use `AsyncRestTemplate Spring's central class for asynchronous client-side HTTP access.` for the call of `PostForEntity` method.
 *
 * @see org.springframework.web.client.AsyncRestTemplate
 * @author maozhengwei
 * @date 2019-04-28 16:29
 * @version V1.0.0
 **/
public abstract class AbstractRestTemplate implements RestTemplate {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractRestTemplate.class);

    public AsyncRestTemplate asyncRestTemplate;

    /**
     * setAsyncRestTemplate
     * @param asyncRestTemplate
     */
    protected abstract void setAsyncRestTemplate(AsyncRestTemplate asyncRestTemplate);


    @Override
    public <T> ListenableFuture<ResponseEntity<T>> postAsyncForEntity(Request request, Class<T> responseType, Object... uriVariables) {
        String url = request.buildUrl();
        HttpEntity<?> httpEntity = request.entityWapper();
        ListenableFuture<ResponseEntity<T>> responseEntityListenableFuture;
        try {
            responseEntityListenableFuture = asyncRestTemplate.postForEntity(url, httpEntity, responseType, uriVariables);
        } catch (Exception e) {
            throw e;
        }
        return responseEntityListenableFuture;
    }
}
