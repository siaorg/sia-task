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

package com.sia.scheduler;

import com.sia.scheduler.util.constant.Constants;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

/**
 * Scheduling project startup class
 *
 * @description
 * @see
 * @author maozhengwei
 * @date 2018-04-28 15:40
 * @version V1.0.0
 **/
@MapperScan({"com.sia.core.mapper"})
@SpringBootApplication(scanBasePackages = {"com.sia"})
@EnableTransactionManagement
public class SchedulerApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerApplication.class);


    public static void main(String[] args) {

        SpringApplication.run(SchedulerApplication.class, args);
        LOGGER.info(Constants.LOG_PREFIX + "SchedulerApplication start OK!");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
