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

package com.sia.hunter.helper;

import com.sia.hunter.constant.OnlineTaskConstant;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 * @description OnlineTaskHelper
 * @see
 * @author pengfeili23
 * @date 2018-07-11 16:11:19
 * @version V1.0.0
 **/
public class OnlineTaskHelper {

    private OnlineTaskHelper() {

    }

    /**
     *
     * 必须对外暴露POST方法
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    public static boolean checkRequestMethod(RequestMethod[] requestMethod) {

        if (requestMethod == null) {
            return false;
        }
        // 如果@RequestMapping不指定method，则method为空，对所有方法开放
        if (requestMethod.length <= 0) {
            return true;
        }
        for (RequestMethod instance : requestMethod) {
            if (instance.equals(RequestMethod.POST)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkReturnType(Class<?> output) {

        return java.lang.String.class.equals(output);
    }

    /**
     *
     * no parameter or one default parameter; parameter type is String(json)
     * {@link } can be checked for the result.
     * @param
     * @return count of parameter: 0 for no param, 1 for one, -1 for invalid param
     * @throws
     */
    public static int checkParameterTypes(Class<?>[] input) {

        if (input == null) {
            return -1;
        }

        if (input.length == 0) {
            return 0;
        }
        if (input.length == 1) {
            return java.lang.String.class.equals(input[0]) ? 1 : -1;
        }
        return -1;
    }

    /**
     *
     * http path must start with "/" and must not contain "\"
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    public static boolean checkHttpPath(String path) {

        if (StringHelper.isEmpty(path) || path.contains(OnlineTaskConstant.HTTP_MASK)) {
            return false;
        }
        return path.startsWith(OnlineTaskConstant.HTTP_SEPARATOR);
    }

    /**
     *
     * ZK's path separation character "/" conflicts with http's, so "/" is replaced by "\" to convert http path
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    public static String encodeHttpPath(String src) {

        if (StringHelper.isEmpty(src)) {
            return src;
        }
        return src.replace(OnlineTaskConstant.HTTP_SEPARATOR, OnlineTaskConstant.HTTP_MASK);
    }

    /**
     *
     * toList
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    public static List<String> toList(Object[] obj) {

        List<String> res = new LinkedList<String>();

        if (obj == null) {
            return res;
        }
        for (Object instance : obj) {
            res.add(instance.toString());
        }
        return res;
    }

}
