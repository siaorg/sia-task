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

import com.sia.task.core.util.Constant;
import com.sia.task.core.util.JsonHelper;
import com.sia.task.core.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 宜信人服务号微信消息接入服务
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2020/5/13 3:58 下午
 **/
public class WeCatMessageService {

    private static final Logger log = LoggerFactory.getLogger(WeCatMessageService.class);

    @Value("${sia.task.alarm.email.default-email:}")
    protected String adminEmailers;

    @Value("${sia.task.alarm.wechat.token-service-id:}")
    protected String weChatTokenServiceId;

    @Value("${sia.task.alarm.wechat.push-service-id:}")
    protected String weChatPushServiceId;

    @Value("${sia.task.alarm.wechat.CE_ID:}")
    protected String weChatCeId;

    @Value("${sia.task.alarm.wechat.sysId:}")
    protected String weChatSysId;

    @Value("${sia.task.alarm.wechat.CE_FUNC:}")
    protected String weChatCeFunc;

    @Value("${sia.task.alarm.wechat.POST_CE_FUNC:}")
    protected String weChatPostCeFunc;

    @Value("${sia.task.alarm.wechat.CE_PARAM:}")
    protected String weChatCeParam;

    @Value("${sia.task.alarm.wechat.WarningRange:SIA-TASK}")
    protected String weChatWarningRange;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 获取 token
     *
     * @return
     */
    private String buildToken() {
        if (StringHelper.isEmpty(weChatTokenServiceId)) {
            log.error(" Failed to send email by method buildToken : weChatTokenServiceId is null");
        }

        String url = weChatTokenServiceId + "?CE_ID={CE_ID}&CE_FUNC={CE_FUNC}&CE_PARAM={CE_PARAM}";
        String response = restTemplate.getForObject(url, String.class, weChatCeId, weChatCeFunc, weChatCeParam);
        Map<String, String> map = JsonHelper.toObject(response, Map.class);
        String token = map.get("CE_RESPONSE");
        log.info(Constant.LOG_PREFIX + " build weChat token : " + response);
        return token;
    }

    /**
     * <p>
     * 是否发送邮件 增加邮件压制功能，在压制时间内，指定压制关键字的邮件只会发一次
     *
     * @param sendTo      预警邮箱，用逗号(,)分隔
     * @param content     邮件正文
     * @param subject     邮件主题
     * @param jobKey      关键字
     * @param warningType 预警类别
     * @param alarmTime   预警时间
     * @throws Exception
     */
    public void sendWeChatMessage(String sendTo, String content, String subject, String jobKey, String warningType, String alarmTime) throws Exception {
        // to something
    }
}

