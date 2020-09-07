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

package com.sia.task.register.zookeeper.core;

import com.sia.task.core.IMetadataUpload;
import com.sia.task.core.util.Constant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;

import javax.annotation.Resource;

/**
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/7/21 11:35 上午
 * @see
 **/
@Slf4j
@AllArgsConstructor
public class ConnectionStateListener4Client implements ConnectionStateListener {

    @Resource
    private IMetadataUpload uploadTaskMetaData;

    @Override
    public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
        log.info(Constant.LOG_PREFIX + "TaskRegister Zookeeper ConnectionState:" + connectionState.name());
        if (connectionState == ConnectionState.LOST) {
            while (true) {
                try {
                    if (curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                        log.info(Constant.LOG_PREFIX + "TaskRegister Zookeeper Reconnected");
                        uploadTaskMetaData.uploadTaskMetaData();
                        log.info(Constant.LOG_PREFIX + "TaskRegister onlineTaskUpload Redo");
                        break;
                    }
                } catch (InterruptedException e) {
                    log.error(Constant.LOG_PREFIX + "Zookeeper Reconnect FAIL, please mailto [you email address]", e);
                }
            }
        }
    }
}
