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

import com.github.pagehelper.PageHelper;
import com.sia.config.web.constants.Constants;
import com.sia.config.web.filter.AuthInterceptor;
import com.sia.config.web.util.PageBean;
import com.sia.config.web.vo.JobPortrait;
import com.sia.core.constant.Constant;
import com.sia.core.curator.Curator4Scheduler;
import com.sia.core.entity.BasicJob;
import com.sia.core.helper.JSONHelper;
import com.sia.core.helper.StringHelper;
import com.sia.core.mapper.BasicJobMapper;
import com.sia.core.mapper.BasicTaskMapper;
import com.sia.core.status.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The logical operation class for jobs
 * @see
 * @author maozhengwei
 * @date 2019-04-28 15:40
 * @version V1.0.0
 **/
@Service
public class BasicJobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicJobService.class);

    @Autowired
    private BasicJobMapper basicJobMapper;

    @Autowired
    private BasicTaskMapper basicTaskMapper;

    @Autowired
    private JobMTaskService jobMTaskService;

    @Autowired
    private Curator4Scheduler curator4Scheduler;

    @Autowired
    protected AuthInterceptor userService;

    @Autowired
    protected RegistryService registryService;

    /**
     * delete job
     * TODO: @********.cn需要清理
     * @param jobGroup
     * @param jobKey
     * @return
     */
    public int deleteByJobGroupAndKey(String jobGroup, String jobKey) {
        int deleteCount;
        Map<String, String> param = new HashMap<>(4);
        param.put("jobGroup", jobGroup);
        param.put("jobKey", jobKey);
        deleteCount = basicJobMapper.deleteByJobKeyAndJobGroup(param);
        return deleteCount;
    }

    /**
     * Delete the choreography relationship and job
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    public int deleteJobByJobKeyAndJobGroup(String jobGroupName, String jobKey) {
        int result;
        // Delete the choreography relationship
        jobMTaskService.deleteByJobKeyAndJobGroup(jobGroupName, jobKey);
        //Delete Job
        result = deleteByJobGroupAndKey(jobGroupName, jobKey);
        LOGGER.info(Constants.LOG_PREFIX + " remove job from db result : {}. jobGroupName is {} jobKey is {}", result, jobGroupName, jobKey);
        return result;
    }

    /**
     * insert Job
     * TODO: @********.cn需要清理
     * @param basicJob
     * @return
     */
    public int insertSelective(BasicJob basicJob) {
        return basicJobMapper.insertSelective(basicJob);
    }

    /**
     * Query jobs whit increasing paging
     * @param jobGroup
     * @param jobKey
     * @param currentPage
     * @param pageSize
     * @return
     */
    public PageBean<BasicJob> selectByJobKeyAndJobGroup(String jobGroup, String jobKey, int currentPage, int pageSize, List<String> roleNames) {
        Map<String, Object> param = new HashMap<>(4);
        param.put("jobKey", jobKey);
        param.put("jobGroup", jobGroup);
        param.put("roleNames", roleNames);
        PageHelper.startPage(currentPage, pageSize);
        //record
        List<BasicJob> basicJobs = basicJobMapper.selectByJobKeyAndJobGroupList(param);
        for (BasicJob basicJob : basicJobs) {
            List<String> jobScheduler = registryService.getJobScheduler(basicJob.getJobGroup(), basicJob.getJobKey());
            basicJob.setTriggerInstance(StringHelper.join(jobScheduler, ","));
        }
        //record count
        int countBasicJobs = basicJobMapper.selectCountBasicJobs(param);
        PageBean<BasicJob> pageData = new PageBean<>(currentPage, pageSize, countBasicJobs);
        pageData.setItems(basicJobs);
        return pageData;
    }

    /**
     * update Job
     * @param basicJob
     * @return
     */
    public int updateByPrimaryKey(BasicJob basicJob) {
        if (basicJob == null) {
            LOGGER.warn(Constants.LOG_PREFIX + " update Job fail, basicJob invalid, basicJob={}", basicJob);
            return 0;
        }
        return basicJobMapper.updateByPrimaryKey(basicJob);
    }

    /**
     * Get permission list
     * @param roleNames
     * @return
     */
    public Map<String, List<String>> selectAuth(List<String> roleNames) {
        List<BasicJob> basicJobs = basicJobMapper.selectAuth(roleNames);
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

    private void updateJobs(List<BasicJob> jobs){
        for (BasicJob basicJob : jobs) {
            List<String> jobScheduler = registryService.getJobScheduler(basicJob.getJobGroup(), basicJob.getJobKey());
            basicJob.setTriggerInstance(StringHelper.join(jobScheduler, ","));
            String jobStatus = registryService.getJobStatus(basicJob.getJobGroup(), basicJob.getJobKey());
            basicJob.setJobDesc(jobStatus);
        }
    }

    /**
     * Gets an overview of the tasks in scheduling monitoring
     * @param roleNames
     */
    public Map<String, Integer> selectTaskView(List<String> roleNames){
        Map<String, Integer> jobStatusSum = new HashMap<>(4);
        jobStatusSum.put("ready", 0);
        jobStatusSum.put("running", 0);
        jobStatusSum.put("stop", 0);
        jobStatusSum.put("expStop", 0);

        Map<String, Object> param = new HashMap<>(4);
        param.put("roleNames", roleNames);
        List<BasicJob> jobs = basicJobMapper.selectByJobKeyAndJobGroupList(param);
        updateJobs(jobs);

        long stopNum = jobs.stream().filter(basicJob -> basicJob.getJobDesc() == null).count();
        jobStatusSum.put("stop", (int)stopNum);

        long expNum = jobs.stream().filter(basicJob -> "stop".equals(basicJob.getJobDesc())).count();
        jobStatusSum.put("expStop", (int)expNum);

        long runNum = jobs.stream().filter(basicJob -> "running".equals(basicJob.getJobDesc())).count();
        jobStatusSum.put("running", (int)runNum);

        long readyNum = jobs.stream().filter(basicJob -> "ready".equals(basicJob.getJobDesc())).count();
        jobStatusSum.put("ready", (int)readyNum);

        return jobStatusSum;
    }

    /**
     * Get the number of scheduling monitoring projects, tasks, and jobs
     * @param roleNames
     */
    public Map<String, Integer> selectSummary(List<String> roleNames){
        Map<String, Integer> pjtNum = new HashMap<>(4);
        pjtNum.put("projectNum", 0);
        pjtNum.put("jobNum", 0);
        pjtNum.put("taskNum", 0);

        Map<String, Object> param = new HashMap<>(4);
        param.put("roleNames", roleNames);
        List<BasicJob> jobs = basicJobMapper.selectByJobKeyAndJobGroupList(param);
        int countBasicTasks = basicTaskMapper.selectCountbasicTasks(param);
        long jobGroupCount = jobs.stream().map(basicJob -> basicJob.getJobGroup()).distinct().count();
        pjtNum.put("projectNum", (int)jobGroupCount);
        pjtNum.put("jobNum", jobs.size());
        pjtNum.put("taskNum", countBasicTasks);

        return pjtNum;
    }

    /**
     * The scheduling monitoring page is filtered by project name
     * @param jobGroupName
     * @param roleNames
     * @return
     */
    public List<JobPortrait> selectJobGroupPortrait(String jobGroupName, List<String> roleNames){
        List<JobPortrait> portraitVoList = new ArrayList<>();

        Map<String, Object> param = new HashMap<>(4);
        param.put("roleNames", roleNames);
        param.put("jobGroup", jobGroupName);
        List<BasicJob> jobs = basicJobMapper.selectByJobKeyAndJobGroupList(param);

        List<String> groups = jobs.stream().map(basicJob -> basicJob.getJobGroup()).distinct().collect(Collectors.toList());
        if (!StringHelper.isEmpty(jobGroupName)){
            groups.clear();
            groups.add(jobGroupName);
        }

        groups.forEach(group -> {
            Map<String, Integer> total = new HashMap<>(4);
            total.put("sum", 0);
            total.put("activated", 0);
            total.put("stop", 0);

            List<BasicJob> gJobs = jobs.stream().filter(basicJob -> basicJob.getJobGroup().equals(group)).collect(Collectors.toList());
            updateJobs(gJobs);

            long activatedNum = gJobs.stream().filter(basicJob -> basicJob.getJobDesc() != null).count();
            total.put("activated", (int)activatedNum);
            long expStopNum = gJobs.stream().filter(basicJob -> "stop".equals(basicJob.getJobDesc())).count();
            total.put("stop", (int)expStopNum);
            total.put("sum", gJobs.size());

            //count
            JobPortrait portrait = new JobPortrait();
            portrait.setPortrait(gJobs);
            portrait.setTotal(total);
            portrait.setGroup(group);
            portraitVoList.add(portrait);
        });

        return portraitVoList;
    }

    /**
     * Gets a list of jobkeys that the Scheduler executes
     *
     * @param scheduler
     * @return
     */
    public List <String> getJobKeyListByScheduler(String scheduler) {
        return registryService.getJobKeyListByScheduler(scheduler);
    }

    /**
     * Gets a list of jobs that the Scheduler executes
     * @param scheduler
     * @return
     */
    public List <BasicJob>  getJobListByScheduler (String scheduler) {
        List<String> jobKeyList = getJobKeyListByScheduler(scheduler);
        List<BasicJob> basicJobs = new ArrayList<>();
        if(jobKeyList.size()>0) {
            Map<String, Object> param = new HashMap<>(4);
            param.put("jobKeys", jobKeyList);
            basicJobs = basicJobMapper.selectByJobKeyList(param);
        }
        return basicJobs;
    }

    public List<Map<String, Integer>> selectGroupAndJobCount(List<String> roleNames, String jobGroupName){

        Map<String, Object> param = new HashMap<>(3);
        param.put("jobGroupName", jobGroupName);
        param.put("roleNames", roleNames);

        return basicJobMapper.selectGroupAndJobNum(param);
    }

    /**
     * Set the cascading Job JobPlan
     * @param basicJob
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    public int updateJobPlanByJobKey(BasicJob basicJob) throws Exception{
        try {
            BasicJob jobChild = basicJob.getJobChild();
            if (null == jobChild){
                BasicJob oldChild = basicJobMapper.selectChilds(basicJob.getJobKey());
                oldChild.setJobPlan(null);
                oldChild.setJobParentKey(null);
                basicJobMapper.updateByPrimaryKey(oldChild);
                basicJob.setJobPlan(null);
                return basicJobMapper.updateByPrimaryKey(basicJob);
            }else {
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
     * Clear job cascading relationships
     * @param basicJob
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    public void removeJobPlan(BasicJob basicJob) {
        BasicJob jobOld = selectJobsByGroupAndJobKey(basicJob.getJobGroup(), basicJob.getJobKey());
        List<BasicJob> basicJobs = selectJobsByPlan(jobOld.getJobPlan());
        basicJobs.forEach(job -> {
            job.setJobPlan(null);
            job.setJobParentKey(null);
            updateByPrimaryKey(job);
        });
    }

    public BasicJob selectJobsByGroupAndJobKey(String jobGroup, String jobKey){
        Map<String, String> param = new HashMap<>(2);
        param.put("jobKey", jobKey);
        param.put("jobGroup", jobGroup);
        return basicJobMapper.selectByJobKeyAndJobGroup(param);
    }

    public List<BasicJob> selectJobsByPlan(String jobPlan) {
        return basicJobMapper.selectJobsByPlan(jobPlan);
    }

    /**
     * activate Job
     * @param jobGroupName
     * @param jobKey
     * @return
     */

    public boolean activateJob(String jobGroupName, String jobKey) {

        String userName = userService.getCurrentUser();
        boolean jobStatus4User = false;
        boolean flag = registryService.createJobKey(jobGroupName, jobKey);
        LOGGER.info(Constants.OPERATION_LOG_PREFIX + "username is: " + userName + "; operation is: activate job,jobKey is:" + jobKey);
        if (flag) {
            jobStatus4User = registryService.casJobStatus4User(jobGroupName, jobKey, JobStatus.STOP.toString(), JobStatus.READY.toString());
        }
        return jobStatus4User;
    }

    /**
     * Batch transfer of job with one key
     * @param scheduler
     * @return
     */
    public Map<String, Object> batchJobTransfer(String scheduler) {

        List <String> jobKeyList = getJobKeyListByScheduler(scheduler);
        //Offline scheduler
        boolean openScheduler = curator4Scheduler.closeScheduler(scheduler);
        if (!openScheduler) {
            return Collections.EMPTY_MAP;
        }
        //Register the offline scheduler with the JobTransfer list
        curator4Scheduler.registerJobTransfer(scheduler);

        int transSuccessCount;
        List <String> transFailedList = new ArrayList<>();
        List <String> runningJobList = new ArrayList<>();
        String userName = userService.getCurrentUser();
        for(int i=0;i<jobKeyList.size();i++) {
            String jobGroupName = jobKeyList.get(i).split(Constant.JOBKEY_SEPARATOR)[0];
            String jobStatus = curator4Scheduler.getJobStatus(jobGroupName, jobKeyList.get(i));
            //The Job state is stopped or running without transition
            if(StringHelper.isEmpty(jobStatus)||JobStatus.RUNNING.toString().equals(jobStatus)) {
                LOGGER.info(Constants.OPERATION_LOG_PREFIX + "username is: " + userName + ";" + jobKeyList.get(i) + " Running/stopped, no transition required");
                continue;
            }

            boolean deleteJobKey = curator4Scheduler.deleteJobKey(jobGroupName, jobKeyList.get(i));
            LOGGER.info(Constants.OPERATION_LOG_PREFIX + "username is: " + userName + ";" + jobKeyList.get(i) + " stop job {}", deleteJobKey);

            boolean activateJob = activateJob(jobGroupName, jobKeyList.get(i));
            LOGGER.info(Constants.OPERATION_LOG_PREFIX + "username is: " + userName + ";" + jobKeyList.get(i) + " activate job {}", activateJob);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOGGER.info(Constants.OPERATION_LOG_PREFIX + " Exception is {}", e);
        }

        transFailedList.addAll(jobKeyList);

        jobKeyList.forEach(jobKey -> {
            List<String> jobScheduler = curator4Scheduler.getJobScheduler(jobKey.split(Constant.JOBKEY_SEPARATOR)[0], jobKey);
            if (jobScheduler.size() > 0) {
                transFailedList.remove(jobKey);
            }

            if (jobScheduler.contains(scheduler)) {
                runningJobList.add(jobKey);
            }
        } );

        transSuccessCount = jobKeyList.size() - transFailedList.size() - runningJobList.size();
        HashMap<String,Object> resultMap = new HashMap<>(8);
        resultMap.put("jobKeyCount",jobKeyList.size());
        resultMap.put("jobKeyList",jobKeyList);
        resultMap.put("transSuccessCount",transSuccessCount);
        resultMap.put("transFailedList", transFailedList);
        resultMap.put("runningJobList", runningJobList);
        //temporarily save the result of one-click transfer Job to JobTransfer
        curator4Scheduler.updateJobTransfer(scheduler, JSONHelper.toString(resultMap));
        return resultMap;
    }
}
