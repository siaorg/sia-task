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

package com.sia.task.controller;

import com.sia.task.core.http.SiaHttpResponse;
import com.sia.task.hunter.annotation.OnlineTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/v1")
@Slf4j
public class V1Controller {


    @RequestMapping("/task-0s")
    @OnlineTask(description = "在线任务示例, success 0s", enableSerial = true)
    public String runTask(@RequestBody String param) {
        log.info("run task ---> " + param);
        return SiaHttpResponse.success("在线任务示例, success 0s");
    }


    @RequestMapping("/task2-3s")
    @OnlineTask(description = "在线任务示例2 success 3s", enableSerial = false)
    public String runTask2(@RequestBody String param) throws InterruptedException {
        log.info("run task ---> " + param);
        TimeUnit.SECONDS.sleep(3);
        return SiaHttpResponse.success("在线任务示例, success 3s");
    }

    @RequestMapping("/task3-8s")
    @OnlineTask(description = "在线任务示例3 success 8s", enableSerial = false)
    public String runTask3(@RequestBody String param) throws InterruptedException {
        log.info("run task ---> " + param);

        TimeUnit.SECONDS.sleep(8);
        return SiaHttpResponse.success("在线任务示例, success 8s");
    }


    @RequestMapping("/task4-fail")
    @OnlineTask(description = "在线任务示例4 fail null ", enableSerial = false)
    public String runTask4(@RequestBody String param) {
        log.info("run task ---> " + param);
        int i = 1 / 0;
        return SiaHttpResponse.success("在线任务示例, success 8s");
    }

    @RequestMapping("/task5-12s")
    @OnlineTask(description = "在线任务示例5", enableSerial = true)
    public String runTask5(@RequestBody String param) throws InterruptedException {
        log.info("run task ---> " + param);
        TimeUnit.SECONDS.sleep(12);
        return SiaHttpResponse.failure("在线任务示例, success 12s");
    }

    @RequestMapping("/task6-ex")
    @OnlineTask(description = "在线任务示例6", enableSerial = false)
    public String runTask6(@RequestBody String param) throws Exception {
        log.info("run task ---> " + param);

        if (param != null) {
            throw new Exception("aaaaaaaaaaaa");
        }
        return SiaHttpResponse.success("在线任务示例, success 8s");
    }
}
