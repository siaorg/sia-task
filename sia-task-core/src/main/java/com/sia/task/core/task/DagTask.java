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

import com.sia.task.core.util.JsonHelper;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <code>{@link DagTask}</code> is an extended use of <code>{@link Task}</code>,
 * which counts the number of multi-threaded operations during task execution
 * and saves the relationship between tasks.
 *
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-11-07 15:46
 * @see Task
 **/
@Data
public class DagTask extends Task {

    /**
     * Task的后置Task
     */
    private List<DagTask> postTask = new ArrayList<>();

    /**
     * Task的前置Task
     */
    private List<DagTask> preTask = new ArrayList<>();

    /**
     * Task的前置任务完成计数器
     */
    private AtomicInteger preTaskCounter = new AtomicInteger(0);

    /**
     * 持久化执行器
     */
    private String executors4Persistent;

    /**
     * 缓存当次运行时的执行器列表
     */
    private List<String> executors;

    /**
     * 当前执行器
     */
    private String currentHandler;

    /**
     * job的预警邮箱
     */
    private String jobAlarmEmail;

    /**
     * traceId
     */
    private String traceId;

    private boolean failoverFlag = false;

    /*
      The following three fields annotated with @Deprecated will be removed in subsequent versions.
      It is not recommended to use them again, it is recommended to use alternative fields
      jobId -> jobKey
      taskId -> taskKey
      jobLogId -> traceId
     */
    /**
     * Job在数据库中的ID
     */
    @Deprecated
    private Integer jobId;

    /**
     * Task在数据库中的ID
     */
    @Deprecated
    private Integer taskId;

    /**
     * jobLog在数据库中的ID
     */
    @Deprecated
    private int jobLogId;

    /**
     * 用于参与job的生命周期
     */
    @Deprecated
    private CountDownLatch countDownLatch;

    @Override
    public String toString() {
        return "SiaDagTask{" +
                "jobKey='" + jobKey + '\'' +
                ", jobGroup='" + jobGroup + '\'' +
                ", traceId='" + traceId + '\'' +
                ", preTaskKey='" + preTaskKey + '\'' +
                ", inputType='" + inputType + '\'' +
                ", inputValue='" + inputValue + '\'' +
                ", outParam='" + outParam + '\'' +
                ", routeStrategy='" + routeStrategy + '\'' +
                ", failover='" + failover + '\'' +
                ", fixIp='" + fixIp + '\'' +
                ", taskKey='" + taskKey + '\'' +
                ", taskSource='" + taskSource + '\'' +
                ", currentHandler='" + currentHandler + '\'' +
                '}';
    }

    public DagTask deepClone() {
        String task = JsonHelper.toString(this);
        return JsonHelper.toObject(task, DagTask.class);
    }

    public DagTask baseCopy() {
        DagTask copy = new DagTask();
        copy.setTraceId(this.traceId);
        copy.setTaskKey(this.taskKey);
        copy.setJobKey(this.jobKey);
        copy.setJobGroup(this.jobGroup);
        copy.setCurrentHandler(this.currentHandler);
        copy.setOutParam(this.outParam);
        copy.setInputType(this.inputType);
        copy.setInputValue(this.inputValue);
        copy.setFixIp(this.fixIp);
        copy.setFailover(this.failover);
        copy.setRouteStrategy(this.routeStrategy);
        copy.setTaskSource(this.getTaskSource());
        copy.setJobAlarmEmail(this.getJobAlarmEmail());
        return copy;
    }
}
