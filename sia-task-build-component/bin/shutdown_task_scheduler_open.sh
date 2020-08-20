#!/bin/sh
current_dir=$(pwd)
cd $current_dir/bin
nohup sh stop.sh task_scheduler_open  >$(pwd)/task_scheduler_open.shutdown 2>&1 &
