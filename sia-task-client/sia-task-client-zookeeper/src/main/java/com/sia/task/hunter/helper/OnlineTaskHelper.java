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

package com.sia.task.hunter.helper;

import com.sia.task.core.util.StringHelper;
import com.sia.task.integration.curator.properties.ZookeeperConstant;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.LinkedList;
import java.util.List;

/**
 * Used to detect whether the Method with the annotation <code>{@link com.sia.task.hunter.annotation.OnlineTask}</code>
 * conforms to the specification
 *
 * @author: pengfeili23
 * @Description: OnlineTaskHelper
 * @date: 2018年7月11日 下午4:11:19
 * @see <code>{@link com.sia.task.hunter.annotation.OnlineTask}</code>
 * @see <code>{@link com.sia.task.hunter.register.TaskRegisterListener}</code>
 */
public class OnlineTaskHelper {

    private OnlineTaskHelper() {

    }

    /**
     * 必须对外暴露POST方法
     *
     * @param requestMethod
     * @return
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

    /**
     * 默认返回值是String（JSON）
     *
     * @param output
     * @return
     */
    public static boolean checkReturnType(Class<?> output) {

        return String.class.equals(output);
    }

    /**
     * 无参或默认输入参数只有一个，且是String（JSON）
     *
     * @param input
     * @return 参数的个数，0表示无参，1表示一个合法参数，-1表示参数不合法
     */
    public static int checkParameterTypes(Class<?>[] input) {

        if (input == null) {
            return -1;
        }

        if (input.length == 0) {
            return 0;
        }
        if (input.length == 1) {
            return String.class.equals(input[0]) ? 1 : -1;
        }
        return -1;
    }

    /**
     * HTTP的访问路径必须以"/"为前缀，且路径中不含"\"(用作替换)，否则认为不合法
     *
     * @param path
     * @return
     */
    public static boolean checkHttpPath(String path) {

        if (StringHelper.isEmpty(path) || path.contains(ZookeeperConstant.HTTP_MASK)) {
            return false;
        }
        return path.startsWith(ZookeeperConstant.HTTP_SEPARATOR);
    }

    /**
     * 因为ZK的路径分隔符为"/"，与HTTP的分隔符"/"冲突，故对HTTP的PATH做转化，用其他字符("\")替代"/"
     *
     * @param src
     * @return
     */
    public static String encodeHttpPath(String src) {

        if (StringHelper.isEmpty(src)) {
            return src;
        }
        return src.replace(ZookeeperConstant.HTTP_SEPARATOR, ZookeeperConstant.HTTP_MASK);
    }

    /**
     * toList
     *
     * @param obj
     * @return
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
