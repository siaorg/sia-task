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

package com.sia.task.collector.log.email;

import com.sia.task.core.util.JsonHelper;
import com.sia.task.core.util.StringHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * 邮件预警服务
 *
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2020/5/13 3:58 下午
 * @see
 **/
public class EmailMessageService {

    private static final Logger log = LoggerFactory.getLogger(EmailMessageService.class);

    @Value("${sia.task.alarm.email.default-email:}")
    protected String adminEmailers;

    @Value("${sia.task.alarm.email.service-id:}")
    protected String alarmServiceId;

    @Autowired
    @Qualifier("loadBalanced")
    private RestTemplate restTemplate;

    /**
     * <p>
     * 是否发送邮件 增加邮件压制功能，在压制时间内，指定压制关键字的邮件只会发一次
     *
     * @param sendTo  预警邮箱，用逗号(,)分隔
     * @param content 邮件正文
     * @param subject 邮件主题
     * @param primary 压制关键字
     * @param elapse  压制时间，单位：毫秒
     */
    public String sendLimitedEmail(String sendTo, String content, String subject, String primary, long elapse) throws Exception {

        if (StringHelper.isEmpty(alarmServiceId)) {
            log.warn(" Failed to send email by method sendLimitedEmail : alarmServiceId is null");
        }
        if (StringHelper.isEmpty(sendTo) && StringHelper.isEmpty(adminEmailers)) {
            log.warn(" Failed to send email by method sendLimitedEmail : Early warning mailbox address is empty");
        }
        String[] all = concatAll(adminEmailers.split(","), sendTo.split(","));
        MailFormat mailFormat = new MailFormat(subject, all, content, primary, elapse);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        String result = restTemplate.postForEntity(alarmServiceId, new HttpEntity<>(JsonHelper.toString(mailFormat), headers), String.class).getBody();
        log.info("Email send finished: {}, {}", result, primary);
        return result;
    }

    public static <T> T[] concatAll(T[] array, T[]... arrays) {
        int totalLength = array.length;
        for (T[] a : arrays) {
            totalLength += a.length;
        }
        T[] copy = Arrays.copyOf(array, totalLength);
        int offset = array.length;
        for (T[] a : arrays) {
            System.arraycopy(a, 0, copy, offset, a.length);
            offset += a.length;
        }
        return copy;
    }

    /**
     * 调用邮件服务的参数格式
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private class MailFormat {

        /**
         * 邮件主题
         */
        private String subject;

        /**
         * 预警邮箱，用逗号(,)分隔
         */
        private String[] mailto;

        /**
         * 邮件正文
         */
        private String content;

        /**
         * 压制关键字
         */
        private String primary;

        /**
         * 压制时间，单位：毫秒
         */
        private Long elapse;
    }
}

