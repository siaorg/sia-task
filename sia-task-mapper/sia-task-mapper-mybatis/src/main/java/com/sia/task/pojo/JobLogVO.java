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

package com.sia.task.pojo;

import com.sia.task.core.entity.JobLog;
import com.sia.task.core.entity.TaskLog;
import lombok.Data;

import java.util.List;

/**
 * @author: MAOZW
 * @Description: JobAndTaskLogVO
 * @date 2018/4/1811:10
 * @see JobLog
 */
@Data
public class JobLogVO extends JobLog {
    private List<TaskLog> taskLogList;
}
