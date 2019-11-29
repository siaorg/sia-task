#!/usr/bin/env bash
nginx -tc /opt/nginx.conf && nginx -c /opt/nginx.conf

#cd /app/jar/ROOT/jarBin
#chmod +x *.sh 
#/app/jar/ROOT/jarBin/start_task_config_open.sh
#/app/jar/ROOT/jarBin/start_task_scheduler_open.sh

task_config="task_config_open.yml"
task_scheduler="task_scheduler_open.yml"
javaOpts="-server -Xms128m -Xmx256m -Xss256k -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:CMSIncrementalDutyCycle=10 -XX:+UseParNewGC -XX:+UseCMSCompactAtFullCollection -XX:-CMSParallelRemarkEnabled -XX:CMSFullGCsBeforeCompaction=0 -XX:CMSInitiatingOccupancyFraction=70 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=."
java $javaOpts -XX:OnOutOfMemoryError='kill -9 %p'  -Dspring.config.location=./jarConfig/$task_config  -jar /app/jar/ROOT/jarPackage/sia-task-config-1.1.0.jar &

javaOpts="-server -Xms128m -Xmx256m -Xss256k -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:CMSIncrementalDutyCycle=10 -XX:+UseParNewGC -XX:+UseCMSCompactAtFullCollection -XX:-CMSParallelRemarkEnabled -XX:CMSFullGCsBeforeCompaction=0 -XX:CMSInitiatingOccupancyFraction=70 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=."
java $javaOpts -XX:OnOutOfMemoryError='kill -9 %p'  -Dspring.config.location=./jarConfig/$task_scheduler  -jar  /app/jar/ROOT/jarPackage/sia-task-scheduler-1.0.0.jar &

echo "启动完毕"

while true;do
echo ">>>" > /tmp/acc.log
sleep 60
done

