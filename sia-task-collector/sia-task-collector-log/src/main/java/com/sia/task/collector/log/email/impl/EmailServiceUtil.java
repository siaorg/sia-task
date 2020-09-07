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

package com.sia.task.collector.log.email.impl;

import com.sia.task.collector.log.context.SpringApplicationContext;
import com.sia.task.collector.log.email.EmailMessageService;
import com.sia.task.collector.log.email.WeCatMessageService;
import com.sia.task.core.task.DagTask;
import com.sia.task.core.util.JsonHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2020/5/13 10:34 上午
 * @see
 **/
public class EmailServiceUtil {

    public static String sendEmail4Limited(String sendTo, String content, String subject, String primary, long elapse) throws Exception {
        return sendLimitedEmail(sendTo, content, subject, primary, elapse);
    }

    private static String sendLimitedEmail(String sendTo, String content, String subject, String primary, long elapse) throws Exception {
        EmailMessageService emailService = SpringApplicationContext.getEmailService();
        return emailService.sendLimitedEmail(sendTo, content, subject, primary, elapse);
    }

    public static void sendEmail4WeChat(String sendTo, String content, String subject, String jobKey, String warningType, String alarmTime) throws Exception {
        WeCatMessageService weCatMessageService = SpringApplicationContext.getWeCatMessageService();
        weCatMessageService.sendWeChatMessage(sendTo, content, subject, jobKey, warningType, alarmTime);
    }

    /**
     * 邮件Content的内容
     *
     * @param task
     * @param ex
     * @param message
     * @return
     */
    public static String mapToMessage(DagTask task, Throwable ex, String message) {
        Map<String, Object> content = new HashMap<>(4);
        content.put("Task", task.toString());
        content.put("Exception", ex);
        content.put("Message", message);
        return JsonHelper.toString(content);
    }
}
