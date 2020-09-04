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

package com.sia.task.core.entity;

import com.sia.task.core.log.LogStatusEnum;
import com.sia.task.core.task.DagTask;
import lombok.Data;

import java.util.Date;

/**
 * 一个LogMessage 用于承载日志信息进行发送至日志消费者
 * <p>
 * Log write mode changed to asynchronous,
 * which avoids the task execution blocking caused by log synchronization,
 * and other network problems cause the DB to be unable to respond or the failure to catch the exception causes the task to terminate.
 * </p>
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2019-11-08 10:40
 * @see
 **/
@Data
public class LogMessage {

    enum LogType {
        jobLog, taskLog
    }

    protected DagTask mTask;

    protected TaskLog taskLog;

    protected JobLog jobLog;

    protected LogType type;

    protected String message;

    protected LogStatusEnum statusEnum;

    protected Date timer;
}
