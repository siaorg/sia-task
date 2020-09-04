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

package com.sia.task.core.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.OptionHelper;
import com.sia.task.core.log.LogStatusEnum;
import com.sia.task.core.task.DagTask;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * LoggerBackBuilder
 *
 * @description
 * @see
 * @author maozhengwei
 * @data 2019-11-07 11:20
 * @version V1.0.0
 **/
public class LoggerBackBuilder {

    private static final String name = "scheduler-";

    private static final Map<String, Logger> loggerContainer = new HashMap();


    public static Logger getLogger(String filePrefix) {
        Logger logger = loggerContainer.get(filePrefix);
        if (null != logger) {
            return logger;
        }

        synchronized (LoggerBackBuilder.class) {
            logger = loggerContainer.get(filePrefix);
            if (null != logger) {
                return logger;
            }
            logger = loggerBuild(filePrefix);
            loggerContainer.put(filePrefix, logger);
        }

        return logger;
    }

    public static Logger removeLogger(String filePrefix){
        return loggerContainer.remove(filePrefix);
    }

    private static Logger loggerBuild(String filePrefix) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        Logger logger = context.getLogger(name + filePrefix);

        logger.setAdditive(false);

        RollingFileAppender appender = new RollingFileAppender();
        appender.setContext(context);
        appender.setName(name + filePrefix);
        appender.setFile(OptionHelper.substVars("${LOG_HOME}/log-" + name + filePrefix + ".log",context));
        appender.setAppend(true);
        appender.setPrudent(false);
        SizeAndTimeBasedRollingPolicy policy = new SizeAndTimeBasedRollingPolicy();
        String fp = OptionHelper.substVars("${LOG_HOME}/log-" + name + filePrefix + ".log.%d{yyyy-MM-dd}.%i",context);
        policy.setMaxFileSize(FileSize.valueOf("128MB"));
        policy.setFileNamePattern(fp);
        policy.setMaxHistory(180);
        policy.setTotalSizeCap(FileSize.valueOf("32GB"));
        policy.setParent(appender);
        policy.setContext(context);
        policy.start();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd/HH:mm:ss.SSS}|%X{localIp}|[%t] %-5level %logger{50} %line - %m%n");
        encoder.start();

        appender.setRollingPolicy(policy);
        appender.setEncoder(encoder);
        appender.start();
        logger.addAppender(appender);
        return logger;
    }

    public static void outputLogToLocal(DagTask dagTask, String content, LogStatusEnum statusEnum){
        Logger logger = LoggerBackBuilder.getLogger(dagTask.getJobKey());
        Map jobLog = new HashMap();
        jobLog.put("dagTask", dagTask.toString());
        jobLog.put("content", content);
        jobLog.put("LogStatusEnum", statusEnum);
        logger.info("local output of scheduling log - [{}]", jobLog);
    }
}
