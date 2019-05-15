#!/bin/sh

# First, delete crontab
process_flag=$1
count=`crontab -l 2>/dev/null | grep "$process_flag" | wc -l`
if [ $count -ne 0 ]; then
    cronfile=$(pwd)/$process_flag".cron.stop"
    crontab -l | grep -v "$process_flag" > $cronfile
    crontab $cronfile
    rm $cronfile
fi

# Second, kill running watcher
runing_watcher=$(ps -ef | grep "task_proc_watcher.sh" | grep "$process_flag" |grep -v grep | awk '{printf "%s ",$2}')
for pid in $runing_watcher; do
	echo "watcher will be killed by kill -9 $pid"
    kill -9 "$pid"
done

# Third, kill application
app_pid=$(ps -ef | grep "$process_flag" |grep -v grep |grep -v "stop.sh"| awk '{printf "%s ",$2}')
for pid in $app_pid; do
    echo "application will be killed by kill -9 $pid"
    kill -9 "$pid"
done
