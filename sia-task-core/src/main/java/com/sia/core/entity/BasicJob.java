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
 * @date 2019-02-20 3:30:40
 * @version V1.0.0
 **/
public @Data class BasicJob {

    /** job id in DB */
    private Integer jobId;

    /** mark the unique job */
    private String jobKey;

    /** group name to which job belongs */
    private String jobGroup;

    /** job's trigger type: cron/fixrate */
    private String jobTrigerType;

    /** job's trigger value */
    private String jobTrigerValue;

    /** job's description */
    private String jobDesc;

    /** job's alarm email */
    private String jobAlarmEmail;

    /** creating time of job */
    private Date jobCreateTime;

    /** modified time of job */
    private Date jobUpdateTime;

    /** pre-jobKey of cascade job */
    private String jobParentKey;

    /** sign of cascade job */
    private String jobPlan;

    /** 非持久化，调度器实例 */
    private String triggerInstance;

    /** post-job in cascade-job */
    private BasicJob jobChild;

}
