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

import org.springframework.http.HttpEntity;

/**
 * @author: MAOZW
 * @Description: 封装request对象
 * @date 2018/9/28 11:19
 */
public class SiaHttpRequest implements Request {

    private String url;

    private HttpEntity<?> entity;

    public SiaHttpRequest(String url, HttpEntity<?> entit) {
        this.url = url;
        this.entity = entit;
    }


    @Override
    public String buildUrl() {
        return url;
    }

    @Override
    public HttpEntity<?> entityWapper() {
        return entity;
    }
}
