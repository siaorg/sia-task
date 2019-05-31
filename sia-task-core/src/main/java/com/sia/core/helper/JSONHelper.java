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

package com.sia.core.helper;




import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 *
 *
 * @description
 * @see
 * @author MAOZW
 * @date 2018-04-18 11:10
 * @version V1.0.0
 **/
public class JSONHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSONHelper.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JSONHelper() {
    }

    /**
     *
     * Object to jsonString
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    public static String toString(Object obj) {

        if (null == obj) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return obj.toString();
    }

    /**
     *
     * jsonString to Object
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    public static <T> T toObject(String jsonString, Class<T> c) {

        if (null == c || StringHelper.isEmpty(jsonString)) {
            return null;
        }
        try {
            return MAPPER.readValue(jsonString, c);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return null;
    }

    /**
     *
     * jsonString to ObjectArray
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    public static <T> List<T> toObjectArray(String jsonString, Class<T> c) {

        if (null == c || StringHelper.isEmpty(jsonString)) {
            return Collections.emptyList();
        }
        try {
            JavaType javaType = MAPPER.getTypeFactory().constructParametricType(List.class, c);
            return MAPPER.readValue(jsonString, javaType);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return null;
    }
}
