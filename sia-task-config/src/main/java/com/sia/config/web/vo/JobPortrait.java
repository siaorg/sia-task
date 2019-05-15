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

package com.sia.config.web.vo;

import com.sia.core.entity.BasicJob;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Monitor the profile class
 * @see
 * @author maozhengwei
 * @date 2019-04-28 15:40
 * @version V1.0.0
 **/
public @Data class JobPortrait {

    String group;

    String app;

    Map<String, Integer> total;

    List<BasicJob> portrait;
}
