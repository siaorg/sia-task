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

package com.sia.task.core.http;

import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 *
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-10-17 15:26
 * @see
 **/
public class RestTemplateFactroy {

    /**
     * ConnectTimeout is the system's default timeout. >>> 30 * 1000;
     * ReadTimeout is the system's default timeout. >>> 60 * 60 * 1000;
     */
    public static final int CONNECTTIMEOUT_MAX = 30 * 1000;
    public static final int READTIMEOUT_DEFULT = 60 * 60 * 1000;

    private AsyncRestTemplate asyncRestTemplate;

    private static RestTemplateFactroy factroy = new RestTemplateFactroy();

    private RestTemplate restTemplate;

    public static AsyncRestTemplate getAsyncRestTemplate(int readTimeout, int connectTimeout) throws IOException {
        return factroy.getAsyncRestTemplateInstence(readTimeout, connectTimeout);
    }

    public static AsyncRestTemplate getAsyncRestTemplate(int readTimeout) throws IOException {
        return factroy.getAsyncRestTemplateInstence(readTimeout);
    }

    public static AsyncRestTemplate getAsyncRestTemplate() throws IOException {
        return factroy.getAsyncRestTemplateInstence();
    }


    public static RestTemplate getRestTemplate(int readTimeout, int connectTimeout) throws IOException {
        return factroy.getRestTemplateInstence(readTimeout, connectTimeout);
    }

    public static RestTemplate getRestTemplate(int readTimeout) throws IOException {
        return factroy.getRestTemplateInstence(readTimeout);
    }

    public static RestTemplate getRestTemplate() throws IOException {
        return factroy.getRestTemplateInstence();
    }




    private AsyncRestTemplate getAsyncRestTemplateInstence() throws IOException {
        return getAsyncRestTemplateInstence(0, 0);
    }

    private AsyncRestTemplate getAsyncRestTemplateInstence(int readTimeout) throws IOException {
        return getAsyncRestTemplateInstence(readTimeout, 0);
    }

    private AsyncRestTemplate getAsyncRestTemplateInstence(int readTimeout, int connectTimeout) throws IOException {
        asyncRestTemplate = new AsyncRestTemplate();
        asyncRestTemplate.setAsyncRequestFactory(buildHttpRequestFactory(readTimeout, connectTimeout));
        return asyncRestTemplate;
    }

    private RestTemplate getRestTemplateInstence() throws IOException {
        return getRestTemplateInstence(0, 0);
    }

    private RestTemplate getRestTemplateInstence(int readTimeout) throws IOException {
        return getRestTemplateInstence(readTimeout, 0);
    }

    private RestTemplate getRestTemplateInstence(int readTimeout, int connectTimeout) throws IOException {
        restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(buildHttpRequestFactory(readTimeout, connectTimeout));
        return restTemplate;
    }



    private SimpleClientHttpRequestFactory buildHttpRequestFactory(int readTimeout, int connectTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout > 0 ? connectTimeout : CONNECTTIMEOUT_MAX);
        requestFactory.setReadTimeout(readTimeout > 0 ? readTimeout : READTIMEOUT_DEFULT);
        requestFactory.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return requestFactory;
    }

}
