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

package com.sia.task.collector.log.util;

import com.sia.task.collector.log.context.SpringApplicationContext;
import com.sia.task.collector.log.email.impl.EmailServiceUtil;
import com.sia.task.core.task.DagTask;
import com.sia.task.core.util.JsonHelper;

import java.util.HashMap;
import java.util.Map;

public class MessageUtil {

    public static void sendMess(DagTask task, String content) throws Exception {
        Map<String, Object> con = new HashMap<>();
        con.put("task", task.toString());
        con.put("content", content);

        String emailContent = JsonHelper.toString(con);
        String limited = EmailServiceUtil.sendEmail4Limited(task.getJobAlarmEmail(),
                emailContent,
                SpringApplicationContext.getEmailSubject(),
                task.getJobKey(),
                SpringApplicationContext.getEmailAlarmElapse()
        );
//                if (LogMessageConstant.emailResponseMessageSuccess.equals(limited)) {
//                    EmailServiceUtil.sendEmail4WeChat(task.getJobAlarmEmail(),
//                            content,
//                            SpringApplicationContext.getEmailSubject(),
//                            task.getJobKey(),
//                            "任务异常",
//                            DateTimeFormatter.ofPattern("y-M-d H:m:s").format(LocalDateTime.now()));
//                }
    }
}
