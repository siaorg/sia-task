#!/bin/sh
nohup sh run4scheduler.sh task_scheduler_open sia-task-scheduler-1.0.0.jar >$(pwd)/task_scheduler_open.start 2>&1 &
