#!/bin/sh
nohup ./stop.sh task_scheduler_open  >$(pwd)/task_scheduler_open.shutdown 2>&1 &
