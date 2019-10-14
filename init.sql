SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for skyworld_basic_job
-- ----------------------------
DROP TABLE IF EXISTS `skyworld_basic_job`;
CREATE TABLE `skyworld_basic_job` (
  `job_id` int(11) NOT NULL AUTO_INCREMENT,
  `job_key` varchar(255) NOT NULL COMMENT '除ID外的 唯一标识 KEY',
  `job_group` varchar(100) NOT NULL COMMENT '命名空间：name+group组成一个唯一key',
  `job_triger_type` varchar(25) DEFAULT NULL COMMENT '触发器类型',
  `job_triger_value` varchar(128) DEFAULT NULL COMMENT '触发器类型值',
  `job_desc` varchar(250) DEFAULT NULL COMMENT 'job描述信息',
  `job_alarm_email` varchar(100) DEFAULT NULL COMMENT 'job预警邮箱',
  `job_create_time` datetime NOT NULL,
  `job_update_time` datetime NOT NULL,
  `job_parent_key` varchar(255) DEFAULT NULL COMMENT 'job父jobKey',
  `job_plan` varchar(255) DEFAULT NULL COMMENT 'job级联',
  PRIMARY KEY (`job_id`),
  UNIQUE KEY `job_id_UNIQUE` (`job_id`),
  UNIQUE KEY `job_key_UNIQUE` (`job_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for skyworld_basic_task
-- ----------------------------
DROP TABLE IF EXISTS `skyworld_basic_task`;
CREATE TABLE `skyworld_basic_task` (
  `task_id` int(11) NOT NULL AUTO_INCREMENT,
  `task_key` varchar(255) NOT NULL COMMENT '唯一键-检索使用(AppName+HttpPath)',
  `task_group_name` varchar(255) NOT NULL COMMENT 'task_group_name',
  `task_app_name` varchar(255) NOT NULL COMMENT 'app_name',
  `task_app_http_path` varchar(255) NOT NULL COMMENT 'task请求路径',
  `task_app_ip_port` varchar(255) DEFAULT NULL COMMENT 'app实例IP:port',
  `param_count` int(2) DEFAULT 1 COMMENT '是否存在入参：0：没有，1：存在',
  `taskDesc` varchar(255) DEFAULT NULL COMMENT 'task描述',
  `task_source` varchar(45) DEFAULT NULL COMMENT 'task来源。TASK_SOURCE_UI：手动录入，TASK_SOURCE_ZK：自动抓取',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`task_id`),
  UNIQUE KEY `task_id_UNIQUE` (`task_id`),
  UNIQUE KEY `task_key_UNIQUE` (`task_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for skyworld_job_log
-- ----------------------------
DROP TABLE IF EXISTS `skyworld_job_log`;
CREATE TABLE `skyworld_job_log` (
  `job_log_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID AUTO_INCREMENT',
  `job_id` int(11) NOT NULL,
  `job_trigger_code` varchar(45) DEFAULT NULL COMMENT '调度-结果状态',
  `job_trigger_msg` varchar(2048) DEFAULT NULL COMMENT '调度-日志',
  `job_trigger_time` datetime DEFAULT NULL COMMENT '调度-时间',
  `job_handle_code` varchar(45) DEFAULT NULL COMMENT '执行结果-状态',
  `job_handle_msg` varchar(2048) DEFAULT NULL COMMENT '执行结果-日志',
  `job_handle_time` datetime DEFAULT NULL COMMENT '执行-时间',
  `job_handle_finished_time` datetime DEFAULT NULL COMMENT '执行完成时间',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`job_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for skyworld_portal_stat
-- ----------------------------
DROP TABLE IF EXISTS `skyworld_portal_stat`;
CREATE TABLE `skyworld_portal_stat` (
  `portal_statistics_id` int(11) NOT NULL AUTO_INCREMENT,
  `scheduler` varchar(2048) NOT NULL COMMENT '调度器IP:PORT',
  `job_call_count` int(11) NOT NULL COMMENT 'JOB调度次数',
  `task_call_count` int(11) NOT NULL COMMENT 'task调度次数',
  `job_exception_count` int(11) NOT NULL COMMENT 'JOB异常数量',
  `job_finished_count` int(11) NOT NULL COMMENT 'JOB已完成数量',
  `task_exception_count` int(11) NOT NULL COMMENT 'task异常数量',
  `task_finished_count` int(11) NOT NULL COMMENT 'task已完成数量',
  `last_time` datetime NOT NULL COMMENT '上次统计时间',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`portal_statistics_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for skyworld_task_log
-- ----------------------------
DROP TABLE IF EXISTS `skyworld_task_log`;
CREATE TABLE `skyworld_task_log` (
  `task_log_id` int(11) NOT NULL AUTO_INCREMENT,
  `job_log_id` int(11) NOT NULL COMMENT 'task计数;',
  `job_key` varchar(255) DEFAULT NULL,
  `task_key` varchar(255) NOT NULL COMMENT 'task_id',
  `task_msg` varchar(2048) DEFAULT NULL COMMENT '状态描述信息,如：异常信息，SUCCESS等',
  `task_status` varchar(45) DEFAULT NULL COMMENT '状态值：ready,running,finished,exception',
  `task_handle_time` datetime DEFAULT NULL,
  `task_finished_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`task_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for task_mapping_job
-- ----------------------------
DROP TABLE IF EXISTS `task_mapping_job`;
CREATE TABLE `task_mapping_job` (
  `task_map_job_id` int(11) NOT NULL AUTO_INCREMENT,
  `job_id` int(11) NOT NULL,
  `job_key` varchar(255) NOT NULL,
  `job_group` varchar(255) NOT NULL,
  `task_id` int(11) NOT NULL,
  `task_key` varchar(255) NOT NULL,
  `pre_task_key` varchar(255) DEFAULT NULL COMMENT '前置任务',
  `input_type` varchar(255) NOT NULL DEFAULT 'from_ui' COMMENT 'task入参来源：{from_ui,from_task}',
  `input_value` varchar(255) DEFAULT NULL COMMENT 'task入参参数值',
  `route_strategy` varchar(45) DEFAULT 'ROUTE_TYPE_RANDOM' COMMENT '路由策略{ROUTE_TYPE_FIRST,ROUTE_TYPE_RANDOM,ROUTE_TYPE_LAST,ROUTE_TYPE_ROUND}',
  `failover` varchar(45) DEFAULT NULL COMMENT '失败恢复策略',
  `fix_ip` varchar(45) DEFAULT NULL COMMENT '预估执行时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `read_timeout` int(11) DEFAULT NULL COMMENT '接口数据返回超时时间',
  PRIMARY KEY (`task_map_job_id`),
  UNIQUE KEY `uni_ind_job_task_id` (`job_key`,`job_group`,`task_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;
