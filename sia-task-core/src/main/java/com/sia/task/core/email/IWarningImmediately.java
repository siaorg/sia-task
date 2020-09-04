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

package com.sia.task.core.email;

/**
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/7/6 2:16 下午
 * @see
 **/
public interface IWarningImmediately {

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
    void send(String sendTo, String content, String subject, String primary, long elapse) throws Exception;
}
