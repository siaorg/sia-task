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

package com.sia.task.integration.curator.hanler;

import org.apache.curator.framework.recipes.cache.NodeCache;

/**
 * @author: pengfeili23
 * @Description: Curator的NodeCache监听器处理接口
 * @date: 2018年6月27日 下午6:35:44
 */
public interface NodeCacheHandler {

    /**
     * 用于处理 NodeCache
     * @param nodeCache
     * @throws Exception
     */
    void process(NodeCache nodeCache) throws Exception;
    
}
