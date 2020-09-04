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

package com.sia.task.quartz.exmple.main;

import com.sia.task.quartz.exception.SchedulerException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Main {


    public static void main(String[] args) throws Exception {
        boolean flag = false;
        List<String> stringList = new ArrayList<>();

        for (String str : stringList) {
            System.out.println(str);
            flag = true;
        }

        System.out.println(false);
        //start();
    }

    private static void start() throws SchedulerException, InterruptedException {
        QuartzScheduler quartzScheduler = new QuartzScheduler();
        quartzScheduler.addJob("g1", "n1", MyJob.class);
        //quartzScheduler.addJob("g2","n2", MyJob.class);

        log.info("===start===");
        quartzScheduler.start();

        Thread.sleep(5000);


        quartzScheduler.stop();
        log.info("===stop===");

        Thread.sleep(5000);

        quartzScheduler.start();
        quartzScheduler.addJob("g1", "n1", MyJob.class);
        log.info("===start===");
    }


}
