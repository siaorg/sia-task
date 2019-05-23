微服务任务调度平台开发指南
===
  # 2.1 自动抓取任务开发规则
  
  使用分布式任务调度，不管是SpringBoot项目还是Spring项目，请务必做到：
  
  ## 1. 任务（`Task`）抓取客户端配置
  通过POM文件引入 `sia-task-hunter`，具体如下：
  
  ```xml
  <dependency>
    <groupId>com.sia</groupId>
    <artifactId>sia-task-hunter</artifactId>
    <version>1.0.0</version>
  </dependency>
  ```
  
  ## 2. 配置文件配置
  
  对配置文件中以下属性进行配置：
  
  ```yml
  # 项目名称（必须）
  # 命名规则：项目组名-项目名-其他
  # 命名示例:skytrain-supervise，项目组名为skytrain
  spring.application.name=spring-3.x-test
  # 应用端口号（必须）
  server.port=8080
  # zookeeper地址（必须）
  # zookeeper地址IP配置形式举例：*.*.*.*:2181,*.*.*.*:2181,*.*.*.*:2181
  # zookeeper地址域名配置形式举例：域名1:2181,域名2:2181,域名3:2181
  zooKeeperHosts=127.0.0.1:2181
  # 应用上下文（可选）
  server.context-path=/spring-3.x-test
  ```
  
  关于应用上下文`server.context-path`的配置：
  
  （1）`SpringBoot`项目中应用上下文配置在 `application.yml` 中，添加属性：
  
  `server.context-path: /CONTEXT/PATH`
  
  （2）`Spring`项目中应用上下文配置是在`TOMCAT`的`server.xml`中添加：
  
  `Context path="/CONTEXT/PATH"`
  
  为了和`SpringBoot`项目保持一致，`Spring`项目需要额外在配置文件（`Spring`项目能加载到上下文中）添加：
  
  ```
  server.context-path=/CONTEXT/PATH（Spring应用）
  ```
  
  总之，不管上下文路径怎么配置，只要保证HTTP的访问路径：
  
  ```
  IP:${server.port}/${server.context-path}/${类的访问路径}/${方法的访问路径}
  ```
  
  能被正常`POST`访问即可！
  
  ## 3. 扫描路径配置
  
   
  对`sia-task-hunter`的扫描路径进行配置：
  
  ### 3.1 在`SpringBoot`项目中，请确保扫描路径中包含`"com.sia"`
  
  示例：`@SpringBootApplication(scanBasePackages = { "com.sia", "你的项目所在包名称" })`
  
  ### 3.2 在`Spring`项目中，请确保扫描路径中包含`"com.sia"`
  
  示例：`<context:component-scan base-package="com.sia,你的项目所在包名称" ></context:component-scan>`
  
  ## 4. 单例单线程配置
  
  通过`@OnlineTask`注解保证`HTTP`访问方法是单例单线程
  
  ### 4.1 `SpringBoot`中的配置
  
  ```yml
  # 是否开启 AOP 切面功能（默认为true）
  spring.aop.auto: true
  
  # 是否开启 @OnlineTask 串行控制（如果使用则必须开启AOP功能）（默认为true）（可选）
  spring.onlinetask.serial: true
  
  # 方法级别的 @OnlineTask 串行控制（如果使用则必须开启之前的AOP功能与串行控制）（默认为true）（可选）
  @OnlineTask(enableSerial=true)
  ```
  
  ### 4.2 `Spring`中的配置
  
  如需开启`AOP`切面功能，请添加配置类：
  
  ```java
  @Configuration
  @EnableAspectJAutoProxy
  public class EnableAspectJAutoProxyConfig {
        //nothing
  }
  ```
  
  只有开启`AOP`功能，`@OnlineTask`串行控制才会生效：
  
  ```yml
  # 是否开启@OnlineTask串行控制（如果使用则必须开启AOP功能）（默认为true）（可选）
  spring.onlinetask.serial=true
  
  # 方法级别的 @OnlineTask 串行控制（如果使用则必须开启之前的AOP功能与串行控制）（默认为true）（可选）
  @OnlineTask(enableSerial=true)
  ```
  
  ## 5. 在线任务标准示例
  
  ```java
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
  
  
  # 2.2 自动抓取任务开发代码示例
  
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
  