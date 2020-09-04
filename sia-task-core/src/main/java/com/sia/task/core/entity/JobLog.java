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

import lombok.Data;

import java.util.Date;

/**
 * Job 调度日志
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2018/4/1811:10
 * @Deprecated Integer <code>jobId</code>
 * @add String <code>jobKey</code> 使用jobKey替换jobId，
 * <p>
 * The reason for this replacement is because considering that subsequent logs
 * will be migrated and not saved locally, then the job Id is meaningless.
 * It is more meaningful to use JobKey,
 * It is more helpful to locate which job log is more helpful to locate subsequent problems.
 * </p>
 * @data 2020/4/25 11:51 上午
 **/
public @Data
class JobLog {

    /**
     * JobLog在数据库中的ID
     */
    private Integer jobLogId;

    /**
     * Job 全局唯一Id
     */
    private String traceId;

    /**
     * Job在数据库中的ID
     */
    @Deprecated
    private Integer jobId;

    /**
     * Job标识
     */
    private String jobKey;
    /**
     * Job所属组
     */
    private String jobGroup;

    /**
     * Job的调度状态
     */
    private String jobTriggerCode;

    /**
     * Job的调度信息，具体某个调度器实例
     */
    private String jobTriggerMsg;

    /**
     * Job的调度时间
     */
    private Date jobTriggerTime;

    /**
     * Job的执行状态
     */
    private String jobHandleCode;

    /**
     * Job的执行结果信息
     */
    private String jobHandleMsg;

    /**
     * Job的执行时间
     */
    private Date jobHandleTime;

    /**
     * Job的执行完成时间
     */
    private Date jobHandleFinishedTime;

    /**
     * JobLog的创建时间
     */
    private Date createTime;
}
