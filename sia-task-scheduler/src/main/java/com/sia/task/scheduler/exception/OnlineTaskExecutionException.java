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

package com.sia.task.scheduler.exception;

import com.sia.task.core.exceptions.TaskBaseExecutionException;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OnlineTaskExecutionException extends TaskBaseExecutionException {

    private static final long serialVersionUID = -3768931046006785679L;


    /**
     * <p>
     * Create a OnlineTaskExecutionException, with the given cause.
     * </p>
     */
    public OnlineTaskExecutionException(Throwable cause) {
        super(cause);
    }

    /**
     * <p>
     * Create a OnlineTaskExecutionException with the given message, and underlying
     * exception.
     * </p>
     */
    public OnlineTaskExecutionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
