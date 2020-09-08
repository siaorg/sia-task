
package com.sia.task.register.zookeeper.core;

import com.sia.task.core.util.StringHelper;
import com.sia.task.integration.curator.Curator4Scheduler;
import com.sia.task.integration.curator.CuratorClient;
import com.sia.task.integration.curator.properties.ZookeeperConfiguration;
import com.sia.task.register.zookeeper.util.ClientCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import javax.annotation.Resource;

/**
 * ZookeeperFactory
 *
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-10-23 18:20
 * @see
 **/
public class ZookeeperFactory {

    private static final Logger log = LoggerFactory.getLogger(ZookeeperFactory.class);

    private Curator4Scheduler curator4Scheduler;

    private CuratorClient curatorClient;

    @Resource
    private ZookeeperConfiguration configuration;

    /**
     * 依赖注入，获取Curator4Scheduler
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingClass(value = "com.sia.hunter.annotation.OnlineTask")
    public Curator4Scheduler getCurator4SchedulerInstance() {

        if (curator4Scheduler == null) {
            if (StringHelper.isEmpty(configuration.getZooKeeperHosts())) {
                log.error("get zooKeeperHosts by @Value(\"${sia.task.zookeeper.host-list:}\") is null,check your config item [sia.task.zookeeper.host-list]");
                System.exit(500);
            }
            log.info("Initialize zk client instance <<curator4Scheduler>> using configuration such as: zooKeeperHosts [{}], taskRootPath [{}]", configuration.getZooKeeperHosts(), configuration.getTaskRoot());
            curator4Scheduler = new Curator4Scheduler(configuration, true);
        }
        return curator4Scheduler;
    }

    /**
     * 依赖注入，获取 CuratorClient
     *
     * @return
     */
    @Bean
    @Conditional(ClientCondition.class)
    public CuratorClient getCurator4ClientInstance() {

        if (curatorClient == null) {
            if (StringHelper.isEmpty(configuration.getZooKeeperHosts())) {
                log.error("get zooKeeperHosts by @Value(\"${sia.task.zookeeper.host-list:}\") is null,check your config item [sia.task.zookeeper.host-list]");
                System.exit(500);
            }
            log.info("Initialize zk client instance <<CuratorClient>> using configuration such as: zooKeeperHosts [{}], taskRootPath [{}]", configuration.getZooKeeperHosts(), configuration.getTaskRoot());
            curatorClient = new CuratorClient(configuration.getZooKeeperHosts(), configuration.getRETRY_TIMES(), configuration.getSLEEP_MS_BETWEEN_RETRIES());
        }
        return curatorClient;
    }

}
