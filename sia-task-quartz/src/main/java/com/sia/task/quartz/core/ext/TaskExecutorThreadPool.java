/*-
 * <<
 * task
 * ==
 * Copyright (C) 2019 - 2020 sia
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * >>
 */

package com.sia.task.quartz.core.ext;

import com.sia.task.quartz.ThreadPool;
import com.sia.task.quartz.exception.SchedulerConfigException;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 提供扩展的线程池，和<code>{@Link SimpleThreadPool}<code/>设计基本一样；
 * 支持线程的动态伸缩，为预防线程的动态伸缩达到操作系统的线程瓶颈，所以建议不要超过其最大值，默认最大值为20;
 *
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-05-23 19:09
 * @see
 **/
@Data
public class TaskExecutorThreadPool implements ThreadPool {


    private static final String MAIN = "main";
    private static final String WORKER_THREAD_LAST_JOB = "WorkerThread-LastJob";
    private static final String WORKER_THREAD_EXT = "—WorkerThread-EXT";

    private static final int PROTECT_THREAD_MAX = 20;


    private List<WorkerThread> threadWorkers;
    private LinkedList<WorkerThread> availWorkers = new LinkedList<>();
    private LinkedList<WorkerThread> busyWorkers = new LinkedList<>();
    private LinkedList<WorkerThread> gcWorkers = new LinkedList<>();

    private int threadCount = -1;

    private int threadPriority = Thread.NORM_PRIORITY;

    private boolean isShutdown = false;

    private volatile boolean handoffPending = false;

    private boolean inheritLoader = false;

    private boolean inheritGroup = true;

    private boolean makeThreadsDaemons = false;

    private ThreadGroup threadGroup;

    private final Object runnableLock = new Object();


    /**
     * The prefix of the schedule thread name
     */
    private String threadNamePrefix;

    public static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutorThreadPool.class);

    /**
     * schedulerInstanceName
     * Create multiple instances by injecting different names.
     */
    private String schedulerInstanceName;


    /**
     * Provide a default constructor.
     * An instance is created by reflection during initialization.
     */
    public TaskExecutorThreadPool() {
    }

    /**
     * <p>
     * Execute the given <code>{@link Runnable}</code> in the next
     * available <code>Thread</code>.
     * </p>
     *
     * <p>
     * The implementation of this interface should not throw exceptions unless
     * there is a serious problem (i.e. a serious misconfiguration). If there
     * are no immediately available threads <code>false</code> should be returned.
     * </p>
     *
     * @param runnable
     * @return true, if the runnable was assigned to run on a Thread.
     */
    @Override
    public boolean runInThread(Runnable runnable) {

        if (runnable == null) {
            return false;
        }

        synchronized (runnableLock) {
            handoffPending = true;

            while (availWorkers.size() <= 0 && !isShutdown) {
                try {
                    runnableLock.wait(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!isShutdown) {
                WorkerThread workerThread = availWorkers.removeFirst();
                busyWorkers.add(workerThread);
                workerThread.run(runnable);
            } else {
                WorkerThread workerThread = new WorkerThread(this, threadGroup, WORKER_THREAD_LAST_JOB, threadPriority, makeThreadsDaemons, runnable);
                busyWorkers.add(workerThread);
                workerThread.start();
            }

            runnableLock.notifyAll();
            handoffPending = false;
        }

        return true;
    }



    /**
     * <p>
     * Determines the number of threads that are currently available in in
     * the pool.  Useful for determining the number of times
     * <code>runInThread(Runnable)</code> can be called before returning
     * false.
     * </p>
     *
     * <p>The implementation of this method should block until there is at
     * least one available thread.</p>
     *
     * @return the number of currently available threads
     */
    @Override
    public int blockForAvailableThreads() {
        synchronized (runnableLock) {
            while ((availWorkers.size() <= 0 || handoffPending) && !isShutdown) {
                try {
                    runnableLock.wait(500);
                    if (availWorkers.size()<=0) {
                        // TODO
                        String extEnable = System.getProperty(threadNamePrefix, String.valueOf(false));
                        if (String.valueOf(true).equals(extEnable)&& threadWorkers.size() < PROTECT_THREAD_MAX){
                            //reset
                            System.setProperty(threadNamePrefix,String.valueOf(false));
                            createWorkerThreads(1);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return availWorkers.size();
    }

    /**
     * 初始化两个线程池
     * <p>
     * Must be called before the <code>ThreadPool</code> is
     * used, in order to give the it a chance to initializeFromProp.
     * </p>
     *
     * <p>Typically called by the <code>SchedulerFactory</code>.</p>
     */
    @Override
    public void initialize() throws SchedulerConfigException {

        //检查是否已经初始化
        if (threadWorkers != null && threadWorkers.size() > 0) {
            return;
        }

        if (threadCount <= 0) {
            throw new SchedulerConfigException("Thread count must be > 0");
        }

        if (Thread.MIN_PRIORITY - 1 >= threadPriority || threadPriority > Thread.MAX_PRIORITY - 1) {
            throw new SchedulerConfigException("Thread priority must be > 0 and <= 9");
        }

        threadGroup = Thread.currentThread().getThreadGroup();
        if (!isInheritGroup()) {
            ThreadGroup parentThreadGroup = threadGroup;
            while (!MAIN.equals(parentThreadGroup.getName())) {
                parentThreadGroup = parentThreadGroup.getParent();
            }
            threadGroup = new ThreadGroup(parentThreadGroup, schedulerInstanceName + "-TaskExecutorThreadPool");
            if (isMakeThreadsDaemons()) {
                threadGroup.setDaemon(true);
            }
        }
        LOGGER.info("threadGroup is {}", threadGroup.getName());

        //TODO Remove code that doesn't make sense here if necessary
        if (isInheritLoader()) {
            LOGGER.info("Job execution threads will use class loader of thread: " + Thread.currentThread().getName());
        }

        //Initialize workerThreads, if threadCount > 0 then create the same number of threads as threadCount and start them
        Iterator<WorkerThread> workerThreadIterator = createWorkerThreads(threadCount).iterator();
        while (workerThreadIterator.hasNext()) {
            WorkerThread workerThread = workerThreadIterator.next();
            workerThread.start();
            availWorkers.add(workerThread);
        }

    }

    /**
     * Terminate any worker threads in this thread group.
     *
     * <p>
     * Called by the QuartzScheduler to inform the <code>ThreadPool</code>
     * that it should free up all of it's resources because the scheduler is
     * shutting down.
     * </p>
     *
     * @param waitForJobsToComplete
     */
    @Override
    public void shutdown(boolean waitForJobsToComplete) {
        synchronized (runnableLock) {
            LOGGER.info("Shutting down taskExecutorThreadPool...");
            isShutdown = true;
            if (threadWorkers == null) {
                return;
            }

            Iterator<WorkerThread> workerThreadIterator = threadWorkers.iterator();
            while (workerThreadIterator.hasNext()) {
                WorkerThread workerThread = workerThreadIterator.next();
                workerThread.shutdown();
                availWorkers.remove(workerThread);
            }

            runnableLock.notifyAll();
            LOGGER.info("Shutdown of taskExecutorThreadPool complete.");
        }
    }

    /**
     * <p>Get the current number of threads in the <code>ThreadPool</code>.</p>
     */
    @Override
    public int getPoolSize() {
        return getThreadCount();
    }

    /**
     * <p>Inform the <code>ThreadPool</code> of the Scheduler instance's Id,
     * prior to initializeFromProp being invoked.</p>
     *
     * @param schedInstId
     * @since 1.7
     */
    @Override
    public void setInstanceId(String schedInstId) {

    }

    /**
     * <p>Inform the <code>ThreadPool</code> of the Scheduler instance's name,
     * prior to initializeFromProp being invoked.</p>
     *
     * @param schedName
     * @since 1.7
     */
    @Override
    public void setInstanceName(String schedName) {
        schedulerInstanceName = schedName;
    }


    /**
     * 添加线程池伸缩的功能
     * @param threadCount
     * @return
     */
    private List<WorkerThread> createWorkerThreads(int threadCount) {
        if (threadWorkers == null) {
            threadWorkers = new LinkedList<>();
            for (int i = 0; i < threadCount; i++) {
                String prefix = getThreadNamePrefix();
                if (prefix == null) {
                    prefix = schedulerInstanceName + "_Worker";
                }
                WorkerThread workerThread = new WorkerThread(this, threadGroup, prefix + "-" + i, getThreadPriority(), isMakeThreadsDaemons());
                if (isInheritLoader()) {
                    workerThread.setContextClassLoader(Thread.currentThread().getContextClassLoader());
                }
                threadWorkers.add(workerThread);
            }
        } else {
            if (gcWorkers.isEmpty()){
                WorkerThread workerThread = gcWorkers.remove();
                threadWorkers.add(workerThread);
                workerThread.start();
                availWorkers.add(workerThread);
            } else {
                int size = threadWorkers.size();
                for (int i = 0; i < threadCount; i++) {
                    String prefix = getThreadNamePrefix();
                    if (prefix == null) {
                        prefix = schedulerInstanceName + WORKER_THREAD_EXT +"_Worker";
                    }
                    WorkerThread workerThread = new WorkerThread(this, threadGroup, prefix + "-" + size, getThreadPriority(), isMakeThreadsDaemons());
                    if (isInheritLoader()) {
                        workerThread.setContextClassLoader(Thread.currentThread().getContextClassLoader());
                    }
                    threadWorkers.add(workerThread);
                    workerThread.start();
                    availWorkers.add(workerThread);
                }
            }
        }
        return threadWorkers;
    }

    /**
     * <p>
     * A Worker loops, waiting to execute tasks.
     * </p>
     */
    class WorkerThread extends Thread {

        private Runnable runnable;

        private final Object lock = new Object();

        private AtomicBoolean run = new AtomicBoolean(true);

        private TaskExecutorThreadPool taskExecutorThreadPool;

        private boolean runOnce = false;

        /**
         * <p>
         * Create a worker thread and start it. Waiting for the next Runnable,
         * executing it, and waiting for the next Runnable, until the shutdown
         * flag is set.
         * </p>
         */
        WorkerThread(TaskExecutorThreadPool tp, ThreadGroup threadGroup, String name, int threadPriority, boolean isDaemon) {
            this(tp, threadGroup, name, threadPriority, isDaemon, null);
        }

        /**
         * <p>
         * Create a worker thread, start it, execute the runnable and terminate
         * the thread (one time execution).
         * </p>
         */
        WorkerThread(TaskExecutorThreadPool taskExecutorThreadPool, ThreadGroup threadGroup, String name, int threadPriority, boolean isDaemon, Runnable runnable) {

            super(threadGroup, name);
            this.taskExecutorThreadPool = taskExecutorThreadPool;
            this.runnable = runnable;
            if (runnable != null) {
                runOnce = true;
            }
            setPriority(threadPriority);
            setDaemon(isDaemon);
        }

        /**
         * <p>
         * Loop, executing targets as they are received.
         * </p>
         */
        @Override
        public void run() {

            boolean ran = false;

            while (run.get()) {
                try {
                    synchronized (lock) {
                        while (runnable == null && run.get()) {
                            lock.wait(500);
                        }

                        if (runnable != null) {
                            ran = true;
                            runnable.run();
                        }
                    }
                } catch (Exception unblock) {
                    LOGGER.error("Error while executing the Runnable or Worker thread was interrupt()'ed.", unblock);
                } finally {
                    synchronized (lock) {
                        runnable = null;
                    }
                    // repair the thread in case the runnable mucked it up...
                    if (getPriority() != threadPriority) {
                        setPriority(threadPriority);
                    }

                    if (runOnce) {
                        run.set(false);
                        clearFromBusyWorkersList(this);
                    } else if (ran) {
                        ran = false;
                        makeAvailable(this);
                    }
                }
            }

            LOGGER.debug("WorkerThread is shut down.");

        }

        public void run(Runnable newRunnable) {
            synchronized (lock) {
                if (runnable != null) {
                    throw new IllegalStateException("Already running a Runnable!");
                }

                runnable = newRunnable;
                lock.notifyAll();
            }
        }
        /**
         * <p>
         * Signal the thread that it should terminate.
         * </p>
         */
        void shutdown() {
            run.set(false);
        }
    }

    /**
     * 增加移除扩展线程，放入GC队列，不会立马回收，会等待一段时间后进行回收
     * @param workerThread
     */
    protected void makeAvailable(WorkerThread workerThread) {
        synchronized (runnableLock) {
            if (!isShutdown){
                if (!workerThread.getName().contains(WORKER_THREAD_EXT)){
                    availWorkers.add(workerThread);
                } else {
                    gcWorkers.add(workerThread);
                }
            }
            busyWorkers.remove(workerThread);
            runnableLock.notifyAll();
        }
    }

    protected void clearFromBusyWorkersList(WorkerThread wt) {
        synchronized (runnableLock) {
            busyWorkers.remove(wt);
            runnableLock.notifyAll();
        }
    }
}
