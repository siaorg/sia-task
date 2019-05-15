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
 * @date 2019-02-20 3:34:36
 * @version V1.0.0
 **/
public @Data class BasicTask {

    /** Task ID in DB */
    private Integer taskId;

    /** mark the unique task */
    private String taskKey;

    /** group name to which task belongs */
    private String taskGroupName;

    /** Task所属的项目实例名称 */
    private String taskAppName;

    /** the request http path of task executor */
    private String taskAppHttpPath;

    /** ip and port of task executor */
    private String taskAppIpPort;

    /** Task's description */
    private String taskDesc;

    /** whether task has param，1:yes，0:no */
    private Integer paramCount;

    /** creating time of task */
    private Date createTime;

    /** modified time of task */
    private Date updateTime;

    /** source of the created task */
    private String taskSource;

}
