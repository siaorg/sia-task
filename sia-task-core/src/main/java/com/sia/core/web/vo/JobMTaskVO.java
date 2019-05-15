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

package com.sia.core.web.vo;


import com.sia.core.entity.JobMTask;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 *
 * @description JobMTaskVO
 * @see
 * @author MAOZW
 * @date 2018-04-18 11:10
 * @version V1.0.0
 **/
@Data
public class JobMTaskVO {

    private Integer jobId;

    private String jobKey;

    private String jobGroup;
    /**
     * 前置Task 存放规则是逗号分隔
     */
    private String preTaskKey;

    private String inputType;

    private String inputValue;

    private String outParam;

    private String routeStrategy;

    private String failover;

    private String fixIp;

    private Integer readTimeout;

    private Date updateTime;

    private Date createTime;

    private String taskKey;

    private Integer taskId;

    /**
     * 非持久化字段
     */
    private List<String> postTaskKey = new ArrayList<>();
    /**
     * 深度，为页面实现dag图
     */
    private Integer depth;

    private AtomicInteger preTaskCounter = new AtomicInteger(0);

    private String ipAndPortList;


    @Override
    public String toString() {
        return "JobMTaskVO{" +
                "jobKey='" + jobKey + '\'' +
                ", jobGroup='" + jobGroup + '\'' +
                ", preTaskKey='" + preTaskKey + '\'' +
                ", inputType='" + inputType + '\'' +
                ", inputValue='" + inputValue + '\'' +
                ", outParam='" + outParam + '\'' +
                ", routeStrategy='" + routeStrategy + '\'' +
                ", failover='" + failover + '\'' +
                ", fixIp='" + fixIp + '\'' +
                ", taskKey='" + taskKey + '\'' +
                '}';
    }
}
