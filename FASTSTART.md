微服务任务调度平台使用指南
===

一.根据部署文档搭建并启动任务调度平台，详见部署文档

二.结合使用文档和开发文档，写一个TASK，并启动，

三.新增一个JOB，并引用该TASK，配置相应参数

四.激活，到执行时间后，观察日志，打完收工

# 一、根据部署文档搭建任务调度平台

# 二、根据开发文档编写TASK示例


根据开发文档，编写TASK示例，具体开发规则见开发文档，TASK示例如下：

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
  public class OnlineTaskExample {
  
      /**
       * OnlineTask示例，标准格式
       * <p>
       * （1）方法上有@OnlineTask注解，用来标注是否被抓取，可以添加description描述，描述该Task的作用
       * <p>
       * （2）方法上有@RequestMapping注解，因为OnlineTask必须对外提供HTTP访问
       * <p>
       * （3）@RequestMapping注解中，请使用value（或path）属性（因为低版本Spring没有path属性，为了兼容，优先抓取value属性的值），且value 以"/"为前缀（减少处理复杂度），路径不能包含"\"（用作替换）
       * <p>
       * （4）@RequestMapping注解中，method中必须要有POST方法（需要传参），且使用@CrossOrigin支持跨域（POST方法默认不允许跨域）或者使用过滤器（Filter）让Task可以跨域
       * <p>
       * （5）请使用 @ResponseBody 标注返回值。类上如果使用 @RestController，则 @ResponseBody可选，如果使用@Controller，则@ResponseBody必选
       * <p>
       * （6）方法返回值是String（JSON），JSON是一个Map，必须有"status" 属性，值为{success,failure,unknown}，用于处理逻辑；必须有 "result" 属性，值为HTTP调用的返回值
       * <p>
       * （7）方法可以无参；若有入参，则只能有一个，且是String（JSON），请使用 @RequestBody 标注
       * <p>
       * （8）@OnlineTask注解使用了AOP技术，保证调用的方法是单例单线程
       * <p>
       * （9）OnlineTask的业务逻辑处理请尽量保证幂等
       * <p>
       * （10）现支持类上使用@RequestMapping注解
       * /
       *
       * @param json
       * @return
       */
      // （1）方法上有@OnlineTask注解，用来标注是否被抓取，可以添加description描述，描述该Task的作用
      @OnlineTask(description = "在线任务示例",enableSerial=true)
      // （2）方法上有@RequestMapping注解，因为OnlineTask必须对外提供HTTP访问
      // （3）@RequestMapping注解中，请使用value（或path）属性（因为低版本Spring没有path属性，为了兼容，优先抓取value属性的值），且value 以"/"为前缀（减少处理复杂度），路径不能包含"\"（用作替换）
      @RequestMapping(value = "/example", method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
      // （4）@RequestMapping注解中，method中必须要有POST方法（需要传参），且使用@CrossOrigin支持跨域（POST方法默认不允许跨域）或者使用过滤器（Filter）让Task可以跨域
      @CrossOrigin(methods = { RequestMethod.POST }, origins = "*")
      // （5）请使用 @ResponseBody 标注返回值。类上如果使用 @RestController，则 @ResponseBody可选，如果使用@Controller，则@ResponseBody必选
      @ResponseBody
      // （6）方法返回值是String（JSON），JSON是一个Map，必须有"status" 属性，值为{success,failure,unknown}，用于处理逻辑；必须有 "result"属性，值为HTTP调用的返回值
      // （7）方法可以无参；若有入参，则只能有一个，且是String（JSON），请使用 @RequestBody 标注
      public String example(@RequestBody String json) {
  
          /**
           * TODO：客户端业务逻辑处理
           */
          // 返回结果存储结构，请使用Map
          Map<String, String> info = new HashMap<String, String>();
          // 返回的信息必须包含以下两个字段
          info.put("status", "success");// status字段表明此次Task调用是否成功，非 success 都是失败
          info.put("result", "as you need");// result字段表示此次Task调用的返回结果（之后可能传递给其他Task） ，其值可能作为其他Task的输入，所以只能是String（JSON）类型
          // 返回值也是String（JSON）类型，客户端包里有JSONHelper，可直接使用
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
# 根据使用文档创建并配置JOB

## 创建JOB，配置参数

## 配置TASK，配置TASK参数

##激活该JOB

##观察JOB日志

##打完收工







