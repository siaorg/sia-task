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

package com.sia.core.entity;

import lombok.Data;

import java.util.Date;
/**
 *
 *
 * @description
 * @see
 * @author zhengweimao
 * @date 2018-04-18 11:10
 * @version V1.0.0
 **/
public @Data class TaskLog {

    /** TaskLog ID in DB */
    private Integer taskLogId;

    private String traceId;

    /** JobLog ID in DB */
    private Integer jobLogId;

    /** mark the unique job */
    private String jobKey;

    /** mark the unique task */
    private String taskKey;

    /** Task's executing info */
    private String taskMsg;

    /** Task's executing status */
    private String taskStatus;

    /** start time of executing task */
    private Date taskHandleTime;

    /** finished time of Task executing */
    private Date taskFinishedTime;

    /** create time of TaskLog */
    private Date createTime;

}
