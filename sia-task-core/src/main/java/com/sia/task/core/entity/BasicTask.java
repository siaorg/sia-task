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
 * @Description: 基础任务
 * @date: 2019年2月20日 下午3:34:36 代码检查
 *
 */
public @Data class BasicTask {

    /** Task在数据库中的ID */
    private Integer taskId;

    /** 唯一标识一个Task的Key值 */
    private String taskKey;

    /** Task所属的项目组名称 */
    private String taskGroupName;

    /** Task所属的项目实例名称 */
    private String taskAppName;

    /** Task执行器的请求路径 */
    private String taskAppHttpPath;

    /** Task执行器的IP和端口号 */
    private String taskAppIpPort;

    /** Task的描述信息 */
    private String taskDesc;

    /** Task是否配置参数，1代表配置参数，0代表不配置参数 */
    private Integer paramCount;

    /** Task的创建时间 */
    private Date createTime;

    /** Task的修改时间 */
    private Date updateTime;

    /** Task的创建来源 */
    private String taskSource;

}
