#!/bin/sh
nohup ./stop.sh task_config_open  >$(pwd)/task_config_open.shutdown 2>&1 &
