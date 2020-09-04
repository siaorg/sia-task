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

import com.sia.task.core.util.Constant;
import com.sia.task.integration.curator.Curator4Scheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 调度器管理 service
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2020/4/26 4:14 下午
 **/
@Slf4j
@Service
public class Scheduler4Service {

    @Resource
    protected Curator4Scheduler curator4Scheduler;

    /**
     * 获取工作调度器列表
     * 获取所以在线调度器列表 移除 黑名单中的调度器
     * allSchedulers - scheduler4Offline
     *
     * @return
     */
    public List<String> getSchedulers4Working() {
        List<String> scheduler4Offline = curator4Scheduler.getBlackList();
        List<String> scheduler4Online = curator4Scheduler.getSchedulers();
        scheduler4Online.removeAll(scheduler4Offline);
        log.info(Constant.LOG_PREFIX + " getSchedulers4Working : schedulers is {}", scheduler4Online);
        return scheduler4Online;
    }

    /**
     * 获取非工作状态的调度器列表
     * 非工作状态的调度器列表 = ZK_OFFLINE_SCHEDULER & ZK_ONLINE_SCHEDULER
     *
     * @return
     */
    public List<String> getScheduler4OutOfServices() {
        List<String> scheduler4Offline = curator4Scheduler.getBlackList();
        List<String> scheduler4Online = curator4Scheduler.getSchedulers();
        scheduler4Offline.retainAll(scheduler4Online);
        log.info(Constant.LOG_PREFIX + " getScheduler4OutOfServices : schedulers is {}", scheduler4Offline);
        return scheduler4Offline;
    }

    /**
     * 获取已经离线调度器列表
     * <p>
     * 离线调度器列表 = <code>ZK_OFFLINE_SCHEDULER</code> - <code>ZK_ONLINE_SCHEDULER</code>
     * </p>
     *
     * @return
     */
    public List<String> getSchedulers4OffLine() {
        List<String> scheduler4Offline = curator4Scheduler.getBlackList();
        List<String> scheduler4Online = curator4Scheduler.getSchedulers();
        scheduler4Offline.removeAll(scheduler4Online);
        log.info(Constant.LOG_PREFIX + " get SchedulersOffLine >>> success : schedulerInstance is {}", scheduler4Offline);
        return scheduler4Offline;
    }


    /**
     * 注销调度器在<code>ZK_OFFLINE_SCHEDULER</code>路径
     * <p>
     * 注销离线路径下的调度器结果分为两种：
     * 1. 如果调度器没有离线，此时该调度器就会进入<code>ZK_ONLINE_SCHEDULER</code>
     * 2. 如果调度器已经离线，此时调度器就会被移除
     * </p>
     *
     * @return
     */
    public boolean unregisterScheduler4Offline(String schedulers) {
        AtomicBoolean flag = new AtomicBoolean(true);
        Arrays.asList(schedulers.split(Constant.REGEX_COMMA)).forEach(scheduler -> {
            if (!curator4Scheduler.openScheduler(scheduler)) {
                flag.set(false);
                return;
            }
        });
        return flag.get();
    }

    /**
     * 注册调度器在<code>ZK_OFFLINE_SCHEDULER</code>路径
     *
     * @return
     */
    public boolean registerScheduler4Offline(String schedulers) {
        AtomicBoolean flag = new AtomicBoolean(true);
        Arrays.asList(schedulers.split(Constant.REGEX_COMMA)).forEach(scheduler -> {
            if (!curator4Scheduler.closeScheduler(scheduler)) {
                flag.set(false);
                return;
            }
        });
        return flag.get();
    }

    /**
     * 获取白名单列表中注册的调度器:<code>Constant.ZK_ONLINE_AUTH</code>
     *
     * @return
     */
    public List<String> getScheduler4OnlineAuth() {
        return curator4Scheduler.getAuthList();
    }

    /**
     * 注册调度器在<code>ZK_ONLINE_AUTH</code>路径
     *
     * @param ip 开通调度权限的ip地址
     * @return
     */
    public boolean registerScheduler4OnlineAuth(String ip) {
        return curator4Scheduler.addToAuth(ip);
    }

    /**
     * 注销调度器在<code>ZK_ONLINE_AUTH</code>路径
     *
     * @param instance
     * @return
     */
    public boolean unregisterScheduler4OnlineAuth(@RequestBody String instance) {
        AtomicBoolean flag = new AtomicBoolean(true);
        Arrays.asList(instance.split(Constant.REGEX_COMMA)).forEach(scheduler -> {
            if (!curator4Scheduler.removeFromAuth(scheduler)) {
                flag.set(false);
                return;
            }
        });
        return flag.get();
    }

}
