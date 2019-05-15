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

package com.sia.scheduler.thread;

import com.sia.scheduler.util.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * 线程池管理类
 *
 * @see
 * @author maozhengwei
 * @date 2019-04-27 10:56
 * @version V1.0.0
 **/
public class ExecutorPoolService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorPoolService.class);


    private static Map<String, ExecutorService> executorPool = new ConcurrentHashMap<>();

    /**
     * 通过 jobKey从资源池中获得一个线程池，该线程池只创建一次，不销毁。
     *
     * <p>
     * 如果该调度器上这个 jobKey 停止后再执行，那还是这个线程池！
     * <p>
     * 该线程池可能被重复使用。
     * <p>
     * 后续可以根据<b>JobGroup</b>来获取线程池，合理使用线程池，避免资源浪费
     *
     * 获取线程池资源，这里没有提供线程池资源的关闭接口，有两个原因：
     * <p>
     * （1）该jobKey可能再次被该调度器执行
     * <p>
     * （2）为了避免由关闭jobKey相关的线程池资源引起的并发问题
     *
     *  增加销毁逻辑，由于job可能存在废弃后永久性关停，考虑上述问题，进行关停 >> 20180325
     * @param jobKey
     * @return
     */
    public static ExecutorService getExecutorService(String jobKey) {

        ExecutorService exec = executorPool.get(jobKey);
        if (exec == null) {
            LOGGER.info(Constants.LOG_PREFIX + "Initialize thread pool for running jobs,job is {}",jobKey);
            exec = Executors.newCachedThreadPool();
            executorPool.putIfAbsent(jobKey, exec);
            exec = executorPool.get(jobKey);
        }
        return exec;
    }

    /**
     *  任务关停 释放线程池
     * @param jobKey
     */
    public static void releaseExecutorService(String jobKey){
        LOGGER.info(Constants.LOG_PREFIX + "Task shutdown, release thread pool,job is {}",jobKey);
        if (executorPool.containsKey(jobKey)) {
            executorPool.remove(jobKey);
        }
    }
}
