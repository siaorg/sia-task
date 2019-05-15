#!/bin/sh
source /etc/profile
process_flag=$1

working_directory=$2

start_command=$3

now=$(date "+%Y-%m-%d %H:%M:%S")
# max log file count
log_file_max=30

# interval of run 
step=15
if [[ "$4" -ge 5 && "$4" -le 60 ]]; then
	step=$4
fi

for(( i = 0; i < 60; i=(i+step) )); do

    # judge process running
    count=`ps -fC java | grep $process_flag | wc -l`
    if [ $count -eq 0 ]; then
        # log
        echo "$now $process_flag restart by $start_command">>$working_directory/$(date "+%Y-%m-%d")_restart.out
        # start 
        cd $working_directory
        nohup $start_command 1>/dev/null 2>&1 &

        # clear log file
        if [ $(find $working_directory -name "*_restart.out" | wc -l) -gt $log_file_max ]; then
            rm -rf $(ls $working_directory/*_restart.out|head -n1)
        fi
    fi

    # 
    if [ $step -ge 60 ];then
        exit 0
    else
        sleep $step
    fi

done
