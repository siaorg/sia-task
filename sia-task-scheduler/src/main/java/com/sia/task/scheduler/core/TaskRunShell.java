package com.sia.task.scheduler.core;

import com.sia.task.core.IExecutorSelector;
import com.sia.task.core.exceptions.SchedulerBaseException;
import com.sia.task.core.exceptions.TaskBaseExecutionException;
import com.sia.task.core.http.ResponseStatus;
import com.sia.task.core.http.SiaHttpResponse;
import com.sia.task.core.log.LogMessageConstant;
import com.sia.task.core.log.LogStatusEnum;
import com.sia.task.core.task.DagTask;
import com.sia.task.core.task.SiaJobStatus;
import com.sia.task.core.util.Constant;
import com.sia.task.core.util.ExecuteTaskThreadPool;
import com.sia.task.core.util.JsonHelper;
import com.sia.task.scheduler.exception.OnlineTaskExecutionException;
import com.sia.task.scheduler.ext.RouteStrategy;
import com.sia.task.scheduler.failover.FailoverEnum;
import com.sia.task.scheduler.log.LogService;
import com.sia.task.scheduler.task.OnlineTask;
import com.sia.task.scheduler.task.SiaTaskDetail;
import com.sia.task.scheduler.task.TriggerOnlineTaskBundle;
import com.sia.task.scheduler.util.TaskMessageUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;


/**
 * TaskRunShell instances are responsible for providing the 'safe' environment
 * for <code>JobMTask</code> s to run in, and for performing all of the work of
 * executing the <code>JobMTask</code>
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2019-10-10 19:48
 **/
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class TaskRunShell implements Runnable {

    private OnlineTaskExecutionContext otec;

    private OnlineScheduler onLineScheduler;

    private TriggerOnlineTaskBundle taskBundle;

    public TaskRunShell(TriggerOnlineTaskBundle taskBundle) {
        this.taskBundle = taskBundle;
    }

    public void initialize(OnlineScheduler onLineScheduler) {
        this.onLineScheduler = onLineScheduler;

        SiaTaskDetail taskDetail = taskBundle.getOnlineTaskDetail();
        DagTask dagTask = taskDetail.getDagTask();
        taskBundle.setModifyOnlineJobStatus(onLineScheduler.getModifyOnlineJobStatus());
        IExecutorSelector selector = onLineScheduler.getIExecutorSelector();
        String instance;
        OnlineTask onlineTask = null;
        try {
            List<String> taskExecutor = selector.getTaskExecutor(dagTask);
            if (RouteStrategy.ROUTE_TYPE_SPECIFY.getRouteType().equals(dagTask.getRouteStrategy())) {
                dagTask.setExecutors(new ArrayList<>(Collections.singletonList(dagTask.getFixIp())));
            } else {
                dagTask.setExecutors(taskExecutor);
            }
            instance = RouteStrategy.match(dagTask.getRouteStrategy()).getExecutorRouter().routeInstance(dagTask, taskExecutor);
            dagTask.setCurrentHandler(instance);
            onlineTask = onLineScheduler.getOnlineTaskFactory().newOnlineTask(taskBundle);
        } catch (Exception e) {
            String mess = "initialize : An error occured instantiating onlineTask to be executed. onlineTask= '" + taskDetail.getOnlineTaskClass().getName() + "' - " + e.getMessage();
            notifyListenersExecutedError(taskBundle, new OnlineTaskExecutionException(mess, e));
        }

        this.otec = new OnlineTaskExecutionContextImpl(onlineTask);
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        run(taskBundle.getOnlineTaskDetail().getDagTask());
    }

    /**
     * Execution entrance of task remote scheduling
     *
     * @param dagTask dag task
     */
    private void run(DagTask dagTask) {
        try {
            if (Constant.ENDTASK.equals(dagTask.getTaskKey())) {
                runEndTask(dagTask);
                return;
            }
            executeTask(dagTask);
        } catch (SchedulerBaseException e) {
            log.info(Constant.LOG_EX_PREFIX + "Exception : run method to be called : ", e);
        }
    }

    /**
     * The last task in the dag task is used to mark the end。
     * <code>{@link TaskRunShell#runEndTask}</code> does not really initiate a remote execution request,
     * but serves as a sign that the end of a job scheduling
     *
     * @param dagTask task
     * @throws SchedulerBaseException
     */
    private void runEndTask(DagTask dagTask) throws SchedulerBaseException {

        if (dagTask.getPreTaskCounter().get() < dagTask.getPreTask().size()) {
            return;
        }
        /*
         * 三种失败原因：
         * 1. cas 失败：任务当前状态已经不是running；
         * 2. 任务不归属当前调度器；
         * 3. 网络中断，修改失败；
         */
        if (completedJobStatus(dagTask)) {
            // task 执行完成通知
            notifyListenersExecuted(taskBundle);
            return;
        }
        log.error(Constant.LOG_EX_PREFIX + " runEndTask[{}] - completedJobStatus : fail.", dagTask.getJobKey());
        LogService.produceLog(dagTask, " runEndTask[{" + dagTask.getJobKey() + "}] - completedJobStatus : fail.", LogStatusEnum.LOG_STATUS_TASK_HANDLE_FAIL_STOP);
    }

    private void executeTask(DagTask dagTask) throws TaskBaseExecutionException {

        if (!ifHasAbilityExecute(dagTask)) {
            throw new TaskBaseExecutionException(" The scheduler has no permission to execute the task, either because the task has been transferred or the status of the task has changed.");
        }

        notifyListenersBeginning(taskBundle);
        OnlineTask onlineTaskInstance = otec.getOnlineTaskInstance();

        // execute the onlineTask
        onlineTaskInstance.run(dagTask).addCallback(new ListenableFutureCallback<ResponseEntity<SiaHttpResponse>>() {

            /**
             * Called when the {@link ListenableFuture} completes with success.
             * <p>Note that Exceptions raised by this method are ignored.
             *
             * @param result the result
             */
            @Override
            public void onSuccess(ResponseEntity<SiaHttpResponse> result) {
                try {
                    log.info(Constant.LOG_PREFIX + ">>->>->>->>->> onSuccess [{}] <<-<<-<<-<<-<<", dagTask.getJobKey());
                    log.info(Constant.LOG_PREFIX + ">>->>->>->>->> onSuccess [{}] - [{}] <<-<<-<<-<<-<<", dagTask.getJobKey(), result);
                    SiaHttpResponse response = result.getBody();
                    if (ResponseStatus.success.equals(response.getStatus().getStatus())) {
                        vSuccess(response, dagTask);
                    } else {
                        dagTask.setOutParam(JsonHelper.toString(response));
                        vFailure(null, dagTask);
                    }
                } catch (Exception e) {
                    log.error(Constant.LOG_EX_PREFIX + "Callback onSuccess - Exception [{}]", dagTask.getJobKey(), e);
                    dagTask.setOutParam(JsonHelper.toString(result.getBody()));
                    vFailure(null, dagTask);
                }
            }

            /**
             * Called when the {@link org.springframework.util.concurrent.ListenableFuture} completes with failure.
             * <p>Note that Exceptions raised by this method are ignored.
             * @param ex the failure
             */
            @Override
            public void onFailure(Throwable ex) {
                log.info(Constant.LOG_EX_PREFIX + ">>->>->>->>->> onFailure [{}] <<-<<-<<-<<-<<", dagTask.getJobKey(), ex);
                vFailure(ex, dagTask);
            }
        });
    }

    private void vFailure(Throwable ex, DagTask mTask) {

        try {
            notifyListenersExecutedError(taskBundle, ex);

            if (FailoverEnum.STOP.getValue().equals(mTask.getFailover())) {
                taskBundle.getModifyOnlineJobStatus().stopJobStatus(mTask, TaskMessageUtil.mapToMessage(mTask, ex, LogMessageConstant.LOG_TASK_MSG_FAIL_STOP));
                return;
            }

            if (FailoverEnum.IGNORE.getValue().equals(mTask.getFailover())) {
                if (ex != null) {
                    mTask.setOutParam(null);
                }
                postTaskSubmit(mTask);
                return;
            }

            if (FailoverEnum.TRANSFER.getValue().equals(mTask.getFailover())) {
                mTask.setOutParam(null);
                if (transfer(mTask)) {
                    return;
                }
                mTask.setFailoverFlag(true);
                taskBundle.getModifyOnlineJobStatus().stopJobStatus(mTask, TaskMessageUtil.mapToMessage(mTask, ex, LogMessageConstant.LOG_TASK_MSG_FAIL_TRANSFER_STOP));
                notifyListenersExecutedError(taskBundle, ex);
                return;
            }

            if (FailoverEnum.MULTI_CALLS.getValue().equals(mTask.getFailover())) {
                if (!mTask.isFailoverFlag()) {
                    mTask.setFailoverFlag(true);
                    run(mTask);
                    return;
                }
                taskBundle.getModifyOnlineJobStatus().stopJobStatus(mTask, TaskMessageUtil.mapToMessage(mTask, ex, LogMessageConstant.LOG_STATUS_TASK_HANDLE_MULTI_CALLS_STOP));
                return;
            }

            if (FailoverEnum.MULTI_CALLS_TRANSFER.getValue().equals(mTask.getFailover())) {
                if (!mTask.isFailoverFlag()) {
                    mTask.setFailoverFlag(true);
                    run(mTask);
                    return;
                }
                if (transfer(mTask)) {
                    return;
                }
                taskBundle.getModifyOnlineJobStatus().stopJobStatus(mTask, TaskMessageUtil.mapToMessage(mTask, ex, LogMessageConstant.LOG_STATUS_TASK_HANDLE_MULTI_CALLS_TRANSFER_STOP));
                notifyListenersExecutedError(taskBundle, ex);
            }
        } catch (Exception e) {
            log.error(Constant.LOG_EX_PREFIX + " There is an exception that cannot be fixed, and it is necessary to manually repair the running of the task. [{}]", mTask, e);
        }
    }

    private void vSuccess(SiaHttpResponse response, DagTask dagTask) {
        try {
            dagTask.setOutParam(JsonHelper.toString(response));
            notifyListenersExecuted(taskBundle);
        } catch (TaskBaseExecutionException e) {
            log.info(Constant.LOG_EX_PREFIX + " vSuccess -> [{}] notifyListenersExecuted ", dagTask.getJobKey(), e);
        }
        postTaskSubmit(dagTask);
    }

    private boolean transfer(DagTask dagTask) throws Exception {
        List<String> executors = dagTask.getExecutors();
        executors.remove(dagTask.getCurrentHandler());
        if (!executors.isEmpty()) {
            String instance = RouteStrategy.match(dagTask.getRouteStrategy()).getExecutorRouter().routeInstance(dagTask, executors);
            dagTask.setCurrentHandler(instance);
            run(dagTask);
            return true;
        }
        dagTask.setCurrentHandler(null);
        return false;
    }

    /**
     * @param dagTask
     */
    private void postTaskSubmit(DagTask dagTask) {
        dagTask.getPostTask().forEach(task -> {
            if (task.getPreTaskCounter().incrementAndGet() < task.getPreTask().size()) {
                log.info(Constant.LOG_PREFIX + " The pre-task tasks are not all completed, and the post-task tasks are not started.{}", dagTask.getTaskKey());
                try {
                    notifyTaskListenersUnExecuted(taskBundle);
                } catch (TaskBaseExecutionException e) {
                    String mess = " The pre-task tasks are not all completed, and the post-task tasks are not started.{" + dagTask.getTaskKey() + "}";
                    notifyListenersExecutedError(taskBundle, new OnlineTaskExecutionException(mess, e));
                }
                return;
            }
            SiaTaskDetail taskDetail = OnlineTaskBuild.newOnlineTaskBuild(taskBundle.getOnlineTaskDetail().getOnlineTaskClass()).build();
            task.setTraceId(dagTask.getTraceId());
            taskDetail.setDagTask(task);

            TriggerOnlineTaskBundle taskBundle = new TriggerOnlineTaskBundle(taskDetail);
            TaskRunShell runShell = new TaskRunShell(taskBundle);
            runShell.initialize(this.onLineScheduler);
            ExecutorService service = ExecuteTaskThreadPool.getExecutorService(task.getJobGroup());
            service.execute(runShell);
        });
    }

    /**
     * Determine if there is any ability to continue execution
     *
     * @param dagTask
     * @return
     */
    private boolean ifHasAbilityExecute(DagTask dagTask) throws TaskBaseExecutionException {
        return taskBundle.getModifyOnlineJobStatus().isJobOwner(dagTask, Constant.LOCALHOST) && taskBundle.getModifyOnlineJobStatus().getJobStatus(dagTask).equals(SiaJobStatus.RUNNING.getStatus());
    }

    private boolean completedJobStatus(DagTask dagTask) throws TaskBaseExecutionException {
        return taskBundle.getModifyOnlineJobStatus().completedJobStatus(dagTask);
    }

    private void notifyListenersBeginning(TriggerOnlineTaskBundle taskBundle) throws TaskBaseExecutionException {
        onLineScheduler.notifyTaskListenersExecuteStarted(taskBundle);
    }

    private void notifyTaskListenersUnExecuted(TriggerOnlineTaskBundle taskBundle) throws TaskBaseExecutionException {
        onLineScheduler.notifyTaskListenersUnExecuted(taskBundle);
    }

    private void notifyListenersExecutedError(TriggerOnlineTaskBundle taskBundle, Throwable ex) {
        onLineScheduler.notifyTaskListenersExecutedError(taskBundle, new SchedulerBaseException(ex));
    }

    private void notifyListenersExecuted(TriggerOnlineTaskBundle taskBundle) throws TaskBaseExecutionException {
        onLineScheduler.notifyTaskListenersExecuted(taskBundle);
    }
}
