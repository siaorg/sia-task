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

import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;

/**
 *
 * A template class that defines asynchronous Http calls.
 * Only declarations for asynchronous calls to post methods are defined in this class.
 *
 * @see
 * @author maozhengwei
 * @date 2018-09-28 9:56
 * @version V1.0.0
 **/
public interface RestTemplate {

    /**
     *
     * PostMethod
     * @see com.sia.scheduler.http.Request
     * @param request
     * @param responseType
     * @param uriVariables
     * @param <T>
     * @return
     */
    <T> ListenableFuture<ResponseEntity<T>> postAsyncForEntity(Request request, Class<T> responseType, Object... uriVariables);
}
