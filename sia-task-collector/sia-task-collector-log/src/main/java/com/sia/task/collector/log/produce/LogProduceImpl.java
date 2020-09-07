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

package com.sia.task.collector.log.produce;

import com.sia.task.collector.log.util.MessageUtil;
import com.sia.task.core.log.ILogProducer;
import com.sia.task.core.log.LogStatusEnum;
import com.sia.task.core.task.DagTask;
import com.sia.task.core.util.Constant;
import lombok.extern.slf4j.Slf4j;

/**
 * LogProduceImpl, as the implementation class of ILogProducer, plug-in the scheduling log module based on the SPI convention。
 * <p>
 * note：
 * Do not easily change the package path of this class
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/7/2 11:14 上午
 * @see ILogProducer
 * /META-INF.services/com.sia.base.core.log.ILogProducer
 **/
@Slf4j
public class LogProduceImpl implements ILogProducer {

    @Override
    public void produce(DagTask task, String content, LogStatusEnum statusEnum) {
        if (statusEnum.isFail()) {
            try {
                MessageUtil.sendMess(task, content);
            } catch (Exception e) {
                log.error(Constant.LOG_EX_PREFIX + "记录日志-异常日志发送预警-出现错误 JobKey[{}], EmailContent[{}]", task.getJobKey(), content, e);
            }
        }
        LogProduceService.produceLogs(task, content, statusEnum);
    }
}
