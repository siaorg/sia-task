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
import com.sia.task.core.task.DagTask;
import com.sia.task.core.util.DirectedCycleHelper;
import com.sia.task.core.util.JsonHelper;
import com.sia.task.core.util.StringHelper;
import com.sia.task.mapper.DagTaskMapper;
import com.sia.task.pojo.JobMTaskVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 任务编排管理
 *
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2020/4/27 12:42 下午
 * @see
 **/
@Slf4j
@Service
public class DagTaskService {

    @Resource
    private DagTaskMapper dagTaskMapper;

    /**
     * 查询JOB对应编排的Task
     *
     * @param jobGroup
     * @param jobKey
     * @return
     */
    public List<JobMTaskVO> selectTasksWithoutActuatorsInJob(String jobGroup, String jobKey) {
        return dagTaskMapper.selectDagTask4ContainsIP(jobGroup, jobKey);
    }

    /**
     * 画Task依赖图
     *
     * @param jobGroup
     * @param jobKey
     * @return
     * @throws Exception
     */
    public List<JobMTaskVO> selectDependencyGraph4Job(String jobGroup, String jobKey) {
        return analyticalTask(dagTaskMapper.selectDagTask4ContainsIP(jobGroup, jobKey));
    }

    /**
     * 设置任务编排关系
     *
     * @param dagTaskExts
     * @return
     */
    public boolean updateDagTask(List<DagTask> dagTaskExts) {
        String jobGroup = dagTaskExts.get(0).getJobGroup();
        String jobKey = dagTaskExts.get(0).getJobKey();
        try {
            //DAG Check
            List<String> list = DirectedCycleHelper.doDAGCheck4Pre(dagTaskExts);
            if (list != null) {
                log.info("dag check fail：error task {}", JsonHelper.toString(list));
                throw new SiaTaskException("dag check fail");
            }
            deleteJobMTask4Job(jobGroup, jobKey);
            return insertSelective(dagTaskExts) == dagTaskExts.size();
        } catch (Exception e) {
            throw new SiaTaskException("Error in update task DAG!", e);
        }
    }

    /**
     * 删除某个Job配置的task有向无环图信息
     *
     * @param jobGroup
     * @param jobKey
     * @return
     * @throws Exception
     */
    public int deleteJobMTask4Job(String jobGroup, String jobKey) {
        return dagTaskMapper.deleteByJobKeyAndJobGroup(jobGroup, jobKey);
    }

    /**
     * insertSelective
     * 配置某个job的task信息 待验证事务 todo
     *
     * @param dagTaskExts
     * @return
     */
    public int insertSelective(List<DagTask> dagTaskExts) {
        int count = 0;
        for (DagTask dagTaskExt : dagTaskExts) {
            count = count + dagTaskMapper.insertSelective(dagTaskExt);
        }
        return count;
    }

    /**
     * 查询对接项目总数和任务总数
     */
    public Map<String, String> getActuatorsAndJobCount() {
        return dagTaskMapper.selectActuatorsAndJobCount();
    }

    /**
     * 获取各个JobGroup下的job与task及联系人列表
     *
     * @return
     */
    public List<Map<String, Object>> selectJobGroupDetails() {
        List<Map<String, Object>> jobGroupDetails = dagTaskMapper.selectJobGroupDetails(UserService.getCurrentUserAllRoles());

        return jobGroupDetails;
    }

    private List<JobMTaskVO> analyticalTask(List<JobMTaskVO> jobMTaskList) {
        if (jobMTaskList.size() == 0) {
            return Collections.EMPTY_LIST;
        }
        Map<String, JobMTaskVO> onlineTasksMap = new HashMap<>(jobMTaskList.size());
        List<JobMTaskVO> startTaskLists = new ArrayList<>();
        for (JobMTaskVO onlineTask : jobMTaskList) {
            onlineTasksMap.put(onlineTask.getTaskKey(), onlineTask);
        }
        List<JobMTaskVO> result = new ArrayList<>();
        for (JobMTaskVO currentTask : jobMTaskList) {
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
        //设置深度
        Map<String, JobMTaskVO> stringJobMTaskVOMap = null;
        for (JobMTaskVO startTask : startTaskLists) {
            stringJobMTaskVOMap = setDepth(onlineTasksMap, startTask, 0);
        }
        //获取起始任务
        for (String key : stringJobMTaskVOMap.keySet()) {
            result.add(onlineTasksMap.get(key));
        }
        return result;
    }

    private Map<String, JobMTaskVO> setDepth(Map<String, JobMTaskVO> onlineTasksMap, JobMTaskVO startTask, int depth) {
        startTask.setDepth(depth);
        List<String> postTaskKey = startTask.getPostTaskKey();
        if (postTaskKey.size() > 0) {
            ++depth;
            for (String post : postTaskKey) {
                JobMTaskVO jobMTaskVO = onlineTasksMap.get(post);
                setDepth(onlineTasksMap, jobMTaskVO, depth);
            }
        }
        return onlineTasksMap;
    }
}
