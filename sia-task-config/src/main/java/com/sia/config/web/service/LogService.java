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

package com.sia.config.web.service;

import com.sia.config.web.filter.AuthInterceptor;
import com.sia.config.web.util.PageBean;
import com.sia.core.mapper.JobLogMapper;
import com.sia.core.web.vo.JobAndTaskLogVO;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Logging service class
 * Provides query operations on logs
 * @see
 * @author maozhengwei
 * @date 2018-04-18 11:21
 * @version V1.0.0
 **/
@Service
public class LogService {

    @Autowired
    private AuthInterceptor userService;

    @Autowired
    private JobLogMapper jobLogMapper;

    /**
     * Gets the JobLog and associated TaskLog,Supportting paging
     * @param jobGroup
     * @param jobKey
     * @return
     */
    public PageBean<JobAndTaskLogVO> selectJobLogAndTaskLogList(String jobGroup, String jobKey, int currentPage, int pageSize) {
        List<String> roleNames = userService.getCurrentUserRoles();
        Map<String, Object> param = new HashMap<>(4);
        param.put("jobGroup", jobGroup);
        param.put("jobKey", jobKey);
        param.put("roleNames", roleNames);
        PageHelper.startPage(currentPage, pageSize);
        //总记录
        List<JobAndTaskLogVO> jobLogs = jobLogMapper.selectJobLogAndTaskLogList(param);
        //总记录数
        int countJobLogs = jobLogMapper.selectCountJobLogs(param);
        PageBean<JobAndTaskLogVO> pageData = new PageBean<>(currentPage, pageSize, countJobLogs);
        pageData.setItems(jobLogs);
        return pageData;
    }

    /**
     * Gets the number of logs contained in each group
     * @return  the number of logs contained in each group
     */
    public List<Map<String,Integer>> selectCountGroupsJobLogs(String jobGroupName) {

        List<String> roleNames = userService.getCurrentUserRoles();
        Map<String, Object> param = new HashMap<>(4);
        param.put("roleNames", roleNames);
        param.put("jobGroup", jobGroupName);

        //jogLogsByGroups
        List<Map<String,Integer>> jobLogsByGroup = jobLogMapper.selectCountGroupsJobLogs(param);
        return jobLogsByGroup;
    }
}
