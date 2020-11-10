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

package com.sia.task.admin.controller;

import com.sia.task.admin.service.BasicJob4Service;
import com.sia.task.admin.vo.SiaResponseBody;
import com.sia.task.core.entity.BasicJob;
import com.sia.task.core.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * Job管理界面 API
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2018/4/19 11:24 上午
 **/
@RestController
@RequestMapping("/jobapi")
@Slf4j
public class BasicJobController {

    @Resource
    BasicJob4Service basicJob4Service;

    /**
     * cron表达式合法性验证
     * cronExpression
     *
     * @param cron
     * @return
     */
    @RequestMapping(value = "/cronexpression", method = RequestMethod.GET)
    public String isValidExpression(@RequestParam String cron) {
        return SiaResponseBody.isOk(basicJob4Service.isValidExpression(cron));
    }

    /**
     * 查询指定Job状态
     * 获取 Job Status
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    @RequestMapping(value = "/selectJobStatus/{jobGroupName}/{jobKey}", method = RequestMethod.GET)
    public String selectJobStatus(@PathVariable String jobGroupName, @PathVariable String jobKey) {
        return SiaResponseBody.success(basicJob4Service.selectJobStatus(jobGroupName, jobKey));
    }

    /**
     * 激活JOB
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    @RequestMapping(value = "/activateJob/{jobGroupName}/{jobKey}", method = RequestMethod.GET)
    public String activateJob(@PathVariable String jobGroupName, @PathVariable String jobKey) {
        return basicJob4Service.activateJob(jobGroupName, jobKey);
    }

    /**
     * 停止Job
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    @RequestMapping(value = "/stopJob/{jobGroupName}/{jobKey}", method = RequestMethod.GET)
    public String stopJob(@PathVariable String jobGroupName, @PathVariable String jobKey) {
        return SiaResponseBody.isOk(basicJob4Service.stop(jobGroupName, jobKey));
    }

    /**
     * 根据角色对应组名进行权限过滤
     *
     * @return
     */
    @RequestMapping(value = "/selectJobKeysByGroup", method = RequestMethod.GET)
    public String selectJobKeysByGroup(@RequestParam String jobGroupName) {
        return SiaResponseBody.success(basicJob4Service.selectJobKeysByGroup(jobGroupName));
    }

    /**
     * 端口调用jobList接口 支持分页
     * 参数可为null
     * showJobList
     * if jobAppName and jobGroupName = null ? return All : return filtration
     *
     * @param jobGroupName 可为null
     * @param jobKey       可为null
     * @return
     */
    @RequestMapping(value = "/selectjobs", method = RequestMethod.GET)
    public String selectJobsByCondition(@RequestParam String jobGroupName,
                                        @RequestParam String jobKey,
                                        String jobDesc,
                                        @RequestParam int currentPage,
                                        @RequestParam int pageSize) {
        return SiaResponseBody.success(basicJob4Service.selectJobsByCondition(jobGroupName, jobKey, jobDesc, currentPage, pageSize));
    }

    /**
     * 获取JobGroup及其对用JobGroup的Job个数
     */
    @RequestMapping(value = "/selectGroupAndJobCount", method = RequestMethod.GET)
    public String selectGroupAndJobCount(@RequestParam String jobGroupName) {
        return SiaResponseBody.success(basicJob4Service.selectGroupAndJobCount(jobGroupName));
    }

    /**
     * 添加Job
     * TODO 入参校验
     *
     * @param basicJob
     * @return
     */
    @PostMapping(value = "/insertJob")
    public String insertJob(@RequestBody BasicJob basicJob) {
        return SiaResponseBody.success(basicJob4Service.insertJob(basicJob));
    }

    /**
     * 更新Job
     * TODO 入参校验
     *
     * @param basicJob
     * @return
     */
    @PostMapping("/updateJob")
    public String updateJob(@RequestBody BasicJob basicJob) {
        return SiaResponseBody.isOk(basicJob4Service.updateJob(basicJob));
    }

    /**
     * 删除Job
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    @RequestMapping(value = "/deleteJobByJobKeyAndGroup", method = RequestMethod.GET)
    public String deleteJob(@RequestParam String jobGroupName, @RequestParam String jobKey) {
        return SiaResponseBody.isOk(basicJob4Service.deleteJob(jobGroupName, jobKey));
    }

    /**
     * 获取调度监控中任务概览
     */
    @RequestMapping(value = "/selectTaskView", method = RequestMethod.GET)
    public String selectTaskView() {
        return SiaResponseBody.success(basicJob4Service.selectTaskMonitorView());
    }

    /**
     * 获取调度监控项目数量、任务数量、JOB数量
     */
    @RequestMapping(value = "/selectSummary", method = RequestMethod.GET)
    public String selectSummary() {
        return SiaResponseBody.success(basicJob4Service.selectSummary());
    }

    /**
     * 调度监控页面按项目名称筛选、获取调度器运行详情
     */
    @RequestMapping(value = "/jobGroupPortrait", method = RequestMethod.GET)
    public String selectJobGroupPortrait(@RequestParam String jobGroupName) {
        return SiaResponseBody.success(basicJob4Service.selectJobGroupPortrait(jobGroupName));
    }

    /**
     * 获取调度器下的job列表
     *
     * @param scheduler
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/getJobList", method = RequestMethod.GET)
    public String getJobListByScheduler(@RequestParam String scheduler) {
        if (StringHelper.isEmpty(scheduler)) {
            return SiaResponseBody.failure();
        }
        return SiaResponseBody.success(basicJob4Service.selectJobs4Scheduler(scheduler));
    }

    /**
     * 设置级联Job JobPlan
     * TODO 功能优化 新版调度版本没有提供实现
     *
     * @param basicJob
     * @return
     */
    @RequestMapping(value = "/updatejobplan", method = RequestMethod.POST)
    public String updateJobPlan(@RequestBody BasicJob basicJob) {
        return basicJob4Service.updateJobPlan(basicJob);
    }

    /**
     * 前端调用接口-JOB执行一次
     * runOnce,监听机制
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    @RequestMapping(value = "/runOnceforweb/{jobGroupName}/{jobKey}", method = RequestMethod.GET)
    @ResponseBody
    public String JobRunOnce(@PathVariable String jobGroupName, @PathVariable String jobKey) {
        return SiaResponseBody.isOk(basicJob4Service.runOnce4Zk(jobGroupName, jobKey));
    }
}
