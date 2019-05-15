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

package com.sia.scheduler.service.common;

import com.sia.core.helper.JSONHelper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author: MAOZW
 * @Description: EmailService description: 邮件预警服务
 * @date 2018/4/1811:10
 */
@Component
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Value("${SKYTRAIN_DEFAULT_EMAIL}")
    protected String adminEmailers;

    @Value("${EMAIL_SERVICE_REQUESTPATH}")
    protected String requestUrl;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * <p>
     * 是否发送邮件 增加邮件压制功能，在压制时间内，指定压制关键字的邮件只会发一次
     * 
     * 
     * @param sendTo
     *            预警邮箱，用逗号(,)分隔
     * @param content
     *            邮件正文
     * @param subject
     *            邮件主题
     * @param primary
     *            压制关键字
     * @param elapse
     *            压制时间，单位：毫秒
     * 
     */
    public void sendLimitedEmail(String sendTo, String content, String subject, String primary, long elapse){

        MailFormat mailFormat = new MailFormat();

        mailFormat.setSubject(subject);
        mailFormat.setContent(content);
        mailFormat.setPrimary(primary);
        mailFormat.setElapse(elapse);

        String[] defaultReceivers = adminEmailers.split(",");
        String[] configReceivers = {};
        if (sendTo != null && sendTo.length() > 0) {
            configReceivers = sendTo.split(",");
        }

        String[] emails = new String[defaultReceivers.length + configReceivers.length];
        System.arraycopy(defaultReceivers, 0, emails, 0, defaultReceivers.length);
        System.arraycopy(configReceivers, 0, emails, defaultReceivers.length, configReceivers.length);
        mailFormat.setMailto(emails);

        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());

        HttpEntity<String> entity = new HttpEntity<String>(JSONHelper.toString(mailFormat), headers);

        String result = restTemplate.postForEntity(requestUrl, entity, String.class).getBody();
        LOGGER.info("Email send finished: " + result);
    }

    /**
     * 发送邮件
     * @param sendTo
     * @param content
     * @param subject
     */
    public void sendEmail(String sendTo, String content, String subject){

        String primary = null;
        long elapse = 0L;

        sendLimitedEmail(sendTo, content, subject, primary, elapse);
    }

}

/**
 * 调用邮件服务的参数格式
 */
@Data
class MailFormat{

    /**  邮件主题 */
    private String subject;

    /** 预警邮箱，用逗号(,)分隔 */
    private String[] mailto;

    /** 邮件正文 */
    private String content;

    /** 压制关键字 */
    private String primary;

    /** 压制时间，单位：毫秒 */
    private Long elapse;
}