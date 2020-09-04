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

package com.sia.task.admin.service;

import com.sia.task.admin.exception.SiaTaskException;
import com.sia.task.core.util.PageBean;
import com.sia.task.core.util.StringHelper;
import com.sia.task.mapper.JobLogMapper;
import com.sia.task.mapper.TaskLogMapper;
import com.sia.task.pojo.JobLogVO;
import com.sia.task.pojo.TaskLogsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 日志管理界面 - Log4Service
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2020/4/25 10:51 下午
 **/
@Service
@Slf4j
public class Log4Service {
    @Resource
    private JobLogMapper jobLogMapper;
    @Resource
    private TaskLogMapper taskLogMapper;

    /**
     * 分页查询日志
     *
     * @param jobGroup
     * @param jobKey
     * @param currentPage
     * @param pageSize
     * @return
     * @throws Exception
     */
    public PageBean<JobLogVO> selectLogs4Page(String jobGroup, String jobKey, int currentPage, int pageSize) throws Exception {
        if (StringHelper.isEmpty(jobGroup) && StringHelper.isEmpty(jobKey)) {
            throw new SiaTaskException("Unreasonable request parameters");
        }
        List<String> roleNames = UserService.getCurrentUserAllRoles();
        roleNames.retainAll(Arrays.asList(jobGroup));
        List<JobLogVO> jobLogs = jobLogMapper.selectLogs4Page(roleNames.get(0), jobKey, (currentPage - 1) * pageSize, pageSize);
        List<String> traceIds = new ArrayList<>();
        for (JobLogVO jobLog : jobLogs) {
            traceIds.add(jobLog.getTraceId());
        }
        Map<String, TaskLogsVO> taskLogMap = null;
        if (traceIds.size() != 0) {
            taskLogMap = taskLogMapper.selectByBatchTraceId(traceIds);
        }
        if (taskLogMap != null && taskLogMap.size() != 0) {
            for (JobLogVO jobLog : jobLogs) {
                TaskLogsVO taskLogsVO = taskLogMap.get(jobLog.getTraceId());
                if (taskLogsVO != null) {
                    jobLog.setTaskLogList(taskLogsVO.getTaskLogList());
                }
            }
        }
        //总记录数
        int countJobLogs = jobLogMapper.selectCount4JobLogs(roleNames.get(0), jobKey);
        PageBean<JobLogVO> pageData = new PageBean<>(currentPage, pageSize, countJobLogs);
        pageData.setItems(jobLogs);
        return pageData;
    }

    /**
     * 获取group所包含的日志数目 selectCountGroupsJobLogs
     * 新版本不需要此种方式统计,接口提供给管理员的监控首页面进行展示数据使用
     *
     * @return
     */
    public List<Map<String, Integer>> selectLogCount4AuthGroups(String jobGroup) throws Exception {
        if (!StringHelper.isEmpty(jobGroup)) {
            return jobLogMapper.selectLogCount4Groups(Arrays.asList(jobGroup));
        }
        List<String> allRoles = UserService.getCurrentUserAllRoles();
        return allRoles.size() == 0 ? Collections.EMPTY_LIST : jobLogMapper.selectLogCount4Groups(allRoles);
    }
}
