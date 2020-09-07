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
import com.sia.task.admin.vo.SiaResponseBody;
import com.sia.task.core.entity.BasicJob;
import com.sia.task.core.entity.JobPortrait;
import com.sia.task.core.task.DagTask;
import com.sia.task.core.task.SiaJobStatus;
import com.sia.task.core.util.Constant;
import com.sia.task.core.util.JsonHelper;
import com.sia.task.core.util.PageBean;
import com.sia.task.core.util.StringHelper;
import com.sia.task.integration.curator.Curator4Scheduler;
import com.sia.task.integration.curator.properties.ZookeeperConstant;
import com.sia.task.mapper.BasicJobMapper;
import com.sia.task.mapper.BasicTaskMapper;
import com.sia.task.mapper.DagTaskMapper;
import com.sia.task.quartz.core.CronExpression;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <code>BasicJob</code> service
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2020/4/25 6:09 下午
 **/
@Slf4j
@Service
public class BasicJob4Service {

    @Resource
    protected Curator4Scheduler curator4Scheduler;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private BasicJobMapper basicJobMapper;
    @Resource
    private BasicTaskMapper basicTaskMapper;
    @Resource
    DagTaskMapper dagTaskMapper;

    public boolean isValidExpression(String cronExpress) {
        if (StringHelper.isEmpty(cronExpress)) {
            return false;
        }
        return CronExpression.isValidExpression(cronExpress);
    }

    /**
     * 获取JobKey的执行状态
     *
     * @param jobGroup
     * @param jobKey
     * @return
     */
    public String selectJobStatus(String jobGroup, String jobKey) {
        String jobStatus = curator4Scheduler.getJobStatus(jobGroup, jobKey);
        log.info(Constant.LOG_PREFIX + " selectJobStatus : jobStatus is {}, jobKey is {}", jobStatus, jobKey);
        return jobStatus;
    }

    /**
     * TODO curator4Scheduler 去除依赖
     * 激活Job
     *
     * @param jobGroup
     * @param jobKey
     * @return
     */
    public String activateJob(String jobGroup, String jobKey) {
        List<DagTask> jobMTaskExt = dagTaskMapper.selectByJobGroupAndKey(jobGroup, jobKey);
        if (jobMTaskExt == null || jobMTaskExt.size() == 0) {
            return SiaResponseBody.failure(null, SiaResponseBody.ResponseCodeEnum.JOB_NO_TASK_CONFIG.getMessage());
        }
        if (curator4Scheduler.createJobKey(jobGroup, jobKey) && curator4Scheduler.casJobStatus4User(jobGroup, jobKey, SiaJobStatus.STOP.toString(), SiaJobStatus.READY.toString())) {
            return SiaResponseBody.success();
        }
        return SiaResponseBody.failure();
    }

    /**
     * TODO curator4Scheduler 去除依赖
     * 激活Job
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    public boolean stop(String jobGroupName, String jobKey) {
        return curator4Scheduler.deleteJobKey(jobGroupName, jobKey);
    }

    /**
     * 根据当前登录用户的权限集返回权限内容Job组集合
     *
     * @return
     */
    @Deprecated
    public Map<String, List<String>> selectGroupByAuth() {
        List<BasicJob> basicJobs = basicJobMapper.selectGroupByAuth(UserService.getCurrentUserAllRoles());
        Map<String, List<String>> tm = new HashMap<>();
        for (BasicJob basicJob : basicJobs) {
            String jobGroupName = basicJob.getJobGroup();
            String jobKey = basicJob.getJobKey();
            if (tm.containsKey(jobGroupName)) {
                tm.get(jobGroupName).add(jobKey);
            } else {
                List<String> keyList = new ArrayList<>();
                keyList.add(jobKey);
                tm.put(jobGroupName, keyList);
            }
        }
        return tm;
    }

    /**
     * 通过指定的过滤条件查询Job
     *
     * @param jobGroup    项目组名称
     * @param jobKey      唯一键
     * @param currentPage
     * @param pageSize
     * @return
     */
    public PageBean<?> selectJobsByCondition(String jobGroup, String jobKey, String jobDesc, int currentPage, int pageSize) {
        PageHelper.startPage(currentPage, pageSize);
        //总记录
        List<BasicJob> basicJobs = basicJobMapper.selectJobsByCondition(Arrays.asList(jobGroup), jobKey, jobDesc);
        for (BasicJob basicJob : basicJobs) {
            List<String> jobScheduler = curator4Scheduler.getJobScheduler(basicJob.getJobGroup(), basicJob.getJobKey());
            basicJob.setTriggerInstance(StringHelper.join(jobScheduler, ","));
        }
        //总记录数
        int countBasicJobs = basicJobMapper.selectJobCountByCondition(jobGroup, jobKey, jobDesc);
        PageBean<BasicJob> pageData = new PageBean<>(currentPage, pageSize, countBasicJobs);
        pageData.setItems(basicJobs);
        return pageData;
    }

    /**
     * 获取JobGroup及其对用JobGroup的Job个数
     *
     * @param jobGroup
     * @return
     */
    public List<Map<String, Integer>> selectGroupAndJobCount(String jobGroup) {
        if (!StringHelper.isEmpty(jobGroup)) {
            return basicJobMapper.selectJopCount4Group(Arrays.asList(jobGroup));
        }
        return basicJobMapper.selectJopCount4Group(UserService.getCurrentUserAllRoles());
    }

    /**
     * 存储Job
     *
     * @param basicJob
     * @return
     */
    public int insertJob(BasicJob basicJob) {
        setEmail(basicJob);
        return basicJobMapper.insertSelective(basicJob);
    }

    /**
     * updateByPrimaryKey
     *
     * @param basicJob
     * @return
     */
    public boolean updateJob(BasicJob basicJob) {
        if (StringHelper.isEmpty(curator4Scheduler.getJobStatus(basicJob.getJobGroup(), basicJob.getJobKey()))) {
            setEmail(basicJob);
            return basicJobMapper.updateByPrimaryKey(basicJob) == 1;
        }
        return false;
    }

    /**
     * 删除Job
     *
     * @param jobGroup
     * @param jobKey
     * @return
     */
    public boolean deleteJob(String jobGroup, String jobKey) {
        curator4Scheduler.deleteJobKey(jobGroup, jobKey);
        dagTaskMapper.deleteByJobKeyAndJobGroup(jobGroup, jobKey);
        return basicJobMapper.deleteByJobKeyAndJobGroup(jobGroup, jobKey) == 1;
    }

    /**
     * 获取调度监控中任务概览
     */
    public Map<String, Integer> selectTaskMonitorView() {
        Map<String, Integer> jobStatusSum = new HashMap<>();
        jobStatusSum.put("ready", 0);
        jobStatusSum.put("running", 0);
        jobStatusSum.put("stop", 0);
        jobStatusSum.put("expStop", 0);
        List<BasicJob> jobs = basicJobMapper.selectJobsByCondition(UserService.getCurrentUserAllRoles(), null, null);
        setTrigger4Jobs(jobs);

        long stopNum = jobs.stream().filter(basicJob -> basicJob.getJobDesc() == null).count();
        jobStatusSum.put("stop", (int) stopNum);

        long expNum = jobs.stream().filter(basicJob -> "stop".equals(basicJob.getJobDesc())).count();
        jobStatusSum.put("expStop", (int) expNum);

        long runNum = jobs.stream().filter(basicJob -> "running".equals(basicJob.getJobDesc())).count();
        jobStatusSum.put("running", (int) runNum);

        long readyNum = jobs.stream().filter(basicJob -> "ready".equals(basicJob.getJobDesc())).count();
        jobStatusSum.put("ready", (int) readyNum);

        return jobStatusSum;
    }

    /**
     * 获取调度监控项目数量、任务数量、JOB数量
     */
    public Map<String, Integer> selectSummary() {
        Map<String, Integer> pjtNum = new HashMap<>(4);
        pjtNum.put("projectNum", 0);
        pjtNum.put("jobNum", 0);
        pjtNum.put("taskNum", 0);
        List<BasicJob> jobs = basicJobMapper.selectJobsByCondition(UserService.getCurrentUserAllRoles(), null, null);
        int countBasicTasks = basicTaskMapper.selectTaskCountByCondition(UserService.getCurrentUserAllRoles(), null, null, null);
        long jobGroupCount = jobs.stream().map(basicJob -> basicJob.getJobGroup()).distinct().count();
        pjtNum.put("projectNum", (int) jobGroupCount);
        pjtNum.put("jobNum", jobs.size());
        pjtNum.put("taskNum", countBasicTasks);

        return pjtNum;
    }

    /**
     * 调度监控页面按项目名称筛选
     */
    public List<JobPortrait> selectJobGroupPortrait(String jobGroup) {
        List<JobPortrait> portraitVoList = new ArrayList<>();
        List<BasicJob> jobs = basicJobMapper.selectJobsByCondition(UserService.getCurrentUserAllRoles(), null, null);
        List<String> groups = jobs.stream().map(basicJob -> basicJob.getJobGroup()).distinct().collect(Collectors.toList());
        if (!StringHelper.isEmpty(jobGroup)) {
            groups.clear();
            groups.add(jobGroup);
        }

        groups.forEach(group -> {
            Map<String, Integer> total = new HashMap<>(4);
            total.put("sum", 0);
            total.put("activated", 0);
            total.put("stop", 0);

            List<BasicJob> gJobs = jobs.stream().filter(basicJob -> basicJob.getJobGroup().equals(group)).collect(Collectors.toList());
            setTrigger4Jobs(gJobs);

            long activatedNum = gJobs.stream().filter(basicJob -> basicJob.getJobDesc() != null).count();
            total.put("activated", (int) activatedNum);
            long expStopNum = gJobs.stream().filter(basicJob -> "stop".equals(basicJob.getJobDesc())).count();
            total.put("stop", (int) expStopNum);
            total.put("sum", gJobs.size());

            //总记录数
            JobPortrait portrait = new JobPortrait();
            portrait.setPortrait(gJobs);
            portrait.setTotal(total);
            portrait.setGroup(group);
            portraitVoList.add(portrait);
        });
        return portraitVoList;
    }

    /**
     * 获取Scheduler执行的job列表
     *
     * @param scheduler
     * @return
     */
    public List<BasicJob> selectJobs4Scheduler(String scheduler) {
        List<String> jobKeyList = new ArrayList<>();
        String path = new StringBuilder().append(ZookeeperConstant.ZK_ROOT).append(curator4Scheduler.getTaskRoot()).append(ZookeeperConstant.ZK_SEPARATOR).append(ZookeeperConstant.ZK_ONLINE_JOB).toString();
        List<String> jobGroupNames = curator4Scheduler.getCuratorClient().getChildren(path);
        for (String jobGroupName : jobGroupNames) {
            List<String> jobKeys = curator4Scheduler.getJobKeys(jobGroupName);
            for (String jobKey : jobKeys) {
                List<String> jobSchedulerList = curator4Scheduler.getJobScheduler(jobGroupName, jobKey);
                if (jobSchedulerList.contains(scheduler)) {
                    jobKeyList.add(jobKey);
                }
            }
        }
        List<BasicJob> basicJobs = null;
        if (jobKeyList.size() > 0) {
            basicJobs = basicJobMapper.selectByJobKeyList(jobKeyList);
        }
        return basicJobs;
    }

    /**
     * 执行一次
     * 通过触发注册中心节点添加事件触发实现
     *
     * @param jobGroup
     * @param jobKey
     * @return
     */
    public boolean runOnce4Zk(String jobGroup, String jobKey) {
        log.info(Constant.LOG_PREFIX + " runOnce : {}，{}", jobGroup, jobKey);
        if (!StringHelper.isEmpty(jobKey)) {
            boolean deleteRunOnceJobKey = curator4Scheduler.deleteRunOnceJobKey(jobKey);
            log.info(Constant.LOG_PREFIX + " runOnce - jobKey:[{}]，deleteRunOnceJobKey:[{}]", jobKey, deleteRunOnceJobKey);
            return curator4Scheduler.createRunOnceJobKey(jobKey);
        }
        return false;
    }

    public String updateJobPlan(BasicJob basicJob) {
        if (!StringHelper.isEmpty(curator4Scheduler.getJobStatus(basicJob.getJobGroup(), basicJob.getJobKey()))) {
            return SiaResponseBody.failure(null, "Job 已激活，请关闭Job 再进行配置！");
        }
        try {
            //basicJob.getJobPlan() = null ? 删除/新增 : 更新
            if (StringHelper.isEmpty(basicJob.getJobPlan())) {
                BasicJob jobChild = basicJob.getJobChild();
                if (jobChild != null) {
                    if (!StringHelper.isEmpty(jobChild.getJobPlan())) {
                        return SiaResponseBody.failure(null, "后置Job[" + jobChild.getJobKey() + "] 已被其它Job关联！！");
                    }
                    //新增
                    updateJobPlanByJobKey(basicJob);
                    log.info(Constant.LOG_PREFIX + "username is: " + UserService.getCurrentUser() + "; operation is:updateJobPlan [add],jobKey is; " + basicJob.getJobKey());
                } else {
                    //删除
                    removeJobPlan(basicJob);
                    log.info(Constant.LOG_PREFIX + "username is: " + UserService.getCurrentUser() + "; operation is:updateJobPlan [remove],jobKey is; " + basicJob.getJobKey());
                }
            } else {
                //更新
                removeJobPlan(basicJob);
                updateJobPlanByJobKey(basicJob);
                log.info(Constant.LOG_PREFIX + "username is: " + UserService.getCurrentUser() + "; operation is:updateJobPlan [update],jobKey is; " + basicJob.getJobKey());
            }
            return SiaResponseBody.success(null, "Job 级联配置成功！");
        } catch (Exception e) {
            log.error(Constant.LOG_PREFIX + " updateJobPlan job fail, job is {}", JsonHelper.toString(basicJob));
            log.error(" updateJobPlan Exception:", e);
            return SiaResponseBody.failure();
        }
    }

    private int updateJobPlanByJobKey(BasicJob basicJob) throws Exception {
        try {
            BasicJob jobChild = basicJob.getJobChild();
            if (null == jobChild) {
                BasicJob oldChild = basicJobMapper.selectJob4Childs(basicJob.getJobKey());
                oldChild.setJobPlan(null);
                oldChild.setJobParentKey(null);
                basicJobMapper.updateByPrimaryKey(oldChild);
                basicJob.setJobPlan(null);
                return basicJobMapper.updateByPrimaryKey(basicJob);
            } else {
                basicJob.setJobPlan(basicJob.getJobKey());
                basicJobMapper.updateByPrimaryKey(basicJob);
                jobChild.setJobParentKey(basicJob.getJobKey());
                jobChild.setJobPlan(basicJob.getJobKey());
                return basicJobMapper.updateByPrimaryKey(jobChild);
            }
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    /**
     * 清除级联关系
     *
     * @param basicJob
     */
    private void removeJobPlan(BasicJob basicJob) {
        BasicJob jobOld = basicJobMapper.selectJob(basicJob.getJobGroup(), basicJob.getJobKey());
        List<BasicJob> basicJobs = basicJobMapper.selectJobsByPlan(jobOld.getJobPlan());
        basicJobs.forEach(job -> {
            job.setJobPlan(null);
            job.setJobParentKey(null);
            setEmail(basicJob);
            basicJobMapper.updateByPrimaryKey(basicJob);
        });
    }

    /**
     * 增加存在zookeeper上面的节点信息
     *
     * @param basicJobs
     */
    //TODO 存在性能问题
    private void setTrigger4Jobs(List<BasicJob> basicJobs) {
        basicJobs.forEach(basicJob -> {
            List<String> jobScheduler = curator4Scheduler.getJobScheduler(basicJob.getJobGroup(), basicJob.getJobKey());
            basicJob.setTriggerInstance(StringHelper.join(jobScheduler, ","));
            String jobStatus = curator4Scheduler.getJobStatus(basicJob.getJobGroup(), basicJob.getJobKey());
            basicJob.setJobDesc(jobStatus);
        });
    }

    private void setEmail(BasicJob basicJob) {
        String jobAlarmEmail = basicJob.getJobAlarmEmail();
        basicJob.setJobAlarmEmail(jobAlarmEmail);
    }
}
