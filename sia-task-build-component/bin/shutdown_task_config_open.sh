#!/bin/sh
case "`uname`" in
	Linux)
		bin_absolute_path=$(readlink -f $(dirname $0))
		;;
	*)
		bin_absolute_path=`cd $(dirname $0);pwd`
		;;
esac

export LANG=en_US.UTF-8
base_dir=${bin_absolute_path}/..
cd $base_dir/bin
nohup sh stop.sh task_config_open  >$(pwd)/task_config_open.shutdown 2>&1 &
