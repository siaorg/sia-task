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

package com.sia.scheduler.thread.execute;

import com.sia.core.entity.JobMTask;
import com.sia.scheduler.thread.ExecutorPoolService;
import com.sia.scheduler.thread.TaskCallable;

import java.util.concurrent.ExecutorService;

/**
 * task 任务提交
 *
 * @description
 * @see
 * @author maozhengwei
 * @date 2019-03-22 20:10
 * @version V1.0.0
 **/
public class TaskCommit {


    public static void commit(JobMTask onlineTask) {

        ExecutorService executorService = ExecutorPoolService.getExecutorService(onlineTask.getJobKey());
        onlineTask.setOutParam(null);
        TaskExecute threadExecute = new TaskExecute(onlineTask);
        TaskCallable schedulerThread = new TaskCallable(threadExecute);
        executorService.submit(schedulerThread);
    }

}
