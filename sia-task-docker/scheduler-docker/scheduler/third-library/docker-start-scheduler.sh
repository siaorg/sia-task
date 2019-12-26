#!/usr/bin/env bash
nginx -tc /opt/nginx.conf && nginx -c /opt/nginx.conf

# start zookeeper server
tar -zxvf /opt/zookeeper-3.4.6.tar.gz -C /opt/
mv /opt/zookeeper-3.4.6/conf/zoo_sample.cfg /opt/zookeeper-3.4.6/conf/zoo.cfg
sh /opt/zookeeper-3.4.6/bin/zkServer.sh start

sleep 3

# start config project
javaOpts="-server -Xms128m -Xmx256m -Xss256k -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:CMSIncrementalDutyCycle=10 -XX:+UseParNewGC -XX:+UseCMSCompactAtFullCollection -XX:-CMSParallelRemarkEnabled -XX:CMSFullGCsBeforeCompaction=0 -XX:CMSInitiatingOccupancyFraction=70 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=."
java $javaOpts -XX:OnOutOfMemoryError='kill -9 %p'  -Dspring.config.location=/app/jar/ROOT/jarConfig/ -Dspring.profiles.active=config  -jar /app/jar/ROOT/jarPackage/sia-task-config-1.0.0.jar &

sleep 3

# start scheduler project
javaOpts="-server -Xms128m -Xmx256m -Xss256k -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:CMSIncrementalDutyCycle=10 -XX:+UseParNewGC -XX:+UseCMSCompactAtFullCollection -XX:-CMSParallelRemarkEnabled -XX:CMSFullGCsBeforeCompaction=0 -XX:CMSInitiatingOccupancyFraction=70 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=."
java $javaOpts -XX:OnOutOfMemoryError='kill -9 %p'  -Dspring.config.location=/app/jar/ROOT/jarConfig/ -Dspring.profiles.active=scheduler  -jar  /app/jar/ROOT/jarPackage/sia-task-scheduler-1.0.0.jar &

echo "启动完毕"

while true;do
echo ">>>" > /tmp/acc.log
sleep 60
done

