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

package com.sia.task.core.task;

import lombok.Data;

import java.util.Date;

/**
 * Task
 *
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-11-07 15:46
 * @see
 **/
public @Data
class Task {
    /**
     * 唯一标识一个Job的Key值
     */
    protected Long Id;

    /**
     * 唯一标识一个Job的Key值
     */
    protected String jobKey;

    /**
     * Job所属的项目组名
     */
    protected String jobGroup;

    /**
     * 前置Task 存放规则是逗号分隔
     */
    protected String preTaskKey;

    /**
     * Task入参来源
     */
    protected String inputType;

    /**
     * Task入参值
     */
    protected String inputValue;

    /**
     * Task出参
     */
    protected String outParam;

    /**
     * Task路由策略
     */
    protected String routeStrategy;

    /**
     * Task的failover策略
     */
    protected String failover;

    /**
     * Task路由策略为固定IP时的IP值
     */
    protected String fixIp;

    /**
     * JobMTask的修改时间
     */
    protected Date updateTime;

    /**
     * JobMTask的创建时间
     */
    protected Date createTime;

    /**
     * 唯一标识一个Task的Key值
     */
    protected String taskKey;

    /**
     * Task执行器超时时间
     */
    protected Integer readTimeout;

    /**
     * Task的创建来源
     */
    protected String taskSource;
}
