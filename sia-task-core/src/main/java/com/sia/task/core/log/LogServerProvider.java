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

package com.sia.task.core.log;

import com.sia.task.core.task.DagTask;
import com.sia.task.core.util.Constant;
import com.sia.task.core.util.LoggerBackBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * LogServerProvider
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/7/2 11:17 上午
 * @see
 **/
@Slf4j
public class LogServerProvider {

    private static volatile List<ILogProducer> logServerProviders;

    /**
     * @param dagTask
     * @param content
     * @param statusEnum
     */
    public static void produce(DagTask dagTask, String content, LogStatusEnum statusEnum) {

        try {
            if (logServerProviders == null) {
                logServerProviders = buildLogServerSpi();
            }

            log.info(Constant.LOG_PREFIX + " logServerProviders [{}]", logServerProviders);
            logServerProviders.forEach(logProducer -> logProducer.produce(dagTask, content, statusEnum));

        } catch (Exception e) {
            LoggerBackBuilder.outputLogToLocal(dagTask, content, statusEnum);
            log.error(Constant.LOG_EX_PREFIX + " Exception: ", e);
        }
    }

    private static ServiceLoader buildServiceLoad() {
        return ServiceLoader.load(ILogProducer.class);
    }

    private static synchronized List<ILogProducer> buildLogServerSpi() {

        if (logServerProviders == null) {

            ServiceLoader serviceLoader = buildServiceLoad();
            Iterator iterator = serviceLoader.iterator();

            List list = new ArrayList();
            while (iterator.hasNext()) {
                ILogProducer logServerSpi = (ILogProducer) iterator.next();
                list.add(logServerSpi);
            }
            logServerProviders = list;
        }
        return logServerProviders;
    }
}
