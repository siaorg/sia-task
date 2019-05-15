/*-
 * <<
 * task
 * ==
 * Copyright (C) 2019 sia
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

package com.sia.scheduler.zk.monitor;

import com.sia.core.curator.Curator4Scheduler;
import com.sia.core.helper.StringHelper;
import com.sia.scheduler.util.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * ZookeeperFactory
 *
 * @see
 * @author maozhengwei
 * @date 2018-04-17 15:15
 * @version V1.0.0
 **/
@Configuration
public class ZookeeperFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperFactory.class);

    @Value("${zooKeeperHosts:}")
    protected String zooKeeperHosts;

    private static Curator4Scheduler curator4Scheduler;

    @Value("${ZK_ONLINE_ROOT_PATH:SkyWorldOnlineTask}")
    private String taskRoot;

    /**
     *用户权限
     */
    //ZK权限模式
    @Value("${DIGEST:digest}")
    private String digest;

    //所有权限（创建、读取、写入，删除，赋权）
    @Value("${ALLAUTH:SIA:SkyWorld}")
    private String allAuth;

    //只有创建权限
    @Value("${CREATEAUTH:guest:guest}")
    private String createAuth;

    /**
     * 依赖注入，获取Curator4Scheduler
     *
     * @return
     */
    @Bean
    Curator4Scheduler getCurator4SchedulerInstance() {

        if (curator4Scheduler == null) {
            LOGGER.info(Constants.LOG_PREFIX + "try to init Curator4Scheduler");
            if (StringHelper.isEmpty(zooKeeperHosts)) {
                LOGGER.info("zooKeeperHosts 为空， 创建 Curator4Scheduler 需要配置 scheduler.zooKeeperHosts！！！");
                return null;
            }
            curator4Scheduler = new Curator4Scheduler(zooKeeperHosts,taskRoot,digest,allAuth,createAuth,true);
        }
        return curator4Scheduler;
    }

}
