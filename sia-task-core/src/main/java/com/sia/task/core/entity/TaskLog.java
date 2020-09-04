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
 * task 日志实例
 *
 * @description
 * @see
 * @author maozhengwei
 * @data 2020/4/21 7:01 下午
 * @version V1.0.0
 **/
public @Data
class TaskLog {

    /**
     * TaskLog在数据库中的ID
     */
    private Integer taskLogId;

    /**
     * 用于代替jobLogId
     * 由于使用jobLogId，会存在关联局限性，生产后和消费不能完全的解耦，存在硬关联.
     */
    private String traceId;

    /**
     * JobLog在数据库中的ID
     */
    @Deprecated
    private Integer jobLogId;

    /**
     * 唯一标识一个Job的Key值
     */
    private String jobKey;

    /**
     * 唯一标识一个Task的Key值
     */
    private String taskKey;

    /**
     * Task的执行信息
     */
    private String taskMsg;

    /**
     * Task的执行状态
     */
    private String taskStatus;

    /**
     * Task执行时间
     */
    private Date taskHandleTime;

    /**
     * Task执行完成时间
     */
    private Date taskFinishedTime;

    /**
     * TaskLog的创建时间
     */
    private Date createTime;

}
