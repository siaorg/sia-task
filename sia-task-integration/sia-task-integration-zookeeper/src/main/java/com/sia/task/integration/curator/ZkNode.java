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

package com.sia.task.integration.curator;

import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.Map;

public class ZkNode {
    private String name;
    private String path;
    private String content;
    private List<ZkNode> children;
    private boolean toggled=false;
    private boolean existed=false;
    private Stat stat;
    private Map<String, Object> acl;

    public ZkNode(){}
    public ZkNode(String name){
        this.name=name;
    }

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    public List<ZkNode> getChildren() {
        return children;
    }

    public void setChildren(List<ZkNode> children) {
        this.children = children;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isExisted() {
        return existed;
    }

    public void setExisted(boolean existed) {
        this.existed = existed;
    }

    public boolean hasChild(String childName){
        boolean has = false;
        if(null != children){
            for (ZkNode child:children) {
                if(child.getName().equals(childName)){
                    has = true;
                }
            }
        }
        return has;
    }

    public ZkNode getChildByName(String name){
        if(null != children){
            for (ZkNode child:children) {
                if(child.getName().equals(name)){
                   return child;
                }
            }
        }
        return null;
    }

    public Stat getStat() {
        return stat;
    }

    public void setStat(Stat stat) {
        this.stat = stat;
    }

    public Map<String, Object> getAcl() {
        return acl;
    }

    public void setAcl(Map<String, Object> acl) {
        this.acl = acl;
    }
}
