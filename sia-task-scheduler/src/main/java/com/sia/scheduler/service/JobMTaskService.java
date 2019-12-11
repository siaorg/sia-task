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

package com.sia.scheduler.service;

import com.sia.core.entity.JobMTask;
import com.sia.core.mapper.JobMTaskMapper;
import com.sia.scheduler.util.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JobMTaskService
 *
 * JOB_TASK mapping relationship
 *
 * @see
 * @author maozhengwei
 * @date 2018-04-18 11:21
 * @version V1.0.0
 **/
@Service
public class JobMTaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobMTaskService.class);

    @Autowired
    private JobMTaskMapper jobMTaskMapper;

    /**
     * 查询Job的task配置信息
     * @Cacheable(value = "taskList",key = "'taskList' + #jobGroup + #jobKey")
     * @param jobGroup
     * @param jobKey
     * @return
     */
    @Cacheable(value = "taskList", key = "#jobKey + #jobGroup")
    public List<JobMTask> selectJobMTask(String jobGroup, String jobKey) {
        LOGGER.info(Constants.LOG_PREFIX + " load JobMTask data from database, jobKey={}", jobKey);
        if (jobKey == null) {
            LOGGER.warn(Constants.LOG_PREFIX + " select JobMTask fail, jobKey invalid, mappingId={}", jobKey);
            return null;
        }
        Map<String, String> param = new HashMap<>(2);
        param.put("jobGroup", jobGroup);
        param.put("jobKey", jobKey);
        List<JobMTask> jobMTaskList = null;
        try{
            jobMTaskList = jobMTaskMapper.selectTaskMJobAndIPListByJobGroupAndKey(param);
        }catch(Exception e) {
            LOGGER.error(Constants.LOG_EX_PREFIX + " selectTaskMJobAndIPListByJobGroupAndKey 数据库查询操作异常", e);
        }
        return jobMTaskList;
    }

    /**
     * 清除缓存信息
     * @CacheEvict(value = "taskList", key = "'taskList' + #jobGroup + #jobKey")
     * @param jobGroup
     * @param jobKey
     */
    @CacheEvict(value = "taskList", key = "#jobKey + #jobGroup")
    public void cleanTasksCache(String jobGroup, String jobKey){
        LOGGER.info(Constants.LOG_PREFIX + " cleanTasksCache : clean data from Cache, jobKey={}", jobKey);
    }

}
