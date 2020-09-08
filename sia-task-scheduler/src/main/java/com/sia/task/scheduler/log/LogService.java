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

package com.sia.task.scheduler.log;

import com.sia.task.core.log.LogServerProvider;
import com.sia.task.core.log.LogStatusEnum;
import com.sia.task.core.task.DagTask;
import com.sia.task.core.util.Constant;
import com.sia.task.core.util.LoggerBackBuilder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Schedule log sending
 * <p>
 * Determine whether to rely on log components, and if so, use third-party log components for log delivery,
 * if not print the local file directly。
 * <p>
 * If a third-party log component is used, then log printing is not performed locally。
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/8/12 10:48 上午
 **/
@Slf4j
public class LogService {

    @Setter
    private static boolean logCollectorEnabled;

    public static void produceLog(DagTask dagTask, String content, LogStatusEnum statusEnum) {
        if (ifNecessaryLogSend()) {
            LogServerProvider.produce(dagTask, content, statusEnum);
            return;
        }
        LoggerBackBuilder.outputLogToLocal(dagTask, content, statusEnum);
        log.info(Constant.LOG_PREFIX + " local output of scheduling log -  statusEnum:[{}], dagTask:[{}] , content:[{}]", statusEnum, dagTask, content);
    }

    private static boolean ifNecessaryLogSend() {

        if (logCollectorEnabled) {
            return true;
        }
        return true;
    }
}
