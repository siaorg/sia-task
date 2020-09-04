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

import com.sia.task.quartz.job.Job;
import com.sia.task.quartz.job.JobExecutionContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-05-16 13:10
 * @see
 **/
@Slf4j
public class MyJob implements Job {

    public void execute(JobExecutionContext context) {
        String name = context.getJobDetail().getKey().getName();
        String group = context.getJobDetail().getKey().getGroup();
        log.info(Thread.currentThread().getName() + " > > > > MyJob.execute " + "name :" + name + " group :" + group);
    }


    public static void main(String[] args) {
        comleteabvle2();
    }


    public static void comleteabvle2(){

        List<String> list = Arrays.asList("aaa","bbb","ccc","ddd");

        CompletableFuture<String> stringCompletableFuture = new CompletableFuture<>();
        for (String str : list) {
            //if ()
        }

        stringCompletableFuture.supplyAsync(() -> list);

        stringCompletableFuture = stringCompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
                log.info("1 >>>>>> ");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "hello ";
        });

        log.info("2 >>>>>> ");
        CompletableFuture<String> stringCompletableFuture1 = stringCompletableFuture.thenApply(s -> {
            String str = s + "world";
            log.info("3 >>>>>> " + str);
            return str;
        });



        stringCompletableFuture1.thenCombine(CompletableFuture.completedFuture(" java"),(s1,s2)-> s1+s2).thenAccept(System.out::println);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    public static void comleteabvle(){


        CompletableFuture<String> stringCompletableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "hello ";
        });

        CompletableFuture<String> stringCompletableFuture1 = stringCompletableFuture.thenApply(s -> {
            String str = s + "world";
            log.info("2 >>>>>> " + str);
            return str;
        });

        stringCompletableFuture1.thenCombine(CompletableFuture.completedFuture(" java"),(s1,s2)-> s1+s2).thenAccept(System.out::println);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
