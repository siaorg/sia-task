### 微服务任务调度平台DB初始化

```sql


-- ----------------------------
-- database sia_task
-- ----------------------------

create database IF not exists `skyworld_task`;

use skyworld_task;

-- ----------------------------
-- Table structure for skyworld_basic_job
-- job 元数据 手动录入
-- ----------------------------
create table if not exists skyworld_basic_job
(
  job_id           int auto_increment,
  job_key          varchar(255) not null comment '除ID外的 唯一标识 KEY',
  job_group        varchar(100) not null comment '命名空间：name+group组成一个唯一key',
  job_triger_type  varchar(25)  null comment '触发器类型',
  job_triger_value varchar(128) null comment '触发器类型值',
  job_desc         varchar(250) null comment 'job描述信息',
  job_alarm_email  varchar(100) null comment 'job预警邮箱',
  job_create_time  datetime     not null,
  job_update_time  datetime     not null,
  job_parent_key   varchar(255) null comment 'job父jobKey',
  job_plan         varchar(255) null comment 'job级联',
  constraint job_id_UNIQUE
    unique (job_id),
  constraint job_key_UNIQUE
    unique (job_key)
)
  charset = utf8;

alter table skyworld_basic_job
  add primary key (job_id);

-- ----------------------------
-- Table structure for skyworld_basic_task
-- task 元数据 自动获取/手动录入
-- ----------------------------
create table if not exists skyworld_basic_task
(
  task_id            int auto_increment,
  task_key           varchar(255)     not null comment '唯一键-检索使用(AppName+HttpPath)',
  task_group_name    varchar(255)     not null comment 'task_group_name',
  task_app_name      varchar(255)     not null comment 'app_name',
  task_app_http_path varchar(255)     not null comment 'task请求路径',
  task_app_ip_port   varchar(255)     null comment 'app实例IP:port',
  param_count        int(2) default 1 null comment '是否存在入参：0：没有，1：存在',
  taskDesc           varchar(255)     null comment 'task描述',
  task_source        varchar(45)      null comment 'task来源。TASK_SOURCE_UI：手动录入，TASK_SOURCE_ZK：自动抓取',
  create_time        datetime         not null,
  update_time        datetime         null,
  constraint task_id_UNIQUE
    unique (task_id),
  constraint task_key_UNIQUE
    unique (task_key)
)
  charset = utf8;

alter table skyworld_basic_task
  add primary key (task_id);

-- ----------------------------
-- Table structure for skyworld_job_log
-- job—log 日志表，Job 调度日志
-- ----------------------------
create table if not exists skyworld_job_log
(
  job_log_id               int auto_increment comment '主键ID AUTO_INCREMENT'
    primary key,
  job_id                   int           not null,
  job_trigger_code         varchar(45)   null comment '调度-结果状态',
  job_trigger_msg          varchar(2048) null comment '调度-日志',
  job_trigger_time         datetime      null comment '调度-时间',
  job_handle_code          varchar(45)   null comment '执行结果-状态',
  job_handle_msg           varchar(2048) null comment '执行结果-日志',
  job_handle_time          datetime      null comment '执行-时间',
  job_handle_finished_time datetime      null comment '执行完成时间',
  create_time              datetime      null
)
  charset = utf8;

-- ----------------------------
-- Table structure for skyworld_portal_stat
-- sia-task 统计表，监控使用
-- ----------------------------
create table if not exists skyworld_portal_stat
(
  portal_statistics_id int auto_increment
    primary key,
  scheduler            varchar(2048) not null comment '调度器IP:PORT',
  job_call_count       int           not null comment 'JOB调度次数',
  task_call_count      int           not null comment 'task调度次数',
  job_exception_count  int           not null comment 'JOB异常数量',
  job_finished_count   int           not null comment 'JOB已完成数量',
  task_exception_count int           not null comment 'task异常数量',
  task_finished_count  int           not null comment 'task已完成数量',
  last_time            datetime      not null comment '上次统计时间',
  create_time          datetime      not null
)
  charset = utf8;;

-- ----------------------------
-- Table structure for skyworld_task_log
-- task-log 日志表，task 调度日志
-- ----------------------------
create table if not exists skyworld_task_log
(
  task_log_id        int auto_increment
    primary key,
  job_log_id         int           not null comment 'task计数;',
  job_key            varchar(255)  null,
  task_key           varchar(255)  not null comment 'task_id',
  task_msg           varchar(2048) null comment '状态描述信息,如：异常信息，SUCCESS等',
  task_status        varchar(45)   null comment '状态值：ready,running,finished,exception',
  task_handle_time   datetime      null,
  task_finished_time datetime      null,
  create_time        datetime      null
)
  charset = utf8;

-- ----------------------------
-- Table structure for task_mapping_job
-- 编排关系 job-task 关系表
-- ----------------------------
create table if not exists task_mapping_job
(
  task_map_job_id int auto_increment
    primary key,
  job_id          int                                      not null,
  job_key         varchar(255)                             not null,
  job_group       varchar(255)                             not null,
  task_id         int                                      not null,
  task_key        varchar(255)                             not null,
  pre_task_key    varchar(255)                             null comment '前置任务',
  input_type      varchar(255) default 'from_ui'           not null comment 'task入参来源：{from_ui,from_task}',
  input_value     varchar(255)                             null comment 'task入参参数值',
  route_strategy  varchar(45)  default 'ROUTE_TYPE_RANDOM' null comment '路由策略{ROUTE_TYPE_FIRST,ROUTE_TYPE_RANDOM,ROUTE_TYPE_LAST,ROUTE_TYPE_ROUND}',
  failover        varchar(45)                              null comment '失败恢复策略',
  fix_ip          varchar(45)                              null comment '预估执行时间',
  update_time     datetime                                 null comment '更新时间',
  create_time     datetime                                 not null comment '创建时间',
  read_timeout    int                                      null comment '接口数据返回超时时间',
  constraint uni_ind_job_task_id
    unique (job_key, job_group, task_key)
)
  charset = utf8;


```