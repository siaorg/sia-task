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

package com.sia.task.collector.log.service;

import com.sia.task.core.entity.JobLog;
import com.sia.task.core.log.LogStatusEnum;
import com.sia.task.core.util.Constant;
import com.sia.task.core.util.ExecuteTaskThreadPool;
import com.sia.task.core.util.JsonHelper;
import com.sia.task.core.util.StringHelper;
import com.sia.task.mapper.JobLogMapper;
import com.sia.task.pojo.JobLogVO;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;


/**
 * @author maozhengwei
 * @version V1.0.0
 * @data 2020/4/22 3:35 下午
 * @see
 **/
@Slf4j
public class JobLogService {

    @Resource
    private JobLogMapper jobLogMapper;

    public void insertSelective(JobLog jobLog) throws Exception {
        jobLogMapper.insertSelective(jobLog);
    }


    public void updateJobLogs4Consumer(JobLog jobLog, LogStatusEnum status) throws Exception {
        log.info(Constant.LOG_PREFIX + "updateJobLog - {} , - {}", jobLog, status);
        jobLogMapper.updataJobLogByTraceId(jobLog);
        boolean sendStoreToEs = LogStatusEnum.LOG_JOB_HANDLE_FAIL_STOP.equals(status) || LogStatusEnum.LOG_STATUS_TASK_HANDLE_FAIL_STOP.equals(status);
        if (sendStoreToEs) {
            // TODO 开发关闭
            //jobLogStoreES(jobLog);
        }
    }

    /**
     * 根据TraceId 查询SiaDagTask调度日志
     * @param jobGroup
     * @param jobKey
     * @param traceId
     * @return 包含Job日志(包含Task日志)
     * @throws Exception
     */
    public String selectLogs4SiaDagTask(String jobGroup, String jobKey, String traceId) throws Exception {
        List<JobLogVO> jobLogs = jobLogMapper.selectLogs4SiaDagTask(jobGroup, jobKey, traceId);
        return JsonHelper.toString(jobLogs);
    }

    /**
     * jobLog及其引用的taskLog存储至ES中
     * 当该线程池为jobLogStoreES专属的线程池时，加入拒绝策略(直接丢弃)，避免大量日志落ES时大量创建线程导致内存耗尽
     *
     * @param jobLog
     */
    private void jobLogStoreES(JobLog jobLog) {
        ExecutorService executor = ExecuteTaskThreadPool.getExecutorService("jobLogStoreES");
        executor.submit(() -> {
            String jobAndTaskLogStr = "";
            long jobHandleTime = 0;
            try {
                jobAndTaskLogStr = selectLogs4SiaDagTask(jobLog.getJobGroup(), jobLog.getJobKey(), jobLog.getTraceId());
                if (!StringHelper.isEmpty(jobAndTaskLogStr)) {
                    List jobLogAndTaskLogList = JsonHelper.toObject(jobAndTaskLogStr, List.class);
                    HashMap<Object, Object> jobLogMap = (HashMap<Object, Object>) jobLogAndTaskLogList.get(0);
                    jobHandleTime = (long) jobLogMap.get("jobHandleTime");
                }
            } catch (Exception e) {
                log.error(Constant.LOG_EX_PREFIX + " selectJobLogAndTaskLogList TASK Exception : ", e);
            }
            Map<String, Object> jobAndTaskLogMap = new HashMap<>(16);
            jobAndTaskLogMap.put("jobLog", jobAndTaskLogStr);
            jobAndTaskLogMap.put("jobKey", jobLog.getJobKey());
            jobAndTaskLogMap.put("traceId", jobLog.getTraceId());
            jobAndTaskLogMap.put("jobHandleTime", jobHandleTime);
            //kafkaProducerHandler.send(new MqMessage("jobLog", JsonHelper.toString(jobAndTaskLogMap)));
            log.debug(Constant.LOG_PREFIX + "send jobLog success!");
        });
    }
}
