### SIA-TASK微服务任务调度平台源码启动-Windows

#### 1、[环境要求](install.md)

#### 2、[源码下载](https://github.com/siaorg/sia-task.git)

有两种方式将源码导入IDE环境：

* 按照给出的源码下载地址下载源码，通过IDE从本地导入；

* 使用IDE通过源码地址从版本控制仓库进行导入。

#### 3、配置文件说明

SIA-TASK微服务任务调度平台需要启动的后端进程有两个：`sia-task-config`和`sia-task-scheduler`。下面以open环境配置文件为例，详细介绍这两个进程的配置文件的配置。

##### (1) sia-task-config

sia-task-config工程dev环境下的配置文件为application-open.yml，修改方式见[配置文件修改](install-config-file.md)。

##### (2) sia-task-scheduler

sia-task-scheduler工程dev环境下的配置文件为application-open.yml，修改方式见[配置文件修改](install-config-file.md)。

#### 4、启动项目

启动项目之前，检查确认`sia-task-scheduler`和`sia-task-config`两个工程的配置是否正确，检查的内容如下图红框中所示：

![](../images/install-pom.png)

确保在`sia-task-scheduler`和`sia-task-config`两个工程pom文件中的红框内容不被注释掉。

检查完毕后，分别启动`sia-task-scheduler`和`sia-task-config`两个springboot工程，启动方式如下：

* 启动`sia-task-scheduler`工程

    (1) 在源码中找到SchedulerApplication启动类，如下图所示：

    ![](../images/install-start-scheduler.png)

    (2) 选中SchedulerApplication启动类，右键点击，在弹出框中选择`Run 'SchedulerApplication'` 或 `Debug 'SchedulerApplication'`(以调试模式启动)
    
    (3) 启动后若输出内容`>>>>>>>>>>SchedulerApplication start OK!`，则表示启动成功,如下图所示。
    
    ![](../images/install-start-scheduler-3.png)

* 启动`sia-task-config`工程

    (1) 在源码中找到TaskConfigApplication启动类，如下图所示：
    
    ![](../images/install-start-config.png)
    
    (2) 选中TaskConfigApplication启动类，右键点击，在弹出框中选择`Run 'TaskConfigApplication'` 或 `Debug 'TaskConfigApplication'`(以调试模式启动)
    
    (3) 启动后若输出内容`>>>>>>>>>>TaskConfig Application start ok!`，则表示启动成功,如下图所示。
    
    ![](../images/install-start-config-3.png)

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

访问sia-task微服务任务调度平台的访问入口(登录页面地址：http://localhost:10615 )。登录页面如下图所示：

![](../images/install-gantry-login.jpg)

输入用户名/密码即可登录（此处没做用户名/密码登录限制，任意字符串的用户名/密码都能登录。登录时选择"是否是管理员"选项后，则会以管理员身份登录）。微服务任务调度菜单项如下图所示：

![](../images/install-gantry.jpg)

在该页面中，即可对SIA-TASK微服务任务调度的功能进行操作。