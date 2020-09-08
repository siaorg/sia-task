package com.sia.task.register.zookeeper.core;/*-
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

//package task.collector.zookeeper.core;
//
//
//import com.sia.base.util.Constant;
//import com.sia.core.helper.JSONHelper;
//import com.sia.core.helper.StringHelper;
//import com.sia.task.integration.curator.Curator4Scheduler;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.HashMap;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//
///**
// * @author maozhengwei
// * @version V1.0.0
// * @description
// * @data 2019-05-27 10:36
// * @see
// **/
//public class Lb {
//    private static final Logger LOGGER = LoggerFactory.getLogger(Lb.class);
//    private static final String MY_JOB_NUM = "MY_JOB_NUM";
//    private static final String ALARM_JOB_NUM = "ALARM_JOB_NUM";
//    private static final String MAX_JOB_NUM = "MAX_JOB_NUM";
//    private static final String ENABLE_JOB_TRANSFER = "ENABLE_JOB_TRANSFER";
//
//
//    private static final int MAX_PRIORITY = 10;
//
//    private static final int DEFAULT_PRIORITY = 5;
//
//    private static final int MIN_PRIORITY = 0;
//
//    private static AtomicInteger MyJobCount = new AtomicInteger(0);
//
//    private static final float GROUP_JOB_COUNT_THRESHOLD = 0.75f;
//
//    private static int alarmThreshold = 32;
//
//    private static ConcurrentHashMap<String, Integer> groupMyJobCount = new ConcurrentHashMap();
//
//    /**
//     * 开关变量，用来表示自己是否下线
//     */
//    private static volatile AtomicBoolean closeGate = new AtomicBoolean(false);
//
//    private static Lock lock = new ReentrantLock();
//
//    private static volatile Curator4Scheduler curator4Scheduler = null;
//
//    private static int priority = -1;
//
//
//    /**
//     * 更新状态为上线，幂等操作
//     */
//    public static boolean online() {
//
//        return closeGate.compareAndSet(true, false);
//    }
//
//    /**
//     * 更新状态为下线，幂等操作
//     */
//    public static boolean offline() {
//
//        return closeGate.compareAndSet(false, true);
//    }
//
//    /**
//     * 判断自己是否应该下线
//     *
//     * @return
//     */
//    public static boolean isOffline() {
//
//        return closeGate.get() == true;
//    }
//
//    /**
//     * 获取当前调度器获取的JOB个数
//     *
//     * @return
//     */
//    public static int getMyJobNum() {
//
//        return MyJobCount.get();
//    }
//
//    public static int getAlarmThreshold() {
//
//        return alarmThreshold;
//    }
//
//    /**
//     * 获取的JOB个数超出阈值则预警
//     *
//     * @return
//     */
//
//    public static boolean isAlarm() {
//
//        return getMyJobNum() > getAlarmThreshold();
//    }
//
//    /**
//     * 初始化操作
//     *
//     * @param curator
//     * @throws Exception
//     */
//    public static void initLoadBalanceHelper(Curator4Scheduler curator) throws Exception {
//
//        // 获得ZK的连接
//        curator4Scheduler = curator;
//
//    }
//
//    /**
//     * my   0  3  5
//     * max  0  0  0
//     * res  0
//     *
//     * @param group
//     * @return
//     */
//    public static int calculationPriority(int jobCount, int instanceCount, int groupJobCount, String group) {
//
//
//        try {
//            lock.lock();
//            priority = DEFAULT_PRIORITY;
//            /**
//             * 1、判断项目组的负载  是否超过阈值
//             * 2、判断总数阈值 是否超过阈值
//             * 3、将所有实例放入队里，进行优先级排序
//             * 4、消费队列，非自己进行等待一小会
//             */
//            //1判断项目组的负载  是否超过阈值
//            if (!groupMyJobCount.containsKey(group) || groupMyJobCount.get(group) == 0) {
//
//                priority = MAX_PRIORITY;
//
//            } else {
//
//                int groupMyJob = groupMyJobCount.get(group);
//                //
//                priority = groupMyJob >= groupJobCount * GROUP_JOB_COUNT_THRESHOLD ? MIN_PRIORITY : priority;
//            }
//
//            //2判断总数阈值 是否超过阈值
//            priority = MyJobCount.get() > Math.round(jobCount / instanceCount) ?
//                    (priority == MAX_PRIORITY ? DEFAULT_PRIORITY : (priority == MIN_PRIORITY ? MIN_PRIORITY : DEFAULT_PRIORITY))
//                    : (priority == MIN_PRIORITY ? DEFAULT_PRIORITY : MAX_PRIORITY);
//        } catch (Exception e) {
//
//        } finally {
//            lock.unlock();
//        }
//
//
//        return priority;
//    }
//
//    public static void updateGroupMyJobCount(String group, int i) {
//        try {
//            lock.lock();
//            if (group != null) {
//                groupMyJobCount.put(group, groupMyJobCount.containsKey(group) ? groupMyJobCount.get(group) + i : i);
//            }
//
//            int myJobCount = i == 1 ? MyJobCount.incrementAndGet() : i == -1 ? MyJobCount.decrementAndGet() : MyJobCount.get();
//
//            // 动态记录获取的JOB个数
//
//            String schedulerData = curator4Scheduler.getSchedulerInfo(Constant.LOCALHOST, Constant.ZK_ONLINE_SCHEDULER);
//            HashMap<String, String> schedulerInfo = new HashMap<>(8);
//            if (!StringHelper.isEmpty(schedulerData)) {
//                schedulerInfo = JSONHelper.toObject(schedulerData, HashMap.class);
//            }
//            schedulerInfo.put(MY_JOB_NUM, String.valueOf(myJobCount));
//            schedulerInfo.put(ALARM_JOB_NUM, String.valueOf(50));
//            schedulerInfo.put(MAX_JOB_NUM, String.valueOf(MyJobCount.get() + 2));
//            //ZK上存储Job一键转移开关
//            schedulerInfo.put(ENABLE_JOB_TRANSFER, schedulerInfo.get(ENABLE_JOB_TRANSFER) != null ? schedulerInfo.get(ENABLE_JOB_TRANSFER) : "false");
//            // ZK上更新负载均衡信息
//
//            LOGGER.info("updateGroupMyJobCount " + i);
//
//            curator4Scheduler.updateScheduler(Constant.LOCALHOST, JSONHelper.toString(schedulerInfo), Constant.ZK_ONLINE_SCHEDULER);
//        } catch (Exception e) {
//
//        } finally {
//            lock.unlock();
//        }
//
//    }
//}
