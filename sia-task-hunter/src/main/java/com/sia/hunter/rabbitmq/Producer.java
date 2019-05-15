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
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.rabbit.hunter.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
///**
// * @Author: huangqian
// * @Date: 2018/6/29 11:15
// * @Description: MQ发送端封装
// */
//@Component
//public class Producer {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);
//    /**
//     * 获得RabbitMQ模版实例
//     */
//    @Autowired
//    private RabbitTemplate instance;
//
//    /**
//     * 发布订阅模式
//     *
//     * @param exchangeName
//     * @param message
//     * @return
//     */
//    public boolean sendPubSub(String exchangeName, Object message) {
//
//        try {
//            instance.convertAndSend(exchangeName, "", message);
//        } catch (Exception ex) {
//            LOGGER.error("", ex);
//            return false;
//        }
//        return true;
//    }
//
//}
