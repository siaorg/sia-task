### SIA-TASK配置文件修改说明

SIA-TASK为`sia-task-config`工程和`sia-task-scheduler`工程提供了open环境的配置,下面介绍配置文件的修改。

#### sia-task-config工程配置文件修改

```
spring.application.name: sia-task-config
server.port: 10615
spring.application.cnname: 任务编排中心

#############################################################
############## zooKeeperHosts config ########################
#############################################################
zooKeeperHosts: 127.0.0.2:2181,127.0.0.3:2181,127.0.0.4:2181

#############################################################
############## DB config ####################################
#############################################################
spring:
    datasource:
        name: test
        url: jdbc:mysql://127.0.0.1:3306/skyworld_task?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true
        username: ****
        password: ****
        # 使用druid数据源
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.jdbc.Driver
        filters: stat
        maxActive: 20
        initialSize: 1
        maxWait: 60000
        minIdle: 1
        timeBetweenEvictionRunsMillis: 60000
        minEvictableIdleTimeMillis: 300000
        validationQuery: select 'x'
        testWhileIdle: true
        testOnBorrow: false
        testOnReturn: false
        poolPreparedStatements: true
        maxOpenPreparedStatements: 20

#############################################################
############## mybatis config ###############################
#############################################################
mybatis:
  mapper-locations: classpath:mappers/*.xml
  type-aliases-package: com.sia.core.entity
#
pagehelper:
  helper-dialect: mysql
  reasonable: true
  support-methods-arguments: true
  params: countSql

logging.file: ./logs/${spring.application.name}.log


#address.kibana: 10.143.131.86:5601
```

需要修改的配置项主要有以下两个：

* zooKeeperHosts：自身环境的zookeeper服务地址ip:port

* Mysql：配置自身环境Mysql的url、username和password

#### sia-task-scheduler工程配置文件修改

```
spring.application.name: sia-task-scheduler
spring.application.cnname: 任务调度中心
#应用端口号
server.port: 11272
#配置报警邮箱，当该服务出现问题后会触发报警(建议配置成组邮箱)，如下：
eureka.instance.metadataMap.alarmEmail: **@**

#开启全信息展示，默认值为true，不展示所有信息
#endpoints.health.sensitive: false
#management.security.enabled: false

#############################################################
############## eureka config ################################
#############################################################
eureka.client.serviceUrl.defaultZone: http://*.*.*.12:19002/eureka/,http://*.*.*.134:19002/eureka/
# 注册时使用ip而不是主机名
eureka.instance.preferIpAddress: true
# ${spring.cloud.client.ipAddress} 为IP地址
eureka.instance.instance-id: ${spring.cloud.client.ipAddress}:${server.port}

#############################################################
############## zooKeeperHosts config ########################
#############################################################
zooKeeperHosts: 127.0.0.2:2181,127.0.0.3:2181,127.0.0.4:2181

#############################################################
############## DB config ####################################
#############################################################
spring:
    datasource:
        name: test
        url: jdbc:mysql://127.0.0.1:3306/skyworld_task?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true
        username: ****
        password: ****
        # 使用druid数据源
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.jdbc.Driver
        filters: stat
        maxActive: 20
        initialSize: 1
        maxWait: 60000
        minIdle: 1
        timeBetweenEvictionRunsMillis: 60000
        minEvictableIdleTimeMillis: 300000
        validationQuery: select 'x'
        testWhileIdle: true
        testOnBorrow: false
        testOnReturn: false
        poolPreparedStatements: true
        maxOpenPreparedStatements: 20


#############################################################
############## mybatis config ###############################
#############################################################
mybatis:
  mapper-locations: classpath:mappers/*.xml
  type-aliases-package: com.sia.core.entity

#############################################################
############## email config #################################
#############################################################
# 预警邮件配置
#whether send alarm email
SKYTRAIN_DEFAULT_EMAIL:
#邮件服务地址：*.*.*.69:12026,*.*.*.47:12026
EMAIL_SERVICE_REQUESTPATH: http://127.0.0.1:10101/sendMail

# 调度器执行的JOB个数阈值，超过则通知添加资源
onlinetask.job.alarm.threshold: 100
# 负载均衡的级别
onlinetask.job.fault.tolerant: 4

#############################################################
############## log config ###################################
#############################################################
logging.file: ./logs/${spring.application.name}.log

# Kafka server:port
#生产环境请配置：域名1:9092,域名2:9092,域名:9092 （支持多个域名:端口号配置）

spring.kafka.bootstrap-servers: *.*.*.12:9092

# 生产者和消费者topic前缀
spring.kafka.topicPrefix: sia-task-scheduler

spring.kafka.producer.enable: true
spring.kafka.producer.retries: 2
# 每次批量发送消息的数量
spring.kafka.producer.batch-size: 16384
spring.kafka.producer.buffer-memory: 33554432
# 指定消息key和消息体的编码方式
spring.kafka.producer.key-serializer: org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

本配置文件需要修改的配置项有以下三个：

* zooKeeperHosts：zookeeper服务地址ip:port

* Mysql：自身环境Mysql的url、username和password

* 邮箱配置：当JOB发生异常时发送预警邮件给相关人员

    SKYTRAIN_DEFAULT_EMAIL：默认收件人邮箱，可以为空
    
    EMAIL_SERVICE_REQUESTPATH：邮件服务接口。
    
    * 需提供的邮件服务接口格式如：http://127.0.0.1:10101/sendMail
        
    * 邮件服务接口需处理的字段如下所示：
    
        ```
        class MailFormat{
        
            /**  邮件主题 */
            private String subject;
        
            /** 预警邮箱，用逗号(,)分隔 */
            private String[] mailto;
        
            /** 邮件正文 */
            private String content;
        
            /** 压制关键字 */
            private String primary;
        
            /** 压制时间，单位：毫秒 */
            private Long elapse;
        }
        ```
        其中，请求方法为POST，subject、mailto、content、primary和elapse将以json格式发送到邮件服务接口，在提供的邮件服务接口中，需接受并处理这些字段。