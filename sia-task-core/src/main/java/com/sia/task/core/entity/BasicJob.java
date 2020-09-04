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
 *
 *
 * @author: zhengweimao
 * @Description: 调度作业类
 * @date: 2019年2月20日 下午3:30:40 代码检查
 *
 */
public @Data class BasicJob {

    /** Job在数据库中的ID */
    private Integer jobId;

    /** 唯一标识一个Job的Key值 */
    private String jobKey;

    /** Job所属的项目组名 */
    private String jobGroup;

    /** Job的触发类型 */
    private String jobTrigerType;

    /** Job的触发类型值 */
    private String jobTrigerValue;

    /** Job的描述信息 */
    private String jobDesc;

    /** Job的预警邮箱 */
    private String jobAlarmEmail;

    /** Job的创建时间 */
    private Date jobCreateTime;

    /** Job的修改时间 */
    private Date jobUpdateTime;

    /** 级联Job的前置jobKey */
    private String jobParentKey;

    /** 级联Job标识 */
    private String jobPlan;

    /** 非持久化，调度器实例 */
    private String triggerInstance;

    /** 后置Job */
    private BasicJob jobChild;

}
