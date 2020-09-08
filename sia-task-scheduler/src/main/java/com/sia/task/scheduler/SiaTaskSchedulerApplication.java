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

package com.sia.task.scheduler;


import com.sia.task.scheduler.annotations.EnableSiaTaskScheduler;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

/**
 * bootstrap
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/6/9 11:23 上午
 **/
@Slf4j
@MapperScan({"com.sia.task.mapper"})
@EnableSiaTaskScheduler
//@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.sia.task"})
public class SiaTaskSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SiaTaskSchedulerApplication.class, args);
        log.info("SiaTaskSchedulerApplication start OK!");
    }

    /**
     * 提供邮件服务使用调度发送邮件接口
     * 配合注册中心使用
     *
     * @return
     */
    @Bean("loadBalanced")
    @LoadBalanced
    public RestTemplate loadBalanced() {
        return new RestTemplate();
    }

    /**
     * 普通的RestTemplate实例bean
     *
     * @return
     */
    @Primary
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
