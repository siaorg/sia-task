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
public @Data class JobLog {

    /** JobLog ID in DB */
    private Integer jobLogId;

    /** Job ID in DB */
    private Integer jobId;

    /** Job's scheduling result */
    private String jobTriggerCode;

    /** Job's scheduling info: scheduler instance */
    private String jobTriggerMsg;

    /** Job's start time of scheduling */
    private Date jobTriggerTime;

    /** Job's executing status */
    private String jobHandleCode;

    /** Job's executing result */
    private String jobHandleMsg;

    /** Job's start time of executing */
    private Date jobHandleTime;

    /** Job's finished time of executing */
    private Date jobHandleFinishedTime;

    /** creating time of JobLog */
    private Date createTime;
}
