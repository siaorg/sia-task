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

package com.sia.scheduler.http.route;

import com.sia.core.entity.JobMTask;
import com.sia.core.helper.JSONHelper;
import com.sia.scheduler.util.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


/**
 *
 * RouteSharding
 *
 * @description
 * @see
 * @author maozhengwei
 * @date 2019-04-28 19:08
 * @version V1.0.0
 **/
@Deprecated
public class ExecutorRouteSharding extends AbstractExecutorRouter {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorRouteSharding.class);

    private static Map<String, Map<String, PriorityBlockingQueue<String>>> shardingQuen = new ConcurrentHashMap<>();

    private static Map<String, Map<String, PriorityBlockingQueue<String>>> instanceQuen = new ConcurrentHashMap<>();

    private static Map<String, Map<String, AtomicInteger>> shardingCount = new ConcurrentHashMap<>();

    private static Map<String, Map<String, AtomicInteger>> maxCount = new ConcurrentHashMap<>();


    /**
     * enums run job
     *
     * @param jobMTask
     * @param addressList
     * @return ReturnT.content: final address
     */
    @Override
    public String routeRun(JobMTask jobMTask, List<String> addressList) {
        if (addressList.isEmpty()) {
            logger.warn(Constants.LOG_PREFIX + " enums address fail, ApplicationInstanceList is empty, jobKey={} taskKey={}", jobMTask.getJobKey(), jobMTask.getTaskKey());
            return null;
        } else {
            return route(jobMTask, addressList);
        }
    }

    private String route(JobMTask task, List<String> addressList) {
        return getInstance(task,addressList).peek();
    }


    public static String getSharding(JobMTask jobMTask) {
        if (!shardingQuen.containsKey(jobMTask.getJobKey())) {
            initSharding(jobMTask);
        }
        return shardingQuen.get(jobMTask.getJobKey()).get(jobMTask.getTaskKey()).poll();
    }


    public static PriorityBlockingQueue<String> getInstance(JobMTask jobMTask, List<String> addressList) {
        if (!instanceQuen.containsKey(jobMTask.getJobKey())) {
            initInstance(jobMTask, addressList);
        }
        return instanceQuen.get(jobMTask.getJobKey()).get(jobMTask.getTaskKey());
    }


    /**
     * finishedShardingCount
     * @param taskKey
     * @return
     */
    public static int finishedShardingCount(JobMTask taskKey) {
        return shardingCount.get(taskKey.getJobKey()).get(taskKey.getTaskKey()).decrementAndGet();
    }

    /**
     *
     * maxExecuteCount
     * @param taskKey
     * @return
     */
    public static int maxExecuteCount(JobMTask taskKey) {
        return maxCount.get(taskKey.getJobKey()).get(taskKey.getTaskKey()).decrementAndGet();
    }

    /**
     *
     * @param jobMTask
     */
    public static void initSharding(JobMTask jobMTask) {
        PriorityBlockingQueue sharding = new PriorityBlockingQueue();
        Map<String, PriorityBlockingQueue<String>> shardingTask = new ConcurrentHashMap<>();
        if (Constants.FROM_UI.equals(jobMTask.getInputType())) {
            Arrays.asList(jobMTask.getInputValue().split(Constants.REGEX_COMMA)).forEach(str ->
                    sharding.put(str));
            shardingTask.put(jobMTask.getTaskKey(), sharding);
        } else {
            String outParam = jobMTask.getPreTask().get(0).getOutParam();
            Map<String, String> map = JSONHelper.toObject(outParam, Map.class);
            Arrays.asList(map.get("result").split(Constants.REGEX_COMMA)).forEach(str ->
                    sharding.put(str));
        }
        shardingTask.put(jobMTask.getTaskKey(), sharding);
        Map<String, AtomicInteger> cout = new HashMap<>();
        Map<String, AtomicInteger> max = new HashMap<>();
        AtomicInteger successCount = new AtomicInteger(sharding.size());
        PriorityBlockingQueue<String> instances = instanceQuen.get(jobMTask.getJobKey()).get(jobMTask.getTaskKey());
        AtomicInteger maxExecuteCount = new AtomicInteger(sharding.size() + instances.size());
        cout.put(jobMTask.getTaskKey(),successCount);
        max.put(jobMTask.getTaskKey(),maxExecuteCount);
        shardingCount.put(jobMTask.getJobKey(), cout);
        shardingQuen.put(jobMTask.getJobKey(), shardingTask);
        maxCount.put(jobMTask.getJobKey(),max);
    }

    /**
     *
     * @param jobMTask
     * @param addressList
     */
    public static void initInstance(JobMTask jobMTask, List<String> addressList){
        PriorityBlockingQueue instance = new PriorityBlockingQueue();
        Map<String, PriorityBlockingQueue<String>> instanceMap = new ConcurrentHashMap<>();
        addressList.forEach(str ->
                instance.put(str));
        instanceMap.put(jobMTask.getTaskKey(), instance);
        instanceQuen.put(jobMTask.getJobKey(), instanceMap);
    }

    /**
     * 释放分片任务和执行器
     *
     * @param jobMTask
     * @param isSuccess
     */
    public synchronized static void release(JobMTask jobMTask,boolean isSuccess) {
        if (!isSuccess){
            PriorityBlockingQueue<String> priorityBlockingQueue = shardingQuen.get(jobMTask.getJobKey()).get(jobMTask.getTaskKey());
            boolean contains = priorityBlockingQueue.contains(jobMTask.getInputValue());
            if (!contains){
                priorityBlockingQueue.put(jobMTask.getInputValue());
            }
            return;
        }

        PriorityBlockingQueue<String> ins = instanceQuen.get(jobMTask.getJobKey()).get(jobMTask.getTaskKey());
        boolean contains = ins.contains(jobMTask.getCurrentHandler());
        if (!contains){
            ins.put(jobMTask.getCurrentHandler());
        }
    }

    /**
     * 删除缓存数据
     */
    public static void clean(String jobkey) {
        LOGGER.info(Constants.LOG_PREFIX + " 尝试删除分片任务数据");
        Map<String, PriorityBlockingQueue<String>> remove = shardingQuen.remove(jobkey);
        LOGGER.info(Constants.LOG_PREFIX + " 尝试删除分片任务数据 {}",remove);
        Map<String, PriorityBlockingQueue<String>> remove1 = instanceQuen.remove(jobkey);
        LOGGER.info(Constants.LOG_PREFIX + " 尝试删除分片执行器数据 {}",remove1);
    }

}
