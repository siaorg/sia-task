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


import com.sia.core.helper.JSONHelper;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 *
 * @description
 * @see
 * @author zhengweimao
 * @date 2018-04-18 11:10
 * @version V1.0.0
 **/
public @Data class JobMTask {

    /** Job ID in DB */
    private Integer jobId;

    /** mark the unique job */
    private String jobKey;

    /** group name to which job belongs */
    private String jobGroup;

    /** pre-Task separated by ',' */
    private String preTaskKey;

    /** param source of Task */
    private String inputType;

    /** param value of Task */
    private String inputValue;

    /** outParam of Task */
    private String outParam;

    /** routing strategy of Task */
    private String routeStrategy;

    /** failover strategy of task */
    private String failover;

    /** ip when Task routing strategy is fixed ip */
    private String fixIp;

    /** update time of JobMTask */
    private Date updateTime;

    /** creating time of JobMTask */
    private Date createTime;

    /** mark the unique task */
    private String taskKey;

    /** Task ID in DB */
    private Integer taskId;

    /** timeout of Task executor */
    private Integer readTimeout;

    /** 非持久化字段,Task的后置Task */
    private List<JobMTask> postTask = new ArrayList<>();

    /** 非持久化字段,Task的前置Task */
    private List<JobMTask> preTask = new ArrayList<>();

    /** 非持久化字段,Task的前置Task个数 */
    private AtomicInteger preTaskCounter = new AtomicInteger(0);
    /**
     * 持久化执行器
     */
    private String ipAndPortList;
    /**
     * current executor
     */
    private String currentHandler;

    /** jobLog ID in DB */
    private int jobLogId;

    private String traceId;

    /** alarm email of job */
    private String jobAlarmEmail;
    /**
     * about the job lifecycle
     */
    private CountDownLatch countDownLatch;


    @Override
    public String toString() {
        return "JobMTask{" +
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
                ", currentHandler='" + currentHandler + '\'' +
                '}';
    }

    public JobMTask deepClone() {
        String task = JSONHelper.toString(this);
        return JSONHelper.toObject(task,JobMTask.class);
    }
}
