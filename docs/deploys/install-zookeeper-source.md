###  搭建zookeeper环境（准备工作）
生产建议至少使用三个节点作为zk集群

假设集群节点IP：100 - 101 - 102

分别在三个节点上面执行如下命令：

#### 命令1：切换app用户
```shell
命令1：切换app用户
[root@localhost ~]# su app(看生产实际情况，需否)
[app@localhost yxgly]$
```
#### 命令2：创建路径
```shell
命令2：创建路径
[root@localhost ~]# cd /app/
[root@localhost app]# mkdir zookeeper
[root@localhost app]# cd zookeeper/
```
#### 命令3：上传zookeeper 安装包
```shell
命令3：上传zookeeper 安装包
[root@localhost zookeeper]# rz 
zookeeper-3.4.6.tar.gz
```
#### 命令4：解压缩
```shell
命令4：解压缩
[root@localhost zookeeper]# tar -zxvf zookeeper-3.4.6.tar.gz 
[root@localhost zookeeper]# ll
total 17292
drwxr-xr-x 10 app  app      4096 Feb 20  2014 zookeeper-3.4.6
-rw-r--r--  1 root root 17699306 Aug 27  2015 zookeeper-3.4.6.tar.gz
```
#### 命令5：创建data log路径
```shell
[root@localhost zookeeper]# mkdir -p zookeeperdata/data
[root@localhost zookeeper]# mkdir -p zookeeperdata/log
```

#### 命令6：修改配置文件
```shell
[root@localhost zookeeper]# cd zookeeper-3.4.6/conf/
[root@localhost conf]# ll
total 12
-rw-rw-r-- 1 app app  535 Feb 20  2014 configuration.xsl
-rw-rw-r-- 1 app app 2161 Feb 20  2014 log4j.properties
-rw-rw-r-- 1 app app  922 Feb 20  2014 zoo_sample.cfg
[root@localhost conf]# cp zoo_sample.cfg zoo.cfg

[root@localhost log]# mkdir -p zookeeperdata/data
[root@localhost conf]# vi zoo.cfg 
```
修改内容如下 zoo.cfg 
```txt
tickTime=2000
# The number of ticks that the initial
# synchronization phase can take
initLimit=10
# The number of ticks that can pass between
# sending a request and getting an acknowledgement
syncLimit=5
# the directory where the snapshot is stored.
dataDir=/app/zookeeper/zookeeperdata/data
dataLogDir=/app/zookeeper/zookeeperdata/log
# the port at which the clients will connect
clientPort=2181
     
server.1=10.10.10.100:2888:3888
server.2=10.10.10.101:2888:3888
server.3=10.10.10.102:2888:3888

```
说明
```txt
*   tickTime=2000
	tickTime这个时间是作为Zookeeper服务器之间或客户端与服务器之间维持心跳的时间间隔，也就是每个tickTime时间就会发送一个心跳；
*   initLimit=10
	initLimit这个配置项是用来配置Zookeeper接受客户端（这里所说的客户端不是用户连接Zookeeper服务器的客户端，而是Zookeeper服务器集群中连接到Leader的Follower 服务器）初始化连接时最长能忍受多少个心跳时间间隔数。 当已经超过10个心跳的时间（也就是tickTime）长度后 Zookeeper 服务器还没有收到客户端的返回信息，那么表明这个客户端连接失败。总的时间长度就是 10*2000=20 秒
*   syncLimit=5
	syncLimit这个配置项标识Leader与Follower之间发送消息，请求和应答时间长度，最长不能超过多少个tickTime的时间长度，总的时间长度就是5*2000=10秒；
*   dataDir=/export/search/zookeeper-cluster/zookeeper-3.4.6-node1/data
	dataDir顾名思义就是Zookeeper保存数据的目录，默认情况下Zookeeper将写数据的日志文件也保存在这个目录里；
*   clientPort=2181
	clientPort这个端口就是客户端连接Zookeeper服务器的端口，Zookeeper会监听这个端口接受客户端的访问请求；
*   server.A=B:C:D
	server.1=localhost:2887:3887
	server.2=localhost:2888:3888
	server.3=localhost:2889:3889
A是一个数字，表示这个是第几号服务器； B是这个服务器的ip地址； C第一个端口用来集群成员的信息交换，表示的是这个服务器与集群中的Leader服务器交换信息的端口； D是在leader挂掉时专门用来进行选举leader所用。
```

#### 命令7 创建ServerID标识
除了修改zoo.cfg配置文件，集群模式下还要配置一个文件myid，这个文件在dataDir目录下，这个文件里面就有一个数据就是A的值，在上面配置文件中zoo.cfg中配置的dataDir路径中创建myid文件
```shell
[root@localhost data]# cd /app/zookeeper/zookeeperdata/data/
[root@localhost data]# vi myid
```
！注意！
节点的myid 分别为:
```txt
 100 -> 1
 101 -> 2
 102 -> 3
```
#### 命令8 逐个启动新节点实例
在三个节点上均执行完毕后；
启动zookeeper命令
```shell
bin/zkServer.sh start
```
#### 命令9 检测集群是否启动
```shell
bin/zkCli.sh -server IP:2181
bin/zkCli.sh

查看ZK服务状态:       sh bin/zkServer.sh status
```