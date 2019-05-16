### SIA-TASK微服务任务调度平台Linux下JAR包部署启动

下面介绍SIA-TASK微服务任务调度平台在Linux下以JAR包方式进行部署启动的步骤，这里以部署open环境为例。

#### 1、[环境要求](install.md)

#### 2、安装包获取

有两种方式可以获取项目安装包：

(1) 在github源码中获取已经打包好的项目安装包，获取地址: https://github.com/siaorg/sia-task 

(2) 从源码打包获取安装包。基本步骤如下：

* 项目源码导入IDE：

    * 按照给出的[源码下载地址](https://github.com/siaorg/sia-task.git)下载源码，通过IDE从本地导入；

    * 使用IDE通过源码地址从版本控制仓库进行导入。

* pom.xml修改：注释掉`sia-task-config`和`sia-task-scheduler`项目pom.xml中关于配置文件打包的配置，见下图：

    ![](images/install-pom.png)

* 使用maven工具打包：

    * 在IDE(以IntelliJ IDEA为例)中打开Maven Projects面板，如下图所示：

    ![](images/install-maven.png)
    
    * 在Maven Projects面板的Profiles下选中jdk18；
    
    * 打开Maven Projects面板的`sia-task-build-component`工程，依次点击`clean`和`install`，查看控制台输出：
    
        * 若两次点击最后都输出`Process finished with exit code 0`，说明两个maven命令都执行成功；
        
        * 若两次点击中有一个的点击不是输出`Process finished with exit code 0`，则对应maven命令执行失败，需查看控制台报错信息进行排查。
        
        * maven命令执行成功之后，在源码Project面板的`sia-task-build-component`工程中会出现名为target的目录，如下图所示：
        
        ![](images/install-project-target.jpg)
        
    * 上图中的.zip包即为项目安装包。打开安装包所在文件夹，将安装包解压，得到task目录，其中包括四个子目录：
    
        * bin：存放`sia-task-config`和`sia-task-scheduler`两个工程的jar包及各类shell脚本，如下图所示：
        
        ![](images/install-build-task.jpg)
        
        * config：存放`sia-task-config`和`sia-task-scheduler`两个工程的配置文件，如下图所示：
        
        ![](images/install-build-config.jpg)
        
        * logs：存放日志
        
        * thirdparty：
    
#### 3、配置文件修改

得到项目安装包之后，需要根据自身环境修改安装包task/config下的配置文件。

##### (1) sia-task-config

sia-task-config工程open环境下的配置文件为task_config_open.yml，修改方式见[配置文件修改](install-config-file.md)。

##### (2) sia-task-scheduler

sia-task-scheduler工程test环境下的配置文件为task_scheduler_open.yml，修改方式见[配置文件修改](install-config-file.md)。

#### 4、启动脚本运行

项目需要启动的后端进程有两个：`sia-task-config`和`sia-task-scheduler`，且两个工程可以单独进行部署，下面分单点部署和集群部署分别介绍。

(1) 单点部署：

将安装包放到Linux测试环境下，进入到安装包的task/bin目录下，就可以通过脚本启动或停止项目进程，且这两个进程都在同一台测试机上部署。

以test环境为例，

若启动这两个工程，则执行命令如下：

* 启动`sia-task-config`工程，运行：

    ```
    sh start_task_config_test.sh
    ```
    
    执行之后，会在当前目录下生成task_config_test.start日志文件，查看日志文件，若日志中输出`>>>>>>>>>>TaskConfig Application start ok!`，则工程启动成功，如下图红色线段所示：
    
    ![](images/install-start-config-3.png)

* 启动`sia-task-scheduler`工程，运行：

    ```
    sh start_task_scheduler_test.sh
    ```
    
    执行之后，会在当前目录下生成task_scheduler_test.start日志文件，查看日志文件，若日志中输出`>>>>>>>>>>SchedulerApplication start OK!`，则工程启动成功，如下图红色线段所示：
    
    ![](images/install-start-scheduler-3.png)

若停止这两个工程，则执行如下命令：

* 停止`sia-task-config`工程，运行：

    ```
    sh shutdown_task_config_test.sh
    ```

* 停止`sia-task-scheduler`工程，运行：
    
    ```
    sh shutdown_task_scheduler_test.sh
    ```

(2) 集群部署：

假设`sia-task-config`工程部署两个节点(ip: 101,102)，`sia-task-scheduler`工程部署三个节点(ip: 201,202,203)，则具体部署方式如下：

* 将以上得到的安装包分别放到101、102、201、202、203五个节点上；

* 对101、102两个节点，只部署`sia-task-config`工程，即在安装包的task/bin目录下，运行`sia-task-config`工程的启动脚本，执行命令可见单点部署方式；

* 同理，对201、202、203三个节点，只部署`sia-task-scheduler`进程，即在安装包的task/bin目录下，运行`sia-task-scheduler`工程的启动脚本，执行命令可见单点部署方式；

#### 5、启动前端项目

启动前端项目有两种方式：

* 单独部署启动，详见[前端部署启动文档](install-front-end.md)；

* 置于`sia-task-config`工程中启动。

    (1) 将获取的前端包解压，得到`static`目录和`index.html`文件，修改`static`目录下的site-map.js文件的`CESHI_API_HOST`配置，如下所示：
    
    ```
    (function () {
      window.API = {
        'CESHI_API_HOST': 'localhost:10615',//修改为部署sia-task-config工程的节点的IP地址和工程启动端口号，这是sia-task微服务任务调度平台的访问入口
        'CESHI_API_HOST_LOG': 'localhost:5601'
      }
      Object.freeze(window.API)
      Object.defineProperty(window, 'API', {
        configurable: false,
        writable: false
      })
    })()
    ```
    
    (2) 在sia-task-config工程中resources目录下新建名为static的目录，将(1)中获得的`static`目录和`index.html`文件放到新建的static目录中；
    
    (3) 启动`sia-task-config`工程时，前端工程也随即启动。

这两种前端启动方式均需提前将前端包中`site-map.js`文件的`CESHI_API_HOST`配置进行修改。

#### 6、访问项目

查询sia-task-config工程相应配置文件中的gantryUrl配置项，该配置项所配url是gantry工程的入口(需提前申请获取gantry访问权限，并修改菜单管理`任务调度`的访问入口)。gantry工程提供的菜单项如下图所示：

![](images/install-gantry.png)

点击菜单栏中的`任务调度`,即可访问SIA-TASK微服务任务调度平台的首页。