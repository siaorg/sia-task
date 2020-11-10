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

import com.github.pagehelper.PageHelper;
import com.sia.task.admin.exception.SiaTaskException;
import com.sia.task.core.entity.BasicTask;
import com.sia.task.core.task.DagTask;
import com.sia.task.core.util.Constant;
import com.sia.task.core.util.NetHelper;
import com.sia.task.core.util.PageBean;
import com.sia.task.core.util.StringHelper;
import com.sia.task.integration.curator.Curator4Scheduler;
import com.sia.task.mapper.BasicTaskMapper;
import com.sia.task.pojo.IndexGroupVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Task 管理API service
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2020/4/26 7:02 下午
 **/
@Slf4j
@Service
public class BasicTask4Service {

    @Resource
    private BasicTaskMapper basicTaskMapper;
    @Resource
    private Curator4Scheduler curator4Scheduler;
    @Resource
    private RestTemplate restTemplate;

    /**
     * 根据角色对应组名进行权限过滤
     * selectGroupsByAuth
     *
     * @return
     */
    public List<String> selectGroupsByAuth() {
        return basicTaskMapper.selectGroupsByAuth(UserService.getCurrentUserAllRoles());
    }

    /**
     * 查询项目的应用名称
     *
     * @param groupName
     * @return
     */
    public List<String> selectAppsByGroup(String groupName) {
        return basicTaskMapper.selectAppsByGroup(groupName);
    }

    /**
     * 查询taskKey通过appName和groupName
     *
     * @param groupName
     * @param appName
     * @return
     */
    public List<String> selectTaskKeysByGroupAndApp(String groupName, String appName) {
        return basicTaskMapper.selectTaskKeysByGroupAndApp(groupName, appName);
    }

    /**
     * 检查网络连接
     *
     * @param host
     * @return
     */
    public boolean checkNetworkConnectivity(String host) {
        boolean result;
        String[] split = host.split(Constant.REGEX_COLON);
        switch (split.length) {
            case 1:
                result = NetHelper.ping(split[0]);
                break;
            case 2:
                result = NetHelper.ping(split[0]) && NetHelper.telnet(split[0], Integer.valueOf(split[1]));
                break;
            default:
                result = false;
        }
        return result;
    }

    /**
     * 查看Task被关联的Job
     *
     * @param taskKey
     * @return
     */
    public List<DagTask> selectTask4Job(String taskAppName, String taskGroupName, String taskKey) {
        return basicTaskMapper.selectTaskInJob(taskGroupName, taskAppName, taskKey);
    }


    /**
     * 查询task - 分页接口
     * <p>
     * 接口会返回task的执行器信息
     * 如果只需要task的基本信息，不要调用该接口，参考使用下面API
     * 入参规则请参看API
     *
     * @param taskAppName
     * @param taskGroupName
     * @param taskKey
     * @param currentPage
     * @param pageSize
     * @return PageBean
     * @see BasicTask4Service#selectTasksByCondition
     * @see BasicTask4Service#selectTasks4WithoutActuators(String, String, String)
     * </p>
     * TODO 存在性能问题
     */
    public PageBean<?> selectTasks4Page(String taskAppName, String taskGroupName, String taskKey, String taskDesc, int currentPage, int pageSize) throws Exception {
        PageHelper.startPage(currentPage, pageSize);
        List<BasicTask> basicTasks = selectTasksByCondition(taskAppName, taskGroupName, taskKey, taskDesc);
        PageBean<BasicTask> pageData = new PageBean<>(currentPage, pageSize, selectTaskCountByCondition(taskAppName, taskGroupName, taskKey, taskDesc));
        pageData.setItems(basicTasks);
        return pageData;
    }

    /**
     * 查询Task 无分页逻辑
     * <p>
     * 接口会返回task的执行器信息
     * 如果只需要task的基本信息，不要调用该接口，参考使用下面API
     * <code>BasicTask4Service#selectTasks4WithoutActuators</code>
     * </p>
     *
     * @param taskAppName   返回<code>taskAppName</code>内的所有task, 前提是没有指定taskKey
     * @param taskGroupName 返回<code>taskGroupName</code>内的所有Task,前提是没有指定<code>taskAppName</code>和<code>taskKey</code>
     * @param taskKey       返回匹配<code>taskKey</code>的task, 如果指定<code>taskKey</code>，其它参数就无意义。
     * @return
     * @throws Exception
     * @see BasicTask4Service#selectTasks4WithoutActuators
     */
    public List<BasicTask> selectTasksByCondition(String taskAppName, String taskGroupName, String taskKey, String taskDesc) throws Exception {
        List<BasicTask> basicTasks = basicTaskMapper.selectTasksByCondition(Arrays.asList(taskGroupName), taskAppName, taskKey, taskDesc);
        //封装来自抓取的Task的执行器实例
        for (BasicTask basicTask : basicTasks) {
            if (Constant.TASK_SOURCE_ZK.equals(basicTask.getTaskSource())) {
                List<String> executorList = curator4Scheduler.getExecutors(basicTask.getTaskGroupName(), basicTask.getTaskAppName(), basicTask.getTaskAppHttpPath());
                String executors = null;
                if (executorList != null) {
                    executors = String.join(Constant.REGEX_COMMA, executorList);
                }
                basicTask.setTaskAppIpPort(executors);
            }
        }
        return basicTasks;
    }

    /**
     * 查询Task 不返回执行器信息 withoutActuator
     *
     * @param taskAppName
     * @param taskGroupName
     * @param taskKey
     * @return
     * @throws Exception
     * @See BasicTask4Service#selectTasks
     */
    public Map<String, List<BasicTask>> selectTasks4WithoutActuators(String taskAppName, String taskGroupName, String taskKey) throws Exception {
        List<String> allRoles = UserService.getCurrentUserAllRoles();
        List<BasicTask> lists = basicTaskMapper.selectTasksByCondition(allRoles, taskAppName, taskKey, null);

        Map<String, List<BasicTask>> resultMap = new HashMap<>();
        if (lists != null && lists.size() > 0) {
            resultMap = lists.stream().collect(Collectors.groupingBy(BasicTask::getTaskGroupName));
        }
        return resultMap;
    }

    /**
     * 查询task的数量
     *
     * @param taskAppName   过滤条件：应用名称
     * @param taskGroupName 过滤条件：项目组名称
     * @param taskKey       过滤条件：任务唯一标识Key
     * @return
     * @see BasicTask4Service#selectTasks4WithoutActuators
     */
    public int selectTaskCountByCondition(String taskAppName, String taskGroupName, String taskKey, String taskDesc) {
        return basicTaskMapper.selectTaskCountByCondition(Arrays.asList(taskGroupName), taskAppName, taskKey, taskDesc);
    }

    /**
     * 任务管理界面得到项目名称和拥有的task数量 TODO 接口的本意是返回登录员工权限内的项目组Task数量
     *
     * @param taskGroupName
     * @return
     */
    public List<Map<String, Integer>> selectTaskCount4Group(String taskGroupName) {
        if (!StringHelper.isEmpty(taskGroupName)) {
            return basicTaskMapper.selectGroupAndCountI(Arrays.asList(taskGroupName));
        }
        return basicTaskMapper.selectGroupAndCountI(UserService.getCurrentUserAllRoles());
    }

    /**
     * 保存BasicTask
     *
     * @param basicTask
     * @return
     */
    public boolean saveBasicTask(BasicTask basicTask) {
        if (basicTask == null || StringHelper.isEmpty(basicTask.getTaskAppName())
                || StringHelper.isEmpty(basicTask.getTaskAppHttpPath())
                || StringHelper.isEmpty(basicTask.getTaskGroupName())) {
            throw new SiaTaskException("The parameters marked with * are required and cannot be empty");
        }
        if (!StringHelper.isGrammatical(basicTask.getTaskGroupName(), Constant.REGEX) || !StringHelper.isGrammatical(basicTask.getTaskAppName(), Constant.REGEX)) {
            throw new SiaTaskException("TaskKey can only contain numbers, letters, underscores and underscores");
        }
        basicTask.setTaskSource(Constant.TASK_SOURCE_UI);
        basicTask.setTaskKey(basicTask.getTaskAppName() + Constant.REGEX_COLON + basicTask.getTaskAppHttpPath());
        return basicTaskMapper.insertSelective(basicTask) == 1;
    }

    /**
     * 更新 BasicTask
     *
     * @param basicTask
     * @return
     */
    public boolean updateBasicTask(BasicTask basicTask) {
        if (basicTask == null || StringHelper.isEmpty(basicTask.getTaskAppName())
                || StringHelper.isEmpty(basicTask.getTaskAppHttpPath())
                || StringHelper.isEmpty(basicTask.getTaskGroupName())) {
            throw new SiaTaskException("The parameters marked with * are required and cannot be empty");
        }
        if (!StringHelper.isGrammatical(basicTask.getTaskGroupName(), Constant.REGEX) || !StringHelper.isGrammatical(basicTask.getTaskAppName(), Constant.REGEX)) {
            throw new SiaTaskException("TaskKey can only contain numbers, letters, underscores and underscores");
        }
        basicTask.setTaskSource(Constant.TASK_SOURCE_UI);
        basicTask.setTaskKey(basicTask.getTaskAppName() + Constant.REGEX_COLON + basicTask.getTaskAppHttpPath());
        return basicTaskMapper.updateByPrimaryKeySelective(basicTask) == 1;
    }

    /**
     * 删除Task
     * 需要注意，同步删除注册的Task元数据
     *
     * @param taskKey
     * @return
     */
    public boolean deleteBasicTask(String taskAppName, String taskGroupName, String taskKey) {
        if (selectTask4Job(taskAppName, taskGroupName, taskKey).size() > 0) {
            return false;
        }
        try {
            curator4Scheduler.deleteTaskKey(taskKey);
            return basicTaskMapper.deleteByPrimaryKey(taskGroupName, taskAppName, taskKey) == 1;
        } catch (Exception e) {
            log.info(Constant.LOG_EX_PREFIX + " [ " + UserService.getCurrentUser() + " ]; operation is: [ deleteBasicTask ],taskKey is " + taskKey);
        }
        return false;
    }

    /**
     * 获取指定Task的执行器实例列表
     *
     * @param basicTask
     * @return
     */
    public String getExecutors4Task(BasicTask basicTask) {
        String executors = null;
        if (Constant.TASK_SOURCE_ZK.equals(basicTask.getTaskSource())) {
            List<String> executorList = curator4Scheduler.getExecutors(basicTask.getTaskKey());
            if (executorList != null) {
                executors = String.join(Constant.REGEX_COMMA, executorList);
            }
        } else {
            BasicTask task = basicTaskMapper.selectTaskByTaskKey(basicTask.getTaskKey());
            if (task != null) {
                executors = task.getTaskAppIpPort();
            }
        }
        return executors;
    }

    /**
     * 连通性测试
     */
    public String connectivityTest4Task(Map<String, String> request) {
        String post;
        try {
            post = restTemplate.postForObject(request.get("url"), request.get("param"), String.class);
        } catch (Exception e) {
            log.error(Constant.LOG_EX_PREFIX + "connectivityTest4Task - {}", request, e);
            post = e.toString();
        }
        return post;
    }

    /**
     * 返回导航页项目列表
     *
     * @return 项目列表 + appSize
     */
    public List<IndexGroupVo> getGroupList() {
        return basicTaskMapper.getGroupList(UserService.getCurrentUserAllRoles());
    }
}
