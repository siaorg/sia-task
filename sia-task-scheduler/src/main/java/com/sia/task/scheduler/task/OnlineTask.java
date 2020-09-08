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

package com.sia.task.scheduler.task;

import com.sia.task.core.exceptions.TaskBaseExecutionException;
import com.sia.task.core.http.SiaHttpResponse;
import com.sia.task.core.task.DagTask;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;

/**
 *
 *
 * @description
 * @see
 * @author maozhengwei
 * @data 2019-10-11 20:48
 * @version V1.0.0
 **/
public interface OnlineTask {

    /**
     * remote execution task
     *
     * @param dagTask
     * @return
     * @throws TaskBaseExecutionException
     */
    ListenableFuture<ResponseEntity<SiaHttpResponse>> run(DagTask dagTask) throws TaskBaseExecutionException;
}
