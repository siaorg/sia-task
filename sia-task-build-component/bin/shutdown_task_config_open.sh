#!/bin/sh
current_dir=$(pwd)
cd $current_dir/bin
nohup sh stop.sh task_config_open  >$(pwd)/task_config_open.shutdown 2>&1 &
