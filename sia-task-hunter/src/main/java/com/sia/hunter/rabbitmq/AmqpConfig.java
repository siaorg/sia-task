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

//package com.sia.hunter.rabbitmq;
//
//import org.springframework.amqp.hunter.Binding;
//import org.springframework.amqp.hunter.BindingBuilder;
//import org.springframework.amqp.hunter.FanoutExchange;
//import org.springframework.amqp.hunter.Queue;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * @Author: huangqian
// * @Date: 2018/6/29 11:14
// * @Description: MQ配置
// */
//@Configuration
//public class AmqpConfig {
//
//    public static final String ONLINE_TASK_EX_NAME = "SKYWORLD_ONLINE_TASK_EX_NAME";
//
//    public static final String ONLINE_TASK_QUEUE_NAME = "SKYWORLD_ONLINE_TASK_QUEUE_NAME";
//
//    @Bean(ONLINE_TASK_QUEUE_NAME)
//    Queue pubsub_queue_one() {
//        return new Queue(ONLINE_TASK_QUEUE_NAME, true); // 队列持久
//    }
//
//    @Bean(ONLINE_TASK_EX_NAME)
//    FanoutExchange pubsub_exchange_one() {
//        return new FanoutExchange(ONLINE_TASK_EX_NAME);
//    }
//
//    @Bean
//    Binding binding() {
//        return BindingBuilder.bind(pubsub_queue_one()).to(pubsub_exchange_one());
//    }
//
//}
