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
import com.sia.core.entity.BasicTask;
import com.sia.core.entity.JobMTask;
import com.sia.core.mapper.BasicTaskMapper;
import com.sia.config.web.constants.Constants;
import com.sia.config.web.util.PageBean;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Task operates on logical classes
 * @see
 * @author maozhengwei
 * @date 2018-04-18 15:40
 * @version V1.0.0
 **/
@Service
public class BasicTaskService {

    public static final Logger LOGGER = LoggerFactory.getLogger(BasicTaskService.class);

    @Autowired
    private BasicTaskMapper basicTaskMapper;

    @Autowired
    private Curator4Scheduler curator4Scheduler;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Query owned groups by roles
     * @param roleNames
     * @return
     * @throws Exception
     */
    public List<String> selectGroupsByAuth(List<String> roleNames) {
        return basicTaskMapper.selectGroupsByAuth(roleNames);
    }

    /**
     * Query the application under the group according to the group
     * @param groupName
     * @return
     */
    public List<String> selectAppsByGroup(String groupName) {
        return basicTaskMapper.selectAppsByGroup(groupName);
    }

    /**
     * Query taskKey by Group and App
     * @param groupName
     * @param appName
     * @return
     */
    public List<String> selectTaskKeysByGroupAndApp(String groupName, String appName) {
        return basicTaskMapper.selectTaskKeysByGroupAndApp(groupName,appName);
    }


    /**
     * Delete tasks by taskKey
     * @param taskAppName
     * @param taskGroupName
     * @param taskKey
     * @return
     * @throws Exception
     */
    public int deleteByPrimaryKey(String taskAppName, String taskGroupName, String taskKey) {
        Map<String, String> param = new HashMap<>(4);
        param.put("taskAppName", taskAppName);
        param.put("taskGroupName", taskGroupName);
        param.put("taskKey", taskKey);
        return basicTaskMapper.deleteByPrimaryKey(param);
    }

    /**
     * insert task
     * @param basicTask
     * @return
     * @throws Exception
     */
    public int insertSelective(BasicTask basicTask) {

        return basicTaskMapper.insertSelective(basicTask);
    }

    /**
     * Query tasks do not provide paging
     *
     * @param taskAppName
     * @param taskGroupName
     * @param taskKey
     * @return
     * @throws Exception
     */
    public List<BasicTask> selectTasksByAppNameOrGroupName(String taskAppName, String taskGroupName, String taskKey, List<String> roleNames) throws Exception {
        Map<String, Object> param = new HashMap<>(8);
        param.put("taskAppName", taskAppName);
        param.put("taskGroupName", taskGroupName);
        param.put("taskKey", taskKey);
        param.put("roleNames", roleNames);
        //总记录
        List<BasicTask> basicTasks = basicTaskMapper.selectTasksByAppNameOrGroupNameL(param);
        //封装来自抓取的Task的执行器实例
        for (BasicTask basicTask : basicTasks) {
            if (Constants.TASK_SOURCE_ZK.equals(basicTask.getTaskSource())) {
                List<String> executorList = curator4Scheduler.getExecutors(basicTask.getTaskGroupName(), basicTask.getTaskAppName(), basicTask.getTaskAppHttpPath());
                String executors = null;
                if (executorList != null) {
                    executors = String.join(Constants.REGEX_COMMA, executorList);
                }
                basicTask.setTaskAppIpPort(executors);
            }
        }
        return basicTasks;
    }

    /**
     * Query Task provides paging
     *
     * @param taskAppName
     * @param taskGroupName
     * @param taskKey
     * @param currentPage
     * @param pageSize
     * @return
     * @throws Exception
     */
    public PageBean<BasicTask> selectTasksByAppNameOrGroupName(String taskAppName, String taskGroupName, String taskKey, int currentPage, int pageSize,List<String> roleNames) throws Exception {
        PageHelper.startPage(currentPage, pageSize);
        List<BasicTask> basicTasks = selectTasksByAppNameOrGroupName(taskAppName, taskGroupName, taskKey,roleNames);
        //总记录数
        Map<String, Object> param = new HashMap<>(8);
        param.put("taskAppName", taskAppName);
        param.put("taskGroupName", taskGroupName);
        param.put("taskKey", taskKey);
        param.put("roleNames", roleNames);
        int countBasicTasks = basicTaskMapper.selectCountbasicTasks(param);
        PageBean<BasicTask> pageData = new PageBean<>(currentPage, pageSize, countBasicTasks);
        pageData.setItems(basicTasks);
        return pageData;
    }

    /**
     * update Task
     *
     * @param basicTask
     * @return
     * @throws Exception
     */
    public int updateByPrimaryKeySelective(BasicTask basicTask) {

        return basicTaskMapper.updateByPrimaryKeySelective(basicTask);
    }

    /**
     * Insert or update Task
     * @param basicTask
     * @return
     */
    public int  insertOrUpdateByTaskKey(BasicTask basicTask) {
        return basicTaskMapper.insertOrUpdateByTaskKey(basicTask);
    }

    /**
     * query Task
     * @param taskAppName
     * @param taskGroupName
     * @param taskKey
     * @return
     */
    public List<JobMTask> selectTaskInJob(String taskAppName, String taskGroupName, String taskKey) {
        Map<String, Object> param = new HashMap<>(4);
        param.put("taskAppName", taskAppName);
        param.put("taskGroupName", taskGroupName);
        param.put("taskKey", taskKey);
        return basicTaskMapper.selectTaskInJob(param);
    }

    /**
     * Task management interface project name and task number query
     * @param roleNames
     * @param taskGroupName
     * @return
     */
    public List<Map<String, Integer>> selectGroupAndCount(List<String> roleNames, String taskGroupName){
        Map<String, Object> param  = new HashMap<>(4);
        param.put("taskGroupName", taskGroupName);
        param.put("roleNames", roleNames);
        return basicTaskMapper.selectGroupAndCountI(param);
    }

    /**
     * Get the list of executor instances based on basicTask
     * @param basicTask
     * @return
     */
    public String getExecutorList(BasicTask basicTask) {
        String executors = null;
        if (Constants.TASK_SOURCE_ZK.equals(basicTask.getTaskSource())) {
            List<String> executorList = curator4Scheduler.getExecutors(basicTask.getTaskKey());
            if (executorList != null) {
                executors = String.join(Constants.REGEX_COMMA, executorList);
            }
        }
        else{
            Map<String, Object> param = new HashMap<>(4);
            param.put("taskKey", basicTask.getTaskKey());
            List<BasicTask> taskList = basicTaskMapper.selectTaskByTaskKey(param);
            if(taskList!=null&&taskList.size()>0) {
                executors = taskList.get(0).getTaskAppIpPort();
            }
        }
        return executors;
    }


    /**
     * Query tasks do not provide paging
     *
     * @param taskAppName
     * @param taskGroupName
     * @param taskKey
     * @return
     * @throws Exception
     */
    public List<BasicTask> selectTasksByAppNameOrGroupNameNoIpPort(String taskAppName, String taskGroupName, String taskKey, List<String> roleNames) throws Exception {
        Map<String, Object> param = new HashMap<>(8);
        param.put("taskAppName", taskAppName);
        param.put("taskGroupName", taskGroupName);
        param.put("taskKey", taskKey);
        param.put("roleNames", roleNames);
        //总记录
        return basicTaskMapper.selectTasksByAppNameOrGroupNameL(param);
    }

    /**
     * Task connectivity test
     * @param url
     * @param param
     * @return
     * @throws Exception
     */
    public String testTask(String url, String param) {
        String resp;
        try {
            resp = restTemplate.postForObject(url, param, String.class);
            LOGGER.info(Constants.LOG_PREFIX + " testTask rst:" + resp);
            return resp;

        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " Exception:"+e.getCause());
            return " Exception:" + e.getMessage();
        }

    }
}
