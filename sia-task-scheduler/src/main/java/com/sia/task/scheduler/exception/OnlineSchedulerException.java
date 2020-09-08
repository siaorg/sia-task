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

import com.sia.task.quartz.exception.SchedulerException;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-10-11 10:11
 * @see
 **/
@NoArgsConstructor
public class OnlineSchedulerException extends SchedulerException implements Serializable {

    private static final long serialVersionUID = -132414981399511888L;


    public OnlineSchedulerException(String message) {
        super(message);
    }

    public OnlineSchedulerException(Throwable cause) {
        super(cause);
    }

    public OnlineSchedulerException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public Throwable getUnderlyingException() {
        return super.getCause();
    }

    @Override
    public String toString() {
        Throwable throwable = getUnderlyingException();
        if (throwable == null || throwable == this) {
            return super.toString();
        }
        return super.toString() + " [see nested exception: " + throwable + "]";
    }
}
