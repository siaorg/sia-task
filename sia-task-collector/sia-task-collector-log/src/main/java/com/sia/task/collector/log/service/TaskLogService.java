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

package com.sia.task.collector.log.service;

import com.sia.task.core.entity.TaskLog;
import com.sia.task.mapper.TaskLogMapper;

import javax.annotation.Resource;


/**
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2020/4/22 2:56 下午
 * @see
 **/
public class TaskLogService {

    @Resource
    private TaskLogMapper taskLogMapper;

    /**
     * 记录task 调度过程日志
     *
     * @param taskLog
     * @throws Exception
     */
    public void recordTaskLogs4Consumer(TaskLog taskLog) throws Exception {
        taskLogMapper.insertSelective(taskLog);
    }
}
