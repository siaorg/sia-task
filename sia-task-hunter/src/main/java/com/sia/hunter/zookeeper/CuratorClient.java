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

package com.sia.hunter.zookeeper;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sia.hunter.constant.OnlineTaskConstant;
import com.sia.hunter.helper.StringHelper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;

/**
 *
 *
 * @description CuratorClient
 * @see
 * @author pengfeili23
 * @date 2018-07-11 16:11:19
 * @version V1.0.0
 **/
@Component
public class CuratorClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(CuratorClient.class);

	@Value("${zooKeeperHosts:}")
	protected String zooKeeperHosts;

	/**
	 * CuratorFramework instance
	 */
	private CuratorFramework client = null;

	/**
	 * 用于注解调用
	 */
	public CuratorClient() {
		shutdownHook();
	}

	/**
	 * CuratorFramework instance
	 */
	@Bean(name = "CuratorFramework")
	public synchronized CuratorFramework client() {
		if (StringHelper.isEmpty(zooKeeperHosts)) {
			LOGGER.error(OnlineTaskConstant.LOGPREFIX + "请配置zookeeper地址，e.g. [zooKeeperHosts: 127.0.0.1:2181]");
			return null;
		}
		if (this.client == null) {
			this.client = CuratorFrameworkFactory.newClient(zooKeeperHosts,
					new RetryNTimes(OnlineTaskConstant.RETRY_TIMES, OnlineTaskConstant.SLEEP_MS_BETWEEN_RETRIES));
			this.client.start();
			ConnectionStateListener listener = new ConnectionStateListener() {

				@Override
				public void stateChanged(CuratorFramework client, ConnectionState newState) {

					LOGGER.info(OnlineTaskConstant.LOGPREFIX + "Zookeeper ConnectionState:" + newState.name());

				}
			};
			ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Zookeeper-ConnectionState-%d").build();
			ExecutorService pool =new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
			this.client.getConnectionStateListenable().addListener(listener, pool);
			LOGGER.info(OnlineTaskConstant.LOGPREFIX + "success connect to Zookeeper: " + zooKeeperHosts);
		}
		return this.client;
	}

	/**
	 *
	 * get CuratorFramework for some use (e.g. lock)
	 * {@link } can be checked for the result.
	 * @param
	 * @return
	 * @throws
	 */
	public CuratorFramework getCuratorFramework() {

		return client();
	}

	/**
	 *
	 * add create authorization, can only create children in give path
	 * {@link } can be checked for the result.
	 * @param
	 * @return
	 * @throws
	 */
	public void addCreateAuth() {

		try {
			client().getZookeeperClient().getZooKeeper().addAuthInfo(OnlineTaskConstant.DIGEST,
					OnlineTaskConstant.CREATEAUTH.getBytes());
			LOGGER.info(OnlineTaskConstant.LOGPREFIX + "addCreateAuth success");
		} catch (Exception e) {
			LOGGER.info(OnlineTaskConstant.LOGPREFIX + "addCreateAuth fail: ", e);
		}
	}

	/**
	 *
	 * createPersistentZKNode, creatingParentsIfNeeded for given path,
	 * CreateMode.PERSISTENT
	 * {@link } can be checked for the result.
	 * @param
	 * @return
	 * @throws
	 */
	public boolean createPersistentZKNode(String path, String data) {

		if (StringHelper.isEmpty(path) || isExists(path) || data == null) {
			return false;
		}
		try {

			String zkPath = client().create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path,
					data.getBytes());
			LOGGER.info(OnlineTaskConstant.LOGPREFIX + "createPersistentZKNode，创建节点成功，节点地址:" + zkPath);
			return true;
		} catch (Exception e) {
			LOGGER.error(
					OnlineTaskConstant.LOGPREFIX + "createPersistentZKNode，创建节点失败:" + e.getMessage() + "，path:" + path,
					e);
		}
		return false;
	}

	/**
	 *
	 * createPersistentZKNode, set default value
	 * {@link } can be checked for the result.
	 * @param
	 * @return
	 * @throws
	 */
	public boolean createPersistentZKNode(String path) {

		return createPersistentZKNode(path, OnlineTaskConstant.ZK_DEFAULT_VALUE);
	}

	/**
	 *
	 * createEphemeralZKNode, creatingParentsIfNeeded for given path, leaf node is
	 * CreateMode.EPHEMERAL
	 * {@link } can be checked for the result.
	 * @param
	 * @return
	 * @throws
	 */
	public boolean createEphemeralZKNode(String path, String data) {

		if (StringHelper.isEmpty(path) || isExists(path) || data == null) {
			return false;
		}
		try {

			String zkPath = client().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path,
					data.getBytes());
			LOGGER.info(OnlineTaskConstant.LOGPREFIX + "createEphemeralZKNode，创建节点成功，节点地址:" + zkPath);
			return true;
		} catch (Exception e) {
			LOGGER.error(
					OnlineTaskConstant.LOGPREFIX + "createEphemeralZKNode，创建节点失败:" + e.getMessage() + "，path:" + path,
					e);
		}
		return false;
	}

	/**
	 *
	 * createEphemeralZKNode, set default value
	 * {@link } can be checked for the result.
	 * @param
	 * @return
	 * @throws
	 */
	public boolean createEphemeralZKNode(String path) {

		return createEphemeralZKNode(path, OnlineTaskConstant.ZK_DEFAULT_VALUE);
	}

	/**
	 *
	 * setData
	 * {@link } can be checked for the result.
	 * @param
	 * @return
	 * @throws
	 */
	public boolean setData(String path, String data) {

		if (!isExists(path) || data == null) {
			return false;
		}
		try {

			Stat stat = client().setData().forPath(path, data.getBytes());
			LOGGER.info(OnlineTaskConstant.LOGPREFIX + "setData，更新数据成功, path:" + path + ", stat: " + stat);
			return true;
		} catch (Exception e) {
			LOGGER.error("setData，更新节点数据失败:" + e.getMessage() + "，path:" + path, e);
		}
		return false;
	}

	/**
	 *
	 * may return null if path not exists
	 * {@link } can be checked for the result.
	 * @param
	 * @return
	 * @throws
	 */
	public String getData(String path) {

		String response = null;
		if (!isExists(path)) {
			return response;
		}
		try {
			byte[] datas = client().getData().forPath(path);
			response = new String(datas, "utf-8");
			LOGGER.info("读取数据成功, path:" + path + ", content:" + response);
		} catch (Exception e) {
			LOGGER.error(OnlineTaskConstant.LOGPREFIX + "getData，读取数据失败! path: " + path + ", errMsg:" + e.getMessage(),
					e);
		}
		return response;
	}

	/**
	 *
	 * may return null if path not exists
	 * {@link } can be checked for the result.
	 * @param
	 * @return
	 * @throws
	 */
	public List<String> getChildren(String path) {

		List<String> list = null;
		if (!isExists(path)) {
			return list;
		}
		try {
			list = client().getChildren().forPath(path);
			LOGGER.info(OnlineTaskConstant.LOGPREFIX + "getChildren，读取数据成功, path:" + path);
		} catch (Exception e) {
			LOGGER.error(
					OnlineTaskConstant.LOGPREFIX + "getChildren，读取数据失败! path: " + path + ", errMsg:" + e.getMessage(),
					e);
		}
		return list;
	}

	/**
	 *
	 * for given path
	 * {@link } can be checked for the result.
	 * @param
	 * @return
	 * @throws
	 */
	public boolean isExists(String path) {

		if (StringHelper.isEmpty(path)) {
			return false;
		}
		try {
			Stat stat = client().checkExists().forPath(path);
			return null != stat;
		} catch (Exception e) {
			LOGGER.error(OnlineTaskConstant.LOGPREFIX + "isExists 读取数据失败! path: " + path + ", errMsg:" + e.getMessage(),
					e);
		}
		return false;
	}

	/**
	 *
	 * for given path (node) isPersistent or (EPHEMERAL)
	 * {@link } can be checked for the result.
	 * @param
	 * @return
	 * @throws
	 */
	public boolean isPersistent(String path) {

		if (StringHelper.isEmpty(path)) {
			return false;
		}
		try {
			Stat stat = client().checkExists().forPath(path);
			if (stat == null) {
				return false;
			}
			// If it is not an ephemeral node, it will be zero.
			return stat.getEphemeralOwner() == 0L;
		} catch (Exception e) {
			LOGGER.error(
					OnlineTaskConstant.LOGPREFIX + "isPersistent 读取数据失败! path: " + path + ", errMsg:" + e.getMessage(),
					e);
		}
		return false;
	}

	/**
	 *
	 * only delete leaf node for given path
	 * {@link } can be checked for the result.
	 * @param
	 * @return
	 * @throws
	 */
	public boolean deleteLeafZKNode(String path) {

		if (!isExists(path)) {
			return false;
		}
		try {
			client().delete().forPath(path);
			LOGGER.info(OnlineTaskConstant.LOGPREFIX + "deleteLeafZKNode，删除节点成功，节点地址:" + path);
			return true;
		} catch (Exception e) {
			LOGGER.error(OnlineTaskConstant.LOGPREFIX + "deleteLeafZKNode，删除节点失败:" + e.getMessage() + "，path:" + path,
					e);
		}
		return false;
	}

	/**
	 *
	 * deletingChildrenIfNeeded for given path
	 * {@link } can be checked for the result.
	 * @param
	 * @return
	 * @throws
	 */
	public boolean deletePathZKNode(String path) {

		if (!isExists(path)) {
			return false;
		}
		try {
			client().delete().deletingChildrenIfNeeded().forPath(path);
			LOGGER.info(OnlineTaskConstant.LOGPREFIX + "deletePathZKNode，删除节点成功，节点地址:" + path);
			return true;
		} catch (Exception e) {
			LOGGER.error(OnlineTaskConstant.LOGPREFIX + "deletePathZKNode，删除节点失败:" + e.getMessage() + "，path:" + path,
					e);
		}
		return false;
	}

	public void close() throws Exception {

		client().close();
	}

	/**
	 *
	 * close connection with zk to make EPHEMERAL node quickly invalid when shutdown app normally
	 * {@link } can be checked for the result.
	 * @param
	 * @return
	 * @throws
	 */
	private void shutdownHook() {
		LOGGER.info(OnlineTaskConstant.LOGPREFIX + "addShutdownHook for CuratorClient");
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					LOGGER.info(OnlineTaskConstant.LOGPREFIX + "shutdownHook begin");
					close();
					LOGGER.info(OnlineTaskConstant.LOGPREFIX + "shutdownHook end");
				} catch (Exception e) {
					LOGGER.error(OnlineTaskConstant.LOGPREFIX, e);
				}

			}
		}));
	}
}
