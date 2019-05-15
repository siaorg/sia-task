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

import com.sia.core.helper.JSONHelper;
import com.sia.scheduler.http.AbstractResponse;
import lombok.Data;

import java.util.Map;

/**
 *
 * Implemented the abstract class Abstract Response.
 * Declare that the generic type `target` is used to load the data data of the specific http response object.
 * This example recommends using Map as the receiving object.
 * The declaration keyword 'status` is used to determine the type of the http request result of the call to the task.
 * The result contains two cases: success/fail
 *
 * @see AbstractResponse
 * @author maozhengwei
 * @date 2018-09-28 11:19
 * @version V1.0.0
 **/
public @Data class AsyncResponse<T extends Map<String,String>> extends AbstractResponse {

    private T target;

    public static final String STATUS="status";


    public AsyncResponse() {
    }

    public AsyncResponse(T target) {
        this.target = target;
    }


    @Override
    public String toString() {
        return "AsyncResponse{" +
                "code" + status +
                "target=" + JSONHelper.toString(target) +
                '}';
    }
}
