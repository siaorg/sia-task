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

import com.google.common.collect.Maps;
import com.sia.task.integration.curator.Curator4Scheduler;
import com.sia.task.mapper.IndexMapper;
import com.sia.task.pojo.Index4UserVo;
import com.sia.task.pojo.TaskErrorLogVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页展示 用户
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/6/12 2:19 下午
 **/
@Service
public class Index4UserService {

    @Resource
    IndexMapper managerMapper;

    @Resource
    protected Curator4Scheduler curator4Scheduler;

    /**
     * 首页大屏统计指标
     *
     * @param group 过滤条件
     * @return <code>{@link Index4UserVo}</code>
     */
    public Index4UserVo homePageStatistics(String group) {
        return managerMapper.homePageStatistics4User(group);
    }

    /**
     * 获取任务执行的健康度
     *
     * @param group 过滤条件
     * @return 返回任务执行的异常计数，mis计数以及总的调度计数
     */
    public HashMap<String, Integer> taskTrackerHealthStatus(String group) {
        return managerMapper.taskTrackerHealthStatus4User(group);
    }

    /**
     * 获取任务执行异常日志 top10
     *
     * @param group 过滤条件
     * @return 返回异常信息统计计数最高的前十条异常信息
     */
    public List<TaskErrorLogVo> warningLogTop(String group) {

        return managerMapper.warningLogTop(group);
    }

    /**
     * 获取最近二十四小时内每小时Job执行统计。
     * 按项目组进行过滤
     *
     * @param group 过滤条件
     * @return 返回二十四小时内每小时Job执行统计技术
     */
    public List<Map<String, String>> jobSchedulingStatistics(String group) {
        List<Map<String, String>> mapList = managerMapper.jobSchedulingStatistics(group);
        if (mapList.size() > 24) {
            mapList.remove(0);
        }
        return mapList;
    }

    /**
     * 获取最近二十四小时内每小时Task执行统计。
     * 按项目组进行过滤
     *
     * @param group 过滤条件
     * @return 返回二十四小时内每小时Task执行统计技术
     */
    public List<Map<String, String>> taskExecutionStatistics(String group) {
        List<Map<String, String>> mapList = managerMapper.taskExecutionStatistics(group);
        if (mapList.size() > 24) {
            mapList.remove(0);
        }
        return mapList;
    }

    /**
     * 获取任务分配情况
     * 按项目组维度进行过滤。
     *
     * @param group 过滤条件
     * @return 返回按照调度器为维度的任务分配
     */
    public HashMap<String, List<String>> jobLoadInfo(String group) {
        HashMap<String, List<String>> map = Maps.newHashMap();
        List<String> schedulers = curator4Scheduler.getSchedulers();
        if (schedulers != null && schedulers.size() > 0) {
            schedulers.forEach(s -> map.put(s, new ArrayList<>()));
            List<String> list = curator4Scheduler.getJobKeys(group);
            if (list != null && list.size() > 0) {
                list.forEach(key -> {
                    List<String> jobScheduler = curator4Scheduler.getJobScheduler(group, key);
                    if (jobScheduler != null && jobScheduler.size() > 0) {
                        jobScheduler.forEach(s -> {
                            List<String> keys = map.get(s);
                            keys.add(key);
                            map.put(s, keys);
                        });
                    }
                });
            }
        }
        return map;
    }
}
