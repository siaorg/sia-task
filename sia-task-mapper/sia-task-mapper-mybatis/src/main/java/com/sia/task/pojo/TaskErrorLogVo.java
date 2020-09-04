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


import lombok.Data;

import java.util.Date;

/**
 * 日常调度日志vo
 * @author maozhengwei
 * @date 2020/6/16 5:40 下午
 * @version V1.0.0
 **/
@Data
public class TaskErrorLogVo {

    private String taskKey;

    private Integer errorCount;

    private String jobKey;

    private Date errorTime;
}
