微服务任务调度平台快速入手demo
===

# 一、根据部署文档搭建任务调度平台
详见[部署指南](DEPLOY.md#部署指南)

# 二、根据开发文档编写TASK示例


根据开发指南，编写TASK示例(配置了两个TASK，使用其中一个即可)，具体开发规则见[开发指南](DEVELOPGUIDE.md#开发指南)，TASK示例如下：

  # 1.2 自动抓取任务开发代码示例
  
  ## 1. `POM`文件
  
  ```xml
  <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
      <modelVersion>4.0.0</modelVersion>
  
      <!-- 项目名称配置，请自定义修改 -->
      <groupId>com.creditease</groupId>
      <artifactId>onlinetask-client</artifactId>
      <version>0.0.1-SNAPSHOT</version>
      <packaging>jar</packaging>
  
      <!-- 基本配置，开始 -->
      <properties>
          <java.version>1.8</java.version>
          <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
          <spring.boot.version>1.5.11.RELEASE</spring.boot.version>
          <spring.cloud.version>Dalston.SR5</spring.cloud.version>
      </properties>
  
      <dependencyManagement>
          <dependencies>
  
              <dependency>
                  <!-- Import dependency management from Spring Boot -->
                  <groupId>org.springframework.boot</groupId>
                  <artifactId>spring-boot-starter-parent</artifactId>
                  <version>${spring.boot.version}</version>
                  <type>pom</type>
                  <scope>import</scope>
              </dependency>
  
              <dependency>
                  <!-- Import dependency management from Spring Cloud -->
                  <groupId>org.springframework.cloud</groupId>
                  <artifactId>spring-cloud-dependencies</artifactId>
                  <version>${spring.cloud.version}</version>
                  <type>pom</type>
                  <scope>import</scope>
              </dependency>
  
          </dependencies>
      </dependencyManagement>
      <!-- 基本配置，结束 -->
  
  
      <dependencies>
          <!-- 基本依赖，开始 -->
          <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter</artifactId>
          </dependency>
  
          <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-web</artifactId>
          </dependency>
  
          <!-- 基本依赖，结束 -->
  
          <!-- 此处添加个性化依赖 -->
          <dependency>
            <groupId>com.sia</groupId>
            <artifactId>sia-task-hunter</artifactId>
            <version>1.0.0</version>
          </dependency>
  
      </dependencies>
  
  
      <!-- 打包配置 -->
      <build>
          <resources>
              <resource>
                  <directory>src/main/resources</directory>
                  <filtering>true</filtering>
              </resource>
          </resources>
          <plugins>
              <plugin>
                  <groupId>org.springframework.boot</groupId>
                  <artifactId>spring-boot-maven-plugin</artifactId>
                  <executions>
                      <execution>
                          <goals>
                              <goal>repackage</goal>
                          </goals>
                      </execution>
                  </executions>
              </plugin>
          </plugins>
      </build>
  
  </project>
  ```
  
  ## 2. 配置文件
  
  ```yml
  # 项目名称（必须）
  spring.application.name: onlinetask-client
  
  # 应用端口号（必须）
  server.port: 10086
  
  # zookeeper地址（必须）
  zooKeeperHosts: *.*.*.*:2181,*.*.*.*:2181,*.*.*.*:2181
  
  # 应用上下文（可选）
  server.context-path: /
  
  # 是否开启 AOP 切面功能（默认为true）
  spring.aop.auto: true
  
  # 是否开启 @OnlineTask 串行控制（如果使用则必须开启AOP功能）（默认为true）（可选）
  spring.onlinetask.serial: true
  ```
  
  ## 3. `controller`
  
  ```java
  package com.creditease.online.example;
  
  import java.util.HashMap;
  import java.util.Map;
  
  import org.springframework.web.bind.annotation.CrossOrigin;
  import org.springframework.web.bind.annotation.RequestBody;
  import org.springframework.web.bind.annotation.RequestMapping;
  import org.springframework.web.bind.annotation.RequestMethod;
  import org.springframework.web.bind.annotation.ResponseBody;
  import org.springframework.web.bind.annotation.RestController;
  
  import com.gantry.onlinetask.annotation.OnlineTask;
  import com.gantry.onlinetask.helper.JSONHelper;
  
  @RestController
  public class OpenTestController {

    @OnlineTask(description = "success,有入参",enableSerial=true)
    @RequestMapping(value = "/success-param", method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    @CrossOrigin(methods = { RequestMethod.POST }, origins = "*")
    @ResponseBody
    public String example(@RequestBody String json) {
        Map<String, String> info = new HashMap<String, String>();
        info.put("result", "success-param"+"入参是："+json);
        info.put("status", "success");

        return JSONHelper.toString(info);
    }


    @OnlineTask(description = "success,无入参",enableSerial=true)
    @RequestMapping(value = "/success-noparam", method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    @CrossOrigin(methods = { RequestMethod.POST }, origins = "*")
    @ResponseBody
    public String example1() {
        Map<String, String> info = new HashMap<String, String>();
        info.put("result", "success-noparam");
        info.put("status", "success");

        return JSONHelper.toString(info);
    }

}
  ```
  
  ## 4. `启动类`
  
  ```java
  package com.creditease;
  
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  import org.springframework.boot.SpringApplication;
  import org.springframework.boot.autoconfigure.SpringBootApplication;
  //务必覆盖扫描包的范围
  @SpringBootApplication(scanBasePackages = { "com.sia"})
  public class OnlineTaskClientApp {
  
      private static final Logger LOGGER = LoggerFactory.getLogger(OnlineTaskClientApp.class);
  
      public static void main(String[] args) {
  
          SpringApplication.run(OnlineTaskClientApp.class, args);
          LOGGER.info("OnlineTaskClient启动！");
  
      }
  
  }
  ```
## 启动该TASK所在进程
启动日志如下图：

![](docs/images/faststart_taskStart.png)

日志表明该进程正常启动且该TASK信息正常上传至ZK中


# 根据使用文档创建并配置JOB

## 观察TASK管理界面：

![](docs/images/faststart_taskNew.png)

TASK已自动注册至ZK，并同步至数据库中


## 创建JOB，配置参数

在JOB管理界面点击添加JOB

![](docs/images/faststart_jobCreate.png)

点击后进入编辑JOB界面

![](docs/images/faststart_jobEdit.png)

选定Job_Group,尽量选定所要关联的TASK所属的Group组名

分别填写Job类型(也可以选择FixRate类型，本例为CRON类型)及其他项

点击添加，添加JOB成功

## 配置TASK，配置TASK参数

添加JOB成功后，需要为该JOB配置相应的TASK，可配置一个或多个，本例以配置一个TASK为例

![](docs/images/faststart_jobMappingTask.png)

点击配置TASK后，进入TASK配置界面

![](docs/images/faststart_jobMappingTaskDetail.png)

如上图所示，将需要配置的TASK拉取至右侧，点击编辑按钮，进入TASK属性编辑界面

![](docs/images/faststart_jobMappingTaskEdit.png)

按图中编辑完成后，点击添加

成功将TASK配置至JOB中

可查看TASK配置详情，观察该JOB的TASK配置情况

TASK配置图：

![](docs/images/faststart_jobMappingTaskMsg.png)

TASK配置信息

![](docs/images/faststart_jobMappingTaskMsg2.png)

##激活JOB

TASK配置成功后，可进行激活JOB操作

![](docs/images/faststart_jobActive.png)

##观察JOB日志

成功激活JOB后，进入调度日志界面，都待至JOB执行时间后，可查看到该JOB执行日志，如下图示：

![](docs/images/faststart_jobTaskLog.png)

标号1：代表该JOB日志

标号2：代表该JOB所关联的TASK日志

标号3：endTask为一个虚拟TASK，仅表示该JOB的一次调度过程完成





##打完收工







