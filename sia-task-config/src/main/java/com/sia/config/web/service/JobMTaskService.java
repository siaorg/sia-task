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


import com.sia.core.curator.Curator4Scheduler;
import com.sia.core.entity.JobMTask;
import com.sia.core.helper.ListHelper;
import com.sia.core.helper.StringHelper;
import com.sia.core.mapper.JobMTaskMapper;
import com.sia.core.web.vo.JobMTaskVO;
import com.sia.config.web.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JOB_TASK mapping relationship
 * @see
 * @author maozhengwei
 * @date 2018-04-18 11:10
 * @version V1.0.0
 **/
@Service
public class JobMTaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobMTaskService.class);
    @Autowired
    private JobMTaskMapper jobMTaskMapper;

    @Autowired
    private Curator4Scheduler curator4Scheduler;

    @Autowired
    private RegistryService registryService;

    /**
     * Delete task information for a Job configuration
     * @param jobGroup
     * @param jobKey
     * @return
     * @throws Exception
     */
    public int deleteByJobKeyAndJobGroup(String jobGroup, String jobKey) {
        Map<String, String> param = new HashMap<>(4);
        param.put("jobGroup", jobGroup);
        param.put("jobKey", jobKey);
        return jobMTaskMapper.deleteByJobKeyAndJobGroup(param);
    }

    /**
     * Configure task information for a job
     * Transaction to be verified todo
     * @param jobMTaskList
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    public int insertSelective(List<JobMTask> jobMTaskList) {
        int count = 0;
        if (jobMTaskList == null || jobMTaskList.contains(null)) {
            LOGGER.warn(Constants.LOG_PREFIX + " insert JobMTask fail, jobMTask invalid, jobMTaskList is null or contains null");
        }else {
            for (JobMTask jobMTask : jobMTaskList) {
                count = count + jobMTaskMapper.insertSelective(jobMTask);
            }
        }
        return count;
    }

    /**
     * Query task information for a job configuration
     * @param jobGroup
     * @param jobKey
     * @return
     * @throws Exception
     */
    public List<JobMTask> selectByJobGroupAndKey(String jobGroup, String jobKey) {
        Map<String, String> param = new HashMap<>(4);
        param.put("jobGroup", jobGroup);
        param.put("jobKey", jobKey);
        List<JobMTask> jobMTasks = jobMTaskMapper.selectByJobGroupAndKey(param);
        return jobMTasks;
    }

    /**
     * The Job is queried by the JOB (jobGroup,jobKey) to correspond to the choreographed Task
     * @param jobGroup
     * @param jobKey
     * @return
     */
    public List<JobMTaskVO> selectTaskMJobAndIPListByJobGroupAndKey(String jobGroup, String jobKey) {
        List<JobMTaskVO> jobMTasks = null;
        if (StringHelper.isEmpty(jobKey)||StringHelper.isEmpty(jobGroup)) {
            LOGGER.warn(Constants.LOG_PREFIX + " select JobMTask fail, jobKey invalid, jobKey={}", jobKey);
            return jobMTasks;
        }
        Map<String, String> param = new HashMap<>(4);
        param.put("jobGroup", jobGroup);
        param.put("jobKey", jobKey);
        jobMTasks = jobMTaskMapper.selectTaskMJobAndIPListByJobGroupAndKeyVO(param);
        for (JobMTaskVO jobMTask : jobMTasks) {
            List<String> addressList;
            List<String> executorsFromZk = registryService.getExecutors(jobMTask.getTaskKey());
            List<String> executorsFromDB = null;
            if (!StringHelper.isEmpty(jobMTask.getIpAndPortList())) {
                executorsFromDB = Arrays.asList(jobMTask.getIpAndPortList().split(Constants.REGEX_COMMA));
            }
            addressList = ListHelper.mergeList(executorsFromZk, executorsFromDB);
            if (addressList != null && addressList.size() > 0) {
                String ipPort = String.join(Constants.REGEX_COMMA, addressList);
                jobMTask.setIpAndPortList(ipPort);
            }
        }
        return jobMTasks;
    }

    /**
     * Query whether a Task is referenced by a Job
     *
     * @param taskKey
     * @return Returns a collection of referenced jobmtasks
     */
    public List<JobMTask> selectJobMTaskTaskKey(String taskKey) {
        return jobMTaskMapper.selectJobMTaskTaskKey(taskKey);
    }

    /**
     * Query the total number of docking projects and tasks
     * @return
     */
    public  Map<String,String> getActuatorsAndJobCount() {
        return jobMTaskMapper.selectActuatorsAndJobCount();
    }

    /**
     * Analyze the list of tasks associated with a Job
     * @return
     */
    public List<JobMTaskVO> analyticalTask(List<JobMTaskVO> jobMTaskList) {
        Map<String, JobMTaskVO> onlineTasksMap = new HashMap<>(jobMTaskList.size());
        List<JobMTaskVO> startTaskLists = new ArrayList<>();
        for (JobMTaskVO onlineTask : jobMTaskList) {
            onlineTasksMap.put(onlineTask.getTaskKey(), onlineTask);
        }
        List<JobMTaskVO> result = new ArrayList<>();
        for (JobMTaskVO currentTask :jobMTaskList) {
            List<String> preTaskKeyList = StringHelper.isEmpty(currentTask.getPreTaskKey()) ? Collections.emptyList() : Arrays.asList(currentTask.getPreTaskKey().split(","));
            if (preTaskKeyList.size() == 0) {
                JobMTaskVO curr = onlineTasksMap.get(currentTask.getTaskKey());
                startTaskLists.add(curr);
                continue;
            }
            for (String preTaskKey : preTaskKeyList) {
                JobMTaskVO curr = onlineTasksMap.get(currentTask.getTaskKey());
                JobMTaskVO pre = onlineTasksMap.get(preTaskKey);
                curr.getPreTaskCounter().getAndIncrement();
                pre.getPostTaskKey().add(currentTask.getTaskKey());
            }
        }

        //Set the depth
        Map<String, JobMTaskVO> stringJobMTaskVOMap = null;
        for (JobMTaskVO startTask : startTaskLists) {
           stringJobMTaskVOMap = setDepth(onlineTasksMap, startTask, 0);
        }
        //Get start task
        for (String key : stringJobMTaskVOMap.keySet()) {
            result.add(onlineTasksMap.get(key));
        }
        return result;
    }

    private Map<String, JobMTaskVO> setDepth(Map<String, JobMTaskVO> onlineTasksMap, JobMTaskVO startTask, int depth){

        startTask.setDepth(depth);
        List<String> postTaskKey = startTask.getPostTaskKey();
        if (postTaskKey.size() > 0) {
            ++depth;
            for (String post : postTaskKey) {
                JobMTaskVO jobMTaskVO = onlineTasksMap.get(post);
                setDepth( onlineTasksMap, jobMTaskVO, depth);
            }
        }

        return onlineTasksMap;
    }

    /**
     * Get the job and task and contact list under each JobGroup
     * @param roleNames
     * @return
     */
    public List<Map<String, Object>> selectJobGroupDetails(List<String> roleNames) {

        List<Map<String, Object>>  jobGroupDetails;
        Map<String, Object> param = new HashMap<>(4);
        param.put("roleNames", roleNames);
        jobGroupDetails = jobMTaskMapper.selectJobGroupDetails(param);

        jobGroupDetails.stream().filter(jobGroup->jobGroup.get("Emails") != null).forEach(jobGroup->{
            String emails = Arrays.stream(((String) jobGroup.get("Emails")).split(",")).map(email->email.toLowerCase().endsWith("@********.cn") ? email : email + "@********.cn") .distinct().collect(Collectors.joining(","));
            jobGroup.put("Emails", emails);
        });

        return jobGroupDetails;
    }
}
