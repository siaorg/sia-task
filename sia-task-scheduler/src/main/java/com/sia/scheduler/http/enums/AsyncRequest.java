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

package com.sia.scheduler.http.enums;

import com.sia.scheduler.http.Request;
import org.springframework.http.HttpEntity;

/**
 *
 *
 * @see com.sia.scheduler.http.Request
 * @author maozhengwei
 * @date 2018-09-28 11:19
 * @version V1.0.0
 **/
public class AsyncRequest implements Request {

    private String url;

    private HttpEntity<?> entity;

    public AsyncRequest(String url,HttpEntity<?> entit) {
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
