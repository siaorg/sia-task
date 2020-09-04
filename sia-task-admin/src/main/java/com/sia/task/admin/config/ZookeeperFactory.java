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

package com.sia.task.admin.config;

import com.sia.task.core.util.Constant;
import com.sia.task.core.util.StringHelper;
import com.sia.task.integration.curator.Curator4Scheduler;
import com.sia.task.integration.curator.properties.ZookeeperConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

/**
 * ZookeeperFactory
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2018/4/18 11:10 上午
 **/
@Configuration
@Import(ZookeeperConfiguration.class)
@Slf4j
public class ZookeeperFactory {

    @Resource
    private ZookeeperConfiguration configuration;

    private static Curator4Scheduler curator4Scheduler;

    @Bean
    Curator4Scheduler getZookeeper() {
        log.info("try connect to Zookeeper...");
        if (StringHelper.isEmpty(configuration.getZooKeeperHosts())) {
            log.info("zooKeeperHosts 为空， 创建 Curator4Scheduler 需要配置[ sia.task.zookeeper.host-list: ]！！！");
            return null;
        }
        curator4Scheduler = new Curator4Scheduler(configuration, true);
        //添加监听
        ConnectionStateListener listener = (client, newState) -> {
            log.info(Constant.LOG_PREFIX + "sia-task-admin Zookeeper ConnectionState:" + newState.name());
            if (newState == ConnectionState.LOST) {
                while (true) {
                    try {
                        if (client.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                            log.info(Constant.LOG_PREFIX + "sia-task-admin Zookeeper Reconnected");
                            curator4Scheduler.getCuratorClient().addAllAuth(configuration.getDIGEST(), configuration.getAllAuth());
                            break;
                        }
                    } catch (Exception e) {
                        log.error(Constant.LOG_PREFIX + "Zookeeper Reconnect FAIL, please mailto [you email address]", e);
                    }
                }
            }
            curator4Scheduler.getCuratorClient().addAllAuth(configuration.getDIGEST(), configuration.getAllAuth());
        };
        curator4Scheduler.getCuratorClient().getCuratorFramework().getConnectionStateListenable().addListener(listener);
        log.info(Constant.LOG_PREFIX + "success connect to Zookeeper");
        return curator4Scheduler;
    }
}
