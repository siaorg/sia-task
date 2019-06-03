微服务任务调度平台SIA-TASK入手实践
===

引言

最近微服务任务调度平台SIA-TASK开源，SIA-TASK属于分布式的任务调度平台，使用起来简单方便，非常容易入手，部署搭建好SIA-TASK任务调度平台之后，编写TASK进行调度，进而实现整个调度流程。本文新建了JOB示例，该JOB关联了前后级联的两个TASK，TASKONE(前置TASK)和TASKTWO(后置TASK)，主要阐述一个JOB怎样关联配置两个级联TASK，以及该JOB是如何通过SIA-TASK实现任务调度，最终实现对两个TASK执行器的调用。


## 首先，根据部署文档来搭建任务调度平台：

源码地址：https://github.com/siaorg/sia-task

官方文档：https://github.com/siaorg/sia-task/blob/master/README.md

任务调度平台主要由任务编排中心、任务调度中心及ZK和DB等第三方服务构成，搭建SIA-TASK任务调度平台需要的主要工作包括：

1.MySQL的搭建及根据建表语句建表

2.zookeeper安装

3.SIA-TASK前端项目打包及部署

4.任务编排中心(sia-task-config)部署

5.任务调度中心(sia-task-scheduler)部署

从github上clone代码仓库并下载源码后，可根据[SIA-TASK部署指南](https://github.com/siaorg/sia-task/blob/master/DEPLOY.md)，搭建SIA-TASK任务调度平台并启动，详见[SIA-TASK部署指南](https://github.com/siaorg/sia-task/blob/master/DEPLOY.md)


## 其次，根据开发文档来编写TASK示例并启动：

根据[SIA-TASK开发指南](https://github.com/siaorg/sia-task/blob/master/DEVELOPGUIDE.md)，编写了两个TASK示例，TASKONE(前置TASK)和TASKTWO(后置TASK)，具体开发规则见[SIA-TASK开发指南](https://github.com/siaorg/sia-task/blob/master/DEVELOPGUIDE.md)，TASK示例关键配置即代码如下：

该示例为springboot项目，并且需要通过POM文件引入SIA-TASK的执行器关键依赖包sia-task-hunter来实现task执行器的自动抓取，示例主要包括以下几部分：

### 配置`POM`文件关键依赖：
```xml 
          <!-- 此处添加个性化依赖(sia-task-hunter) -->
          <dependency>
            <groupId>com.sia</groupId>
            <artifactId>sia-task-hunter</artifactId>
            <version>1.0.0</version>
          </dependency>
```
### 配置文件主要配置项：
```yml
  # 项目名称（必须）
  spring.application.name: onlinetask-demo
  
  # 应用端口号（必须）
  server.port: 10086
  
  # zookeeper地址（必须）
  zooKeeperHosts: *.*.*.*:2181,*.*.*.*:2181,*.*.*.*:2181
  
  # 是否开启 AOP 切面功能（默认为true）
  spring.aop.auto: true
  
  # 是否开启 @OnlineTask 串行控制（如果使用则必须开启AOP功能）（默认为true）（可选）
  spring.onlinetask.serial: true
```
### 编写TASK执行器主要代码
```java
  @RestController
  public class OpenTestController {

    @OnlineTask(description = "success,有入参",enableSerial=true)
    @RequestMapping(value = "/success-param", method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    @CrossOrigin(methods = { RequestMethod.POST }, origins = "*")
    @ResponseBody
    public String taskOne(@RequestBody String json) {
        Map<String, String> info = new HashMap<String, String>();
        info.put("result", "success-param"+"入参是："+json);
        info.put("status", "success");
        System.out.println("调用任务成功");

        return JSONHelper.toString(info);
    }


    @OnlineTask(description = "success,无入参",enableSerial=true)
    @RequestMapping(value = "/success-noparam", method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    @CrossOrigin(methods = { RequestMethod.POST }, origins = "*")
    @ResponseBody
    public String taskTwo() {
        Map<String, String> info = new HashMap<String, String>();
        info.put("result", "success-noparam");
        info.put("status", "success");
        System.out.println("调用任务成功");

        return JSONHelper.toString(info);
    }

}

### 当编写完TASK执行器实例后，启动该执行器所在进程

启动日志如下图：

![](docs/images/faststart_taskStart.png)

日志表明该进程正常启动，并且TASK执行器信息正常上传至ZK当中

观察TASK管理界面，如图示：

![](docs/images/faststart_taskNew.png)


从图中可知，TASK已同步至数据库中


## 再次，需要进行JOB的创建和JOB对TASK的关联和配置

根据[使用指南](https://github.com/siaorg/sia-task/blob/master/USERSGUIDE.md)进行如下操作：

TASK已自动注册至ZK，并同步至数据库中

### 创建JOB，配置参数

在JOB管理界面点击`添加Job`

![](docs/images/faststart_jobCreate.png)

点击后进入`添加Job`界面

![](docs/images/faststart_jobEdit.png)

选定Job_Group,尽量选定所要关联的TASK所属的Group组名

分别填写Job类型及其他项，Job类型也可以选择FixRate(特定时间点)类型，本例为CRON类型，具体数值为：0/30 * * * * ?，表示从当前时刻开始，每30秒执行一次

点击`添加`，添加JOB成功

### 配置TASK

添加JOB成功后，需要为该JOB配置相应的TASK，可配置单个或多个，本例以配置单个TASK为例

![](docs/images/faststart_jobMappingTask.png)

点击`配置TASK`后，进入`Task信息配置`界面

![](docs/images/faststart_jobMappingTaskDetail.png)

如上图所示，将需要配置的TASK拉取至右侧，点击`编辑`按钮(铅笔形状)，进入TASK`参数配置`界面

![](docs/images/faststart_jobMappingTaskEdit.png)

按图中编辑完成后，点击`添加`，成功将TASK配置至JOB中，可点击`TASK信息`按钮，查看`TASK配置信息详情`，观察该JOB的TASK配置情况

`TASK配置信息图`：

![](docs/images/faststart_jobMappingTaskMsg.png)

`TASK配置信息详情`

![](docs/images/faststart_jobMappingTaskMsg2.png)

## 最后，激活JOB并观察相应日志

TASK配置成功后，点击`状态操作`下拉按钮中`激活`按钮，激活JOB


![](docs/images/faststart_jobActive.png)

### 先观察管理界面JOB及TASK日志

成功激活JOB后，进入调度日志界面，等待至JOB执行时间后，可查看到该JOB执行日志，如下图示：

![](docs/images/faststart_jobTaskLog.png)

标号1：代表该JOB日志

标号2：代表该JOB所关联的TASK日志

标号3：endTask为系统追加的一个虚拟TASK，仅表示该JOB的一次调度过程完成

### 再观察执行器TASK实例日志

可观察执行器实例TASK日志，验证是否调用成功

![](docs/images/faststart_taskRun.png)

从日志可知，确实调用成功，并且每30秒调用一次

### 停止JOB

当需要停止JOB时，点击`状态操作`下拉按钮中`停止`按钮，停止JOB

![](docs/images/faststart_jobActive.png)

本文仅是对微服务任务调度平台SIA-TASK的初步实践使用，通过以上描述，可实现SIA-TASK对执行器实例TASK实现任务调度的功能，本文中搭建的示例非常简单，适合快速入手SIA-TASK，当然，SIA-TASK还有更加强大的任务调度功能，可以应对更加复杂的业务场景，大家可以继续深度使用体验，将SIA-TASK的功能点和业务相结合，将其应用至更加复杂的业务场景之下。