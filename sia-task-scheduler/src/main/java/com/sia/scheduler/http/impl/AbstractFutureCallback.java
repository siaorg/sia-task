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

import com.sia.scheduler.service.common.CommonService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 *
 * The http asynchronous callback handles the abstract class.
 * Implement {@see org.springframework.util.concurrent.ListenableFutureCallback<T>},
 * implementation {@Link ListenableFutureCallback<T>#onFailure(Throwable ex),ListenableFutureCallback<T>#onSuccess(T result) { vSuccess(result)}
 *
 * The inheritance {@see com.sia.scheduler.service.common.CommonService} is used to use the instance methods provided by CommonService and the instance objects managed by Spring.
 *
 * @see
 * @author maozhengwei
 * @date 2019-04-28 18:12
 * @version V1.0.0
 **/
public abstract class AbstractFutureCallback<T extends ResponseEntity<String>> extends CommonService implements ListenableFutureCallback<T> {

    /**
     *
     * Call failure processing logic interface
     * @param ex Failure exception
     */
    protected abstract void vFailure(Throwable ex);

    /**
     *
     * Call successfully processing logical interface
     * @param result Return result
     */
    protected abstract void vSuccess(T result);

    @Override
    public void onFailure(Throwable ex) {
        vFailure(ex);
    }

    @Override
    public void onSuccess(T result) {
        vSuccess(result);
    }

}
