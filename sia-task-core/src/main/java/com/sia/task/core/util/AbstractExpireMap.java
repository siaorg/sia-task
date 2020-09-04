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

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 实现延时过期MAP集合 支持自定义过期触发事件
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/6/10 4:02 下午
 * @see
 **/
@Slf4j
public abstract class AbstractExpireMap<K, V> {

    private long expTime;

    private TimeUnit unit;

    ConcurrentHashMap<K, V> expireMap;
    /**
     * 控制过期时间
     */
    ConcurrentHashMap<K, Long> delayMap;

    /**
     * 将map提供给外部程序操作
     *
     * @return
     */
    public Map<K, V> getDataMap() {
        return Collections.unmodifiableMap(this.expireMap);
    }

    public AbstractExpireMap(long expTime, TimeUnit unit) {
        expireMap = new ConcurrentHashMap<K, V>();
        delayMap = new ConcurrentHashMap<K, Long>();
        this.expTime = expTime;
        this.unit = unit;
        // 启动监听线程
        ExpireCheckTask task = new ExpireCheckTask(expireMap, delayMap) {
            @Override
            protected void expireEvent(K key, V val) {
                timerExpireCallback(key, val);
            }
        };
        task.setDaemon(false);
        task.start();
    }

    /**
     * 过期回调函数 子类实现
     * baseExpireEvent
     *
     * @param key k
     * @param val v
     */
    protected abstract void timerExpireCallback(K key, V val);

    public V put(K key, V val) {
        delayMap.put(key, getExpireTime());
        return expireMap.put(key, val);
    }

    public boolean containsKey(String jobKey) {
        return expireMap.containsKey(jobKey);
    }

    public V remove(K key) {
        return expireMap.remove(key);
    }

    public V get(K key) {
        return expireMap.get(key);
    }

    private Long getExpireTime() {
        return unit.toMillis(expTime) + System.currentTimeMillis();
    }

    /**
     * 定时器扫描线程 定期移除过期元素并触发过期事件
     */
    private abstract class ExpireCheckTask extends Thread {
        ConcurrentHashMap<K, Long> delayMap;
        ConcurrentHashMap<K, V> expireMap;

        public ExpireCheckTask(ConcurrentHashMap<K, V> expireMap, ConcurrentHashMap<K, Long> delayMap) {
            this.delayMap = delayMap;
            this.expireMap = expireMap;
        }

        protected abstract void expireEvent(K key, V val);

        /**
         * 定期扫描容器，元素已超时则清理，并触发清理回调函数
         */
        @Override
        public void run() {
            Iterator<K> it;
            K key;
            while (true) {
                if (delayMap != null && !delayMap.isEmpty()) {
                    it = delayMap.keySet().iterator();
                    while (it.hasNext()) {
                        key = it.next();
                        if (delayMap.get(key) <= System.currentTimeMillis()) {
                            // 触发回调
                            expireEvent(key, expireMap.get(key));
                            it.remove();
                            expireMap.remove(key);
                            delayMap.remove(key);
                        }
                    }
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }
}
