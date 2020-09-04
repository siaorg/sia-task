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

import com.sia.task.core.entity.TaskLog;
import lombok.Data;

import java.util.List;

/**
 * TODO 后续删除过多的VO对象，尽量使用现有的数据结果去实现，避免类增多
 * @version V1.0.0
 * @author: XSL
 * @description: 根据traceId查询到的TaskLog列表
 * @create: 2019-12-23 16:44
 * @see
 **/
@Data
public class TaskLogsVO {

    private String traceId;

    private List<TaskLog> taskLogList;
}
