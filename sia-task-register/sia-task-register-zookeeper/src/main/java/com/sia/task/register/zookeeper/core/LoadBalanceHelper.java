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

package com.sia.task.register.zookeeper.core;

import com.sia.task.core.util.Constant;
import com.sia.task.core.util.JsonHelper;
import com.sia.task.core.util.StringHelper;
import com.sia.task.integration.curator.Curator4Scheduler;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: pengfeili23
 * @Description: 用来做负载均衡和平滑迁移
 * @date: 2018年6月21日 下午9:25:24
 */
@Slf4j
public class LoadBalanceHelper {

    private LoadBalanceHelper() {

    }

    private static final String MY_JOB_NUM = "MY_JOB_NUM";
    private static final String ALARM_JOB_NUM = "ALARM_JOB_NUM";
    private static final String MAX_JOB_NUM = "MAX_JOB_NUM";
    private static final String ENABLE_JOB_TRANSFER = "ENABLE_JOB_TRANSFER";
    /**
     * 开关变量，用来表示自己是否下线
     */
    private static AtomicBoolean closeGate = new AtomicBoolean(false);

    /**
     * 计数器，记录自己获得JOB的个数
     */
    private static AtomicInteger myJob = new AtomicInteger(0);
    private static Curator4Scheduler curator4Scheduler = null;

    /**
     * 初始化操作
     *
     * @param curator
     * @throws Exception
     */
    public static void initLoadBalanceHelper(Curator4Scheduler curator) throws Exception {

        // 获得ZK的连接
        curator4Scheduler = curator;

    }

    private static int faultTolerant = 1;

    private static int alarmThreshold = 32;

    public static int getFaultTolerant() {

        return faultTolerant;
    }

    public static int getAlarmThreshold() {

        return alarmThreshold;
    }

    public static void setFaultTolerant(int n) {

        faultTolerant = n;
    }

    public static void setAlarmThreshold(int m) {

        alarmThreshold = m;
    }

    private static int max(int x, int y) {

        return x > y ? x : y;
    }

    /**
     * 获取当前调度器可获得JOB的上限（动态计算得出）
     *
     * @return
     */
    public static int getJobThreshold() {

        int k = curator4Scheduler.getAllJobKeys().size();
        int s = curator4Scheduler.getSchedulers().size();
        int n = max(getFaultTolerant(), 1);

        return 1 + (k / max(s - n, 1));

    }

    /**
     * 更新状态为下线，幂等操作
     * 开启调度器下线开关，准备下线：（1）不再争抢JOB（2）对于已获得的JOB，调度JOB结束后主动释放
     */
    public static boolean offline() {

        return closeGate.compareAndSet(false, true);
    }

    /**
     * 更新状态为上线，幂等操作
     */
    public static boolean online() {

        return closeGate.compareAndSet(true, false);
    }

    /**
     * 判断自己是否应该下线
     *
     * @return
     */
    public static boolean isOffline() {

        return closeGate.get() == true;
    }

    /**
     * 获取当前调度器获取的JOB个数
     *
     * @return
     */
    public static int getMyJobNum() {

        return myJob.get();
    }

    /**
     * 获取的JOB个数超出阈值则预警
     *
     * @return
     */

    public static boolean isAlarm() {

        return getMyJobNum() > getAlarmThreshold();
    }

    /**
     * 获取的JOB个数超出上限则拒绝
     *
     * @return
     */
    public static boolean isRefuse() {

        log.info("[{}] : [{}]", getMyJobNum(), getJobThreshold());
        return getMyJobNum() >= getJobThreshold();
    }

    /**
     * 更新调度器的信息，主要是当前执行的JOB个数，JOB预警阈值，获取JOB上限（超出则拒绝）
     *
     * @return
     */
    public static synchronized boolean updateScheduler(int myJobDelta, String jobKeyMessage) {
        log.info(Constant.LOG_PREFIX + " updateScheduler load balance - [operand:{}], [jobKeyMessage : {}] ", myJobDelta, jobKeyMessage);
        // 更新job计数
        myJob.addAndGet(myJobDelta);
        // 获取当前调度器zookeeper上的负载计数信息
        String schedulerData = curator4Scheduler.getSchedulerInfo(Constant.LOCALHOST);
        HashMap<String, String> schedulerInfo = new HashMap<String, String>(8);
        if (!StringHelper.isEmpty(schedulerData)) {
            schedulerInfo = JsonHelper.toObject(schedulerData, HashMap.class);
        }
        schedulerInfo.put(MY_JOB_NUM, String.valueOf(getMyJobNum()));
        schedulerInfo.put(ALARM_JOB_NUM, String.valueOf(getAlarmThreshold()));
        schedulerInfo.put(MAX_JOB_NUM, String.valueOf(getJobThreshold()));
        log.info(Constant.LOG_PREFIX + " updateScheduler load balance - {} - [jobKeyMessage : {}] ", JsonHelper.toString(schedulerInfo), jobKeyMessage);
        // 更zookeeper上负载信息
        return curator4Scheduler.updateScheduler(Constant.LOCALHOST, JsonHelper.toString(schedulerInfo));
    }
}
