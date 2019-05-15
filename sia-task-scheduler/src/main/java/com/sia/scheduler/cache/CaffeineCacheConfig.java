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

package com.sia.scheduler.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine Cache configuration class. Used to specify the number of cached data and the lifetime.
 * Instantiate the management component of Caffeine Cache.
 *
 * @see
 * @author maozhengwei
 * @date 2018-11-2 15:36
 * @version V1.0.0
 **/
@Configuration
@EnableCaching
public class CaffeineCacheConfig {

    private static final int CAFFEINE_CACHE_MAXSIZE = 5000;

    private static final int CAFFEINE_CACHE_TTL = 24 * 60 * 60;


    /**
     *  Define the Cache name and timeout (s), the maximum number of caches
     */
    public enum Caches {
        /**
         * Job cache, task cache
         */
        basicJob(CAFFEINE_CACHE_TTL),
        taskList(CAFFEINE_CACHE_TTL);

        private int maxSize = CAFFEINE_CACHE_MAXSIZE;
        private int ttl = CAFFEINE_CACHE_TTL;

        Caches() {
        }

        Caches(int ttl) {
            this.ttl = ttl;
        }

        Caches(int ttl, int maxSize) {
            this.ttl = ttl;
            this.maxSize = maxSize;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public int getTtl() {
            return ttl;
        }

    }

    /**
     * Create Caffeine-based Cache Manager
     *
     * @return
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        ArrayList<CaffeineCache> caches = new ArrayList<>();
        for (Caches c : Caches.values()) {
            caches.add(new CaffeineCache(c.name(),
                    Caffeine.newBuilder().recordStats()
                            .expireAfterWrite(c.getTtl(), TimeUnit.SECONDS)
                            .maximumSize(c.getMaxSize())
                            .build())
            );
        }
        cacheManager.setCaches(caches);
        return cacheManager;
    }
}
