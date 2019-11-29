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

package com.sia.scheduler.http.async;

import com.sia.scheduler.http.AbstractRestTemplate;
import com.sia.scheduler.http.Request;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

/**
 *
 * The implementation class for asynchronous access.
 * Provide custom @ReadRestTemplateInstance method to set custom ReadTimeout
 *
 * ConnectTimeout ：default timeout. >>> 30 * 1000;
 * ReadTimeout ： default timeout. >>> 60 * 60 * 1000;
 * @see
 * @author maozhengwei
 * @date 2018-09-14 19:46
 * @version V1.0.0
 **/
public class AsyncRestTemplateImpl extends AbstractRestTemplate {

    /**
     * ConnectTimeout is the system's default timeout. >>> 30 * 1000;
     * ReadTimeout is the system's default timeout. >>> 60 * 60 * 1000;
     */
    public static final int CONNECTTIMEOUT_MAX = 30 * 1000;
    public static final int READTIMEOUT_DEFULT = 60 * 60 * 1000;


    /**
     *
     *
     *
     * @return
     */
    public static AsyncRestTemplateImpl getRestTemplateInstance(int readTimeout) {

        return new AsyncRestTemplateImpl(readTimeout > 0 ? readTimeout : READTIMEOUT_DEFULT);
    }


    private AsyncRestTemplateImpl(int readTimeout) {
        super();
        setAsyncRestTemplate(getAsyncRestTemplate(readTimeout));
    }

    /**
     * 封装asyncRestTemplate
     *
     * @param template
     */
    @Override
    protected void setAsyncRestTemplate(AsyncRestTemplate template) {
        super.asyncRestTemplate = template;
    }


    /**
     *  CONNECTTIMEOUT_MAX Set the underlying URLConnection's connect timeout (in milliseconds).
     * @param readTimeout    Set the underlying URLConnection's read timeout (in milliseconds).
     * @return
     */
    private AsyncRestTemplate getAsyncRestTemplate(int readTimeout) {
        return createAsyncRestTemplate(readTimeout);
    }

    /**
     *  CONNECTTIMEOUT_MAX Set the underlying URLConnection's connect timeout (in milliseconds).
     * @param readTimeout    Set the underlying URLConnection's read timeout (in milliseconds).
     *                       readTimeout * 1000 转换为seconds
     * @return
     */
    private AsyncRestTemplate createAsyncRestTemplate(int readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECTTIMEOUT_MAX);
        requestFactory.setReadTimeout(readTimeout);
        requestFactory.setTaskExecutor(new SimpleAsyncTaskExecutor());
        AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();
        asyncRestTemplate.setAsyncRequestFactory(requestFactory);
        return asyncRestTemplate;
    }


    @Override
    public <T> ListenableFuture<ResponseEntity<T>> postAsyncForEntity(Request request, Class<T> responseType, Object... uriVariables) {
        return super.postAsyncForEntity(request, responseType, uriVariables);
    }


}
