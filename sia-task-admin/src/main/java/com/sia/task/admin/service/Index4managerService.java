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
import com.sia.task.core.util.JsonHelper;
import com.sia.task.integration.curator.Curator4Scheduler;
import com.sia.task.integration.curator.properties.ZookeeperConstant;
import com.sia.task.mapper.IndexMapper;
import com.sia.task.pojo.Index4ManagerVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页展示
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/6/12 2:19 下午
 **/
@Slf4j
@Service
public class Index4managerService {

    @Resource
    IndexMapper managerMapper;

    @Resource
    protected Curator4Scheduler curator4Scheduler;

    public Index4ManagerVo homePageStatistics() {
        return managerMapper.homePageStatistics4Manager();
    }

    public HashMap<String, Integer> taskTrackerHealthStatus() {
        return managerMapper.taskTrackerHealthStatus4Manager();
    }

    public Map<String, String> schedulerLoadInfo() {

        Map<String, String> schedulerInfo = new HashMap<>();
        List<String> schedulers = curator4Scheduler.getSchedulers();
        if (schedulers != null && schedulers.size() > 0) {
            schedulers.forEach(scheduler -> {
                HashMap<String, String> infoMap = JsonHelper.toObject(curator4Scheduler.getSchedulerInfo(scheduler), HashMap.class);
                if (infoMap != null) {
                    infoMap.remove("ENABLE_JOB_TRANSFER");
                }
                String infoRes = JsonHelper.toString(infoMap);
                schedulerInfo.put(scheduler, infoRes);
            });
        }
        return schedulerInfo;
    }

    /**
     * 获取任务分配情况
     * 按项目组维度进行过滤。
     *
     * @return 返回按照调度器为维度的任务分配
     */
    public HashMap<String, List<String>> jobLoadInfo() throws Exception {
        HashMap<String, List<String>> map = Maps.newHashMap();
        List<String> schedulers = curator4Scheduler.getSchedulers();
        schedulers.forEach(s -> map.put(s, new ArrayList<>()));
        String jobPath = new StringBuilder().append(ZookeeperConstant.ZK_ROOT).append(curator4Scheduler.getTaskRoot()).append(ZookeeperConstant.ZK_SEPARATOR).append(ZookeeperConstant.ZK_ONLINE_JOB).toString();
        List<String> groupNames = curator4Scheduler.getCuratorClient().getChildren(jobPath);
        groupNames.forEach(group -> {
            List<String> jobKeys = curator4Scheduler.getJobKeys(group);
            jobKeys.forEach(key -> {
                List<String> jobScheduler = curator4Scheduler.getJobScheduler(group, key);
                if (jobScheduler != null && jobScheduler.size() > 0) {
                    List<String> keys = map.get(jobScheduler.get(0));
                    keys.add(key);
                    map.put(jobScheduler.get(0), keys);
                }
            });
        });
        return map;
    }
}
