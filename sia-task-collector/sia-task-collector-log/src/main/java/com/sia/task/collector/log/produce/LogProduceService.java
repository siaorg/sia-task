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

package com.sia.task.collector.log.produce;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sia.task.collector.log.LogMessageShell;
import com.sia.task.core.log.LogStatusEnum;
import com.sia.task.core.task.DagTask;
import com.sia.task.core.util.Constant;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.concurrent.*;

/**
 * @author maozhengwei
 * @version V1.0.0
 * @data 2020/4/25 1:05 下午
 **/
@Slf4j
public class LogProduceService {

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(
            10,
            10,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(5000),
            new ThreadFactoryBuilder().setNameFormat("log-consumer-thread-%d").build(),
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    if (r instanceof FutureTask) {
                        FutureTask task = (FutureTask) r;
                        LogMessageShell logShell = null;
                        try {
                            Field declaredField = task.getClass().getDeclaredField("callable");
                            declaredField.setAccessible(true);
                            Callable callable = (Callable) declaredField.get(task);

                            Field taskField = callable.getClass().getDeclaredField("task");
                            taskField.setAccessible(true);
                            logShell = (LogMessageShell) taskField.get(callable);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            log.info(Constant.LOG_EX_PREFIX + " Rejected 日志消费-丢弃日志 ", e);
                        }

                        if (logShell != null) {
                            log.error(Constant.LOG_EX_PREFIX + "Rejected 日志消费线程-丢弃日志 {}" + logShell);
                        }
                    } else {
                        log.error(Constant.LOG_EX_PREFIX + "Rejected 日志消费线程-丢弃日志 {}" + r);
                    }
                }
            });


    public static void produceLogs(DagTask mTask, String message, LogStatusEnum statusEnum) {
        produce(mTask, message, statusEnum);
    }

    private static void produce(DagTask mTask, String message, LogStatusEnum statusEnum) {
        LogMessageShell logShell = new LogMessageShell();
        logShell.setMTask(mTask.baseCopy());
        logShell.setMessage(message);
        logShell.setStatusEnum(statusEnum);
        logShell.setTimer(new Date());
        log.info(Constant.LOG_PREFIX + " produce log - submit(logShell) [{}]", logShell);
        executor.submit(logShell);
    }
}
