# Docker本地测试部署
Docker部署到本地，仅提供本地测试，请勿在生产环境直接使用！如需生产环境使用，请自己重新定制。

## 部署准备
在直接使用Docker部署前，需要准备Docker环境以及修改相关配置。

### Docker及Docker compose安装
部署需安装Docker，Docker compose请自行参考[官网](https://docs.docker.com/install/)。

### 前端配置
参考[部署指南](DEPLOY.md)将项目打包编译，修改dist/static/site-map.js如下： 
```js
(function () {
  window.API = {
    'CESHI_API_HOST': '127.0.0.1:10615'
  }
  Object.freeze(window.API)
  Object.defineProperty(window, 'API', {
    configurable: false,
    writable: false
  })
})()
```
### 后端配置
后端不需要任何配置，只需要到sia-task-build-component和sia-task-executor-demo下均执行
> mvn clean install

**【注意】**：   
在sia-task-executor-demo中执行之前，需要先确认sia-task-hunter的版本，即maven打包时激活的profile。    
去修改sia-task-executor-demo的pom文件，修改如下：
```xml
        <dependency>
            <groupId>com.sia</groupId>
            <artifactId>sia-task-hunter</artifactId>
            <version>1.0.0</version>
            <!-- 你激活的maven profile产生的classifier-->
            <classifier>jdk18</classifier>
        </dependency>
```
### 运行
在项目根目录下运行：
>  docker-compose -f docker-compose.yml up -d

等待运行成功后，即可通过127.0.0.1:9999访问管理页面。

### 说明
所有项目均在docker中运行，访问地址均为127.0.0.1，端口如下：

|项目|远程调试端口|访问端口|
|:---:|:---:|:---:|
|display|--|9999|
|config|9109|10615|
|scheduler|9209|19011|
|demo|9309|10089|
|db|3306|3306|
|zk1|--|2181|
|zk2|--|2182|
|zk3|--|2183|

前端项目，将前端打包的代码以及nginx配置挂载成了卷。其他项目未挂载任何卷。
