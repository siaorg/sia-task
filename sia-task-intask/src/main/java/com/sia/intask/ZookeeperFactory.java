package com.sia.intask;

import com.sia.core.curator.Curator4Scheduler;
import com.sia.core.helper.StringHelper;
import com.sia.hunter.constant.OnlineTaskConstant;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: MAOZW
 * @Description: ZookeeperFactory
 * @date 2018/4/1811:10
 */
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


    @Bean
    Curator4Scheduler getZookeeper() {

        LOGGER.info("try to init Curator4Scheduler");
        if (StringHelper.isEmpty(zooKeeperHosts)) {
            LOGGER.info("zooKeeperHosts 为空， 创建 Curator4Scheduler 需要配置zooKeeperHosts！！！");

            return null;
        }

        curator4Scheduler = new Curator4Scheduler(zooKeeperHosts,taskRoot,"digest",allAuth,createAuth,true);


        //添加监听
        ConnectionStateListener listener = (client, newState) -> {

            LOGGER.info(OnlineTaskConstant.LOGPREFIX + "OnlineTaskRegister Zookeeper ConnectionState:"
                    + newState.name());

            if (newState == ConnectionState.LOST) {
                while (true) {
                    try {
                        if (client.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                            LOGGER.info(OnlineTaskConstant.LOGPREFIX
                                    + "OnlineTaskRegister Zookeeper Reconnected");
                            //initJobZookeeper();
                            /**
                             *  对用户进行授权
                             */
                            curator4Scheduler.getCuratorClient().addAllAuth(digest,allAuth);
                            LOGGER.info(OnlineTaskConstant.LOGPREFIX
                                    + "OnlineTaskRegister onlineTaskUpload Redo");

                            break;
                        }
                    } catch (InterruptedException e) {
                        LOGGER.error(OnlineTaskConstant.LOGPREFIX
                                        + "Zookeeper Reconnect FAIL, please mailto [sia.list@creditease.cn]",
                                e);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        curator4Scheduler.getCuratorClient().getCuratorFramework().getConnectionStateListenable().addListener(listener);
        LOGGER.info("success connect to Zookeeper");


        return curator4Scheduler;
    }
}
