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

package com.sia.config;

import com.sia.config.web.constants.Constants;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

/**
 * sia-task-config boot entry
 * @see
 * @author maozhengwei
 * @date 2019-04-27 15:40
 * @version V1.0.0
 **/
@MapperScan({"com.sia.core.mapper"})
@SpringBootApplication(scanBasePackages = {"com.sia"})
@EnableTransactionManagement
public class TaskConfigApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskConfigApplication.class);

/**
     *
     * sia-task-config boot entry main method.
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(TaskConfigApplication.class, args);
        LOGGER.info(Constants.LOG_PREFIX + "TaskConfig Application start ok!");


    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
