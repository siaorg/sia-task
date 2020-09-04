0001 MQuartz-自定义配置说明
===

MQuartz : 改进说明 ---   

| Property Name | Required | Type | Default Value |
|---|---|---|---|
| com.sia.task.quartz.scheduler.instanceName | No | String | DefaultMQuartzScheduler |
| com.sia.task.quartz.scheduler.threadName | No | String | _MQuartzSchedulerThread_Worker-N |
| org.quartz.scheduler.batchTriggerAcquisitionMaxCount | No | Int | 1 |
| org.quartz.scheduler.batchTriggerAcquisitionFireAheadTimeWindow | No | Int | 0 |
| com.sia.task.quartz.scheduler.idleWaitTime | No | int | 30s |
| com.sia.task.quartz.scheduler.makeSchedulerThreadDaemon | No | boolean | false |
| A4 | B4 | C4 | D4 |
| A4 | B4 | C4 | D4 |


* **InstanceName**

`PROP_SCHED_INSTANCE_NAME=com.sia.task.quartz.scheduler.instanceName`   

```properties 
com.sia.task.quartz.scheduler.instanceName:DefaultMQuartzScheduler 
```
Can be any string, and the value has no meaning to the scheduler itself - but rather serves as a mechanism for client code to distinguish schedulers when multiple instances are used within the same program.


* **threadName**

`PROP_SCHED_THREAD_NAME = "com.sia.task.quartz.scheduler.threadName"`   

```properties 
com.sia.task.quartz.scheduler.threadName:_MQuartzSchedulerThread
```
Can be any String that is a valid name for a java thread. If this property is not specified, the thread will receive the scheduler's name ("com.sia.task.quartz.scheduler.instanceName") plus an the appended string '_QuartzSchedulerThread'.

* **batchTriggerAcquisitionMaxCount**   

`PROP_SCHED_MAX_BATCH_SIZE=com.sia.task.quartz.scheduler.batchTriggerAcquisitionMaxCount`  

 ```java triggers = qsRsrcs.getJobStore().acquireNextTriggers(
 now + idleWaitTime, Math.min( availThreadCount, qsRsrcs.getMaxBatchSize()), qsRsrcs.getBatchTimeWindow());  
 ```   

允许调度程序节点一次获取（用于触发）的触发器的最大数量，默认是1;

* **batchTriggerAcquisitionFireAheadTimeWindow**   

`PROP_SCHED_BATCH_TIME_WINDOW = "com.sia.task.quartz.scheduler.batchTriggerAcquisitionFireAheadTimeWindow"`  

 ```java triggers = qsRsrcs.getJobStore().acquireNextTriggers(
 now + idleWaitTime, Math.min( availThreadCount, qsRsrcs.getMaxBatchSize()), qsRsrcs.getBatchTimeWindow());  
 ```   
时间窗口调节参数 允许触发器在其预定的火灾时间之前被获取和触发的时间（毫秒）的时间量，默认是0;

* **idleWaitTime**   

`PROP_SCHED_IDLE_WAIT_TIME = "com.sia.task.quartz.scheduler.idleWaitTime"`  

 ```java triggers = qsRsrcs.getJobStore().acquireNextTriggers(
 now + idleWaitTime, Math.min( availThreadCount, qsRsrcs.getMaxBatchSize()), qsRsrcs.getBatchTimeWindow());  
 ```   
在调度程序处于空闲状态时，调度程序查询的触发器可用之前等待的时间量（以毫秒为单位），默认是30秒;30秒内没有需要执行的任务，则等待一个随机时间。getRandomizedIdleWaitTime产生一个30秒内随机等待时间。

* **makeSchedulerThreadDaemon**   

`PROP_SCHED_IDLE_WAIT_TIME = "com.sia.task.quartz.scheduler.idleWaitTime"`  

 ```java triggers = qsRsrcs.getJobStore().acquireNextTriggers(
 now + idleWaitTime, Math.min( availThreadCount, qsRsrcs.getMaxBatchSize()), qsRsrcs.getBatchTimeWindow());  
 ```   
A boolean value ('true' or 'false') that specifies whether the main thread of the scheduler should be a daemon thread or not.
