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

package com.sia.task.quartz.core;


import com.sia.task.quartz.ClassLoadHelper;
import com.sia.task.quartz.QuartzInitConfiguration;
import com.sia.task.quartz.ThreadPool;
import com.sia.task.quartz.core.simpl.DefaultThreadExecutor;
import com.sia.task.quartz.core.simpl.RAMJobStore;
import com.sia.task.quartz.core.simpl.SimpleThreadPool;
import com.sia.task.quartz.core.simpl.StdJobRunShellFactory;
import com.sia.task.quartz.exception.SchedulerConfigException;
import com.sia.task.quartz.exception.SchedulerException;
import com.sia.task.quartz.job.JobFactory;
import com.sia.task.quartz.job.JobStore;
import com.sia.task.quartz.job.matchers.EverythingMatcher;
import com.sia.task.quartz.listeners.JobListener;
import com.sia.task.quartz.listeners.TriggerListener;
import com.sia.task.quartz.plugin.SchedulerPlugin;
import com.sia.task.quartz.utils.PropertiesParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Method;
import java.security.AccessControlException;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;

/**
 * <p>
 * An implementation of <code>{@link SchedulerFactory}</code> that
 * does all of its work of creating a <code>QuartzScheduler</code> instance
 * based on the contents of a <code>Properties</code> file.
 * </p>
 *
 * <p>
 * By default a properties file named "quartz.properties" is loaded from the
 * 'current working directory'. If that fails, then the "quartz.properties"
 * file located (as a resource) in the org/quartz package is loaded. If you
 * wish to use a file other than these defaults, you must define the system
 * property 'org.quartz.properties' to point to the file you want.
 * </p>
 *
 * <p>
 * Alternatively, you can explicitly initializeFromProp the factory by calling one of
 * the <code>initializeFromProp(xx)</code> methods before calling <code>getScheduler()</code>.
 * </p>
 *
 * <p>
 * See the sample properties files that are distributed with Quartz for
 * information about the various settings available within the file.
 * Full configuration documentation can be found at
 * http://www.quartz-scheduler.org/docs/index.html
 * </p>
 *
 * <p>
 * Instances of the specified <code>{@link JobStore}</code>,
 * <code>{@link ThreadPool}</code>, and other SPI classes will be created
 * by name, and then any additional properties specified for them in the config
 * file will be set on the instance by calling an equivalent 'set' method. For
 * example if the properties file contains the property
 * 'org.quartz.jobStore.myProp = 10' then after the JobStore class has been
 * instantiated, the method 'setMyProp()' will be called on it. Type conversion
 * to primitive Java types (int, long, float, double, boolean, and String) are
 * performed before calling the property's setter method.
 * </p>
 * 
 * <p>
 * One property can reference another property's value by specifying a value
 * following the convention of "$@other.property.name", for example, to reference
 * the scheduler's instance name as the value for some other property, you
 * would use "$@org.quartz.scheduler.instanceName".
 * </p> 
 *
 *
 * @author @see Quartz
 * @data 2019-06-24 17:59
 * @version V1.0.0
 **/
public class StdSchedulerFactory implements SchedulerFactory {

    private SchedulerException initException = null;

    private String propSrc = null;

    private PropertiesParser cfg;

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Create an uninitialized StdSchedulerFactory.
     */
    public StdSchedulerFactory() {
    }

    /**
     * Create a StdSchedulerFactory that has been initialized via
     * <code>{@link #initializeFromProp(Properties)}</code>.
     *
     * @see #initializeFromProp(Properties)
     */
    public StdSchedulerFactory(Properties props) {
        initializeFromProp(props);
    }

    /**
     * Create a StdSchedulerFactory that has been initialized via
     * <code>{@link #initializeFromFile(String)}</code>.
     *
     * @see #initializeFromFile(String)
     */
    public StdSchedulerFactory(String fileName) throws SchedulerException {
        initializeFromFile(fileName);
    }

    public Logger getLog() {
        return log;
    }

    /**
     * <p>
     * Initialize the <code>{@link SchedulerFactory}</code> with
     * the contents of a <code>Properties</code> file and overriding System
     * properties.
     * </p>
     *
     * <p>
     * By default a properties file named "quartz.properties" is loaded from
     * the 'current working directory'. If that fails, then the
     * "quartz.properties" file located (as a resource) in the org/quartz
     * package is loaded. If you wish to use a file other than these defaults,
     * you must define the system property 'org.quartz.properties' to point to
     * the file you want.
     * </p>
     *
     * <p>
     * System properties (environment variables, and -D definitions on the
     * command-line when running the JVM) override any properties in the
     * loaded file.  For this reason, you may want to use a different initializeFromProp()
     * method if your application security policy prohibits access to
     * <code>{@link System#getProperties()}</code>.
     * </p>
     */
    public void initialize() throws SchedulerException {
        // short-circuit if already initialized
        if (cfg != null) {
            return;
        }
        if (initException != null) {
            throw initException;
        }

        String requestedFile = System.getProperty(QuartzInitConfiguration.PROPERTIES_FILE);
        String propFileName = requestedFile != null ? requestedFile
                : "quartz.properties";
        File propFile = new File(propFileName);

        Properties props = new Properties();

        InputStream in = null;

        try {
            if (propFile.exists()) {
                try {
                    if (requestedFile != null) {
                        propSrc = "specified file: '" + requestedFile + "'";
                    } else {
                        propSrc = "default file in current working dir: 'quartz.properties'";
                    }

                    in = new BufferedInputStream(new FileInputStream(propFileName));
                    props.load(in);

                } catch (IOException ioe) {
                    initException = new SchedulerException("Properties file: '"
                            + propFileName + "' could not be read.", ioe);
                    throw initException;
                }
            } else if (requestedFile != null) {
                in =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(requestedFile);

                if(in == null) {
                    initException = new SchedulerException("Properties file: '"
                        + requestedFile + "' could not be found.");
                    throw initException;
                }

                propSrc = "specified file: '" + requestedFile + "' in the class resource path.";

                in = new BufferedInputStream(in);
                try {
                    props.load(in);
                } catch (IOException ioe) {
                    initException = new SchedulerException("Properties file: '"
                            + requestedFile + "' could not be read.", ioe);
                    throw initException;
                }

            } else {
                propSrc = "default resource file in Quartz package: 'quartz.properties'";

                ClassLoader cl = getClass().getClassLoader();
                if(cl == null)
                    cl = findClassloader();
                if(cl == null)
                    throw new SchedulerConfigException("Unable to find a class loader on the current thread or class.");

                in = cl.getResourceAsStream(
                        "quartz.properties");

                if (in == null) {
                    in = cl.getResourceAsStream(
                            "/quartz.properties");
                }
                if (in == null) {
                    in = cl.getResourceAsStream(
                            "com/sia/task/quartz/core/quartz.properties");
                }
                if (in == null) {
                    initException = new SchedulerException(
                            "Default quartz.properties not found in class path");
                    throw initException;
                }
                try {
                    props.load(in);
                } catch (IOException ioe) {
                    initException = new SchedulerException(
                            "Resource properties file: 'com/sia/task/quartz/core/quartz.properties' "
                                    + "could not be read from the classpath.", ioe);
                    throw initException;
                }
            }
        } finally {
            if(in != null) {
                try { in.close(); } catch(IOException ignore) { /* ignore */ }
            }
        }

        initializeFromProp(overrideWithSysProps(props));
    }

    /**
     *
     * Add all System properties to the given <code>props</code>.  Will override
     * any properties that already exist in the given <code>props</code>.
     */
    private Properties overrideWithSysProps(Properties props) {
        Properties sysProps = null;
        try {
            sysProps = System.getProperties();
        } catch (AccessControlException e) {
            getLog().warn(
                "Skipping overriding quartz properties with System properties " +
                "during initialization because of an AccessControlException.  " +
                "This is likely due to not having read/write access for " +
                "java.util.PropertyPermission as required by java.lang.System.getProperties().  " +
                "To resolve this warning, either add this permission to your policy file or " +
                "use a non-default version of initializeFromProp().",
                e);
        }

        if (sysProps != null) {
            props.putAll(sysProps);
        }

        return props;
    }

    /**
     * <p>
     * Initialize the <code>{@link SchedulerFactory}</code> with
     * the contents of the <code>Properties</code> file with the given
     * name.
     * </p>
     */
    public void initializeFromFile(String filename) throws SchedulerException {
        // short-circuit if already initialized
        if (cfg != null) {
            return;
        }

        if (initException != null) {
            throw initException;
        }

        InputStream stream;
        Properties props = new Properties();

        stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);

        try {
            if(stream != null) {
                stream = new BufferedInputStream(stream);
                propSrc = "the specified file : '" + filename + "' from the class resource path.";
            } else {
                stream = new BufferedInputStream(new FileInputStream(filename));
                propSrc = "the specified file : '" + filename + "'";
            }
            props.load(stream);
        } catch (IOException ioe) {
            initException = new SchedulerException("Properties file: '"
                    + filename + "' could not be read.", ioe);
            throw initException;
        }
        finally {
            if(stream != null)
                try { stream.close(); } catch(IOException ignore) {}
        }

        initializeFromProp(props);
    }

    /**
     * <p>
     * Initialize the <code>{@link SchedulerFactory}</code> with
     * the contents of the <code>Properties</code> file opened with the
     * given <code>InputStream</code>.
     * </p>
     */
    public void initialize(InputStream propertiesStream)
        throws SchedulerException {
        // short-circuit if already initialized
        if (cfg != null) {
            return;
        }

        if (initException != null) {
            throw initException;
        }

        Properties props = new Properties();

        if (propertiesStream != null) {
            try {
                props.load(propertiesStream);
                propSrc = "an externally opened InputStream.";
            } catch (IOException e) {
                initException = new SchedulerException(
                        "Error loading property data from InputStream", e);
                throw initException;
            }
        } else {
            initException = new SchedulerException(
                    "Error loading property data from InputStream - InputStream is null.");
            throw initException;
        }

        initializeFromProp(props);
    }

    /**
     * <p>
     * Initialize the <code>{@link SchedulerFactory}</code> with
     * the contents of the given <code>Properties</code> object.
     * </p>
     */
    public void initializeFromProp(Properties props) {
        if (propSrc == null) {
            propSrc = "an externally provided properties instance.";
        }

        this.cfg = new PropertiesParser(props);
    }

    private Scheduler instantiate() throws SchedulerException {
        if (cfg == null) {
            initialize();
        }

        if (initException != null) {
            throw initException;
        }

        JobStore jobStore;
        ThreadPool tp;
        QuartzScheduler qs = null;
        Properties tProps;
        long idleWaitTime = -1;
        long dbFailureRetry = 15000L; // 15 secs
        String classLoadHelperClass;
        String jobFactoryClass;
        ThreadExecutor threadExecutor;


        SchedulerRepository schedRep = SchedulerRepository.getInstance();

        // Get Scheduler Properties
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        String schedName = cfg.getStringProperty(QuartzInitConfiguration.MQUARTZ_INSTANCE_NAME,
                "MQuartzScheduler");

        String threadName = cfg.getStringProperty(QuartzInitConfiguration.MQUARTZ_THREAD_NAME,
                schedName + "_M-Q-S-Thread");

        classLoadHelperClass = cfg.getStringProperty(
                QuartzInitConfiguration.MQUARTZ_CLASS_LOAD_HELPER_CLASS,
                "com.sia.task.quartz.core.simpl.CascadingClassLoadHelper");

        jobFactoryClass = cfg.getStringProperty(
                QuartzInitConfiguration.MQUARTZ_JOB_FACTORY_CLASS, null);

        idleWaitTime = cfg.getLongProperty(QuartzInitConfiguration.MQUARTZ_IDLE_WAIT_TIME,
                idleWaitTime);
        if(idleWaitTime > -1 && idleWaitTime < 1000) {
            throw new SchedulerException("com.task.quartz.scheduler.idleWaitTime of less than 1000ms is not legal.");
        }


        boolean makeSchedulerThreadDaemon =
            cfg.getBooleanProperty(QuartzInitConfiguration.MQUARTZ_MAKE_SCHEDULER_THREAD_DAEMON);

        boolean threadsInheritInitalizersClassLoader =
            cfg.getBooleanProperty(QuartzInitConfiguration.MQUARTZ_SCHEDULER_THREADS_INHERIT_CONTEXT_CLASS_LOADER_OF_INITIALIZING_THREAD);

        long batchTimeWindow = cfg.getLongProperty(QuartzInitConfiguration.MQUARTZ_BATCH_TIME_WINDOW, 0L);
        int maxBatchSize = cfg.getIntProperty(QuartzInitConfiguration.MQUARTZ_MAX_BATCH_SIZE, 1);

        boolean interruptJobsOnShutdown = cfg.getBooleanProperty(QuartzInitConfiguration.MQUARTZ_INTERRUPT_JOBS_ON_SHUTDOWN, false);
        boolean interruptJobsOnShutdownWithWait = cfg.getBooleanProperty(QuartzInitConfiguration.MQUARTZ_INTERRUPT_JOBS_ON_SHUTDOWN_WITH_WAIT, false);


        Properties schedCtxtProps = cfg.getPropertyGroup(QuartzInitConfiguration.MQUARTZ_CONTEXT_PREFIX, true);


        // Create class load helper
        ClassLoadHelper loadHelper;
        try {
            loadHelper = (ClassLoadHelper) loadClass(classLoadHelperClass)
                    .newInstance();
        } catch (Exception e) {
            throw new SchedulerConfigException(
                    "Unable to instantiate class load helper class: "
                            + e.getMessage(), e);
        }
        loadHelper.initialize();


        JobFactory jobFactory = null;
        if(jobFactoryClass != null) {
            try {
                jobFactory = (JobFactory) loadHelper.loadClass(jobFactoryClass)
                        .newInstance();
            } catch (Exception e) {
                throw new SchedulerConfigException(
                        "Unable to instantiate JobFactory class: "
                                + e.getMessage(), e);
            }

            tProps = cfg.getPropertyGroup(QuartzInitConfiguration.MQUARTZ_JOB_FACTORY_PREFIX, true);
            try {
                setBeanProps(jobFactory, tProps);
            } catch (Exception e) {
                initException = new SchedulerException("JobFactory class '"
                        + jobFactoryClass + "' props could not be configured.", e);
                throw initException;
            }
        }


        // Get ThreadPool Properties
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        String tpClass = cfg.getStringProperty(QuartzInitConfiguration.MQUARTZ_THREAD_POOL_CLASS, SimpleThreadPool.class.getName());

        if (tpClass == null) {
            initException = new SchedulerException(
                    "ThreadPool class not specified. ");
            throw initException;
        }

        try {
            tp = (ThreadPool) loadHelper.loadClass(tpClass).newInstance();
        } catch (Exception e) {
            initException = new SchedulerException("ThreadPool class '"
                    + tpClass + "' could not be instantiated.", e);
            throw initException;
        }
        tProps = cfg.getPropertyGroup(QuartzInitConfiguration.MQUARTZ_THREAD_POOL_PREFIX, true);
        try {
            setBeanProps(tp, tProps);
        } catch (Exception e) {
            initException = new SchedulerException("ThreadPool class '"
                    + tpClass + "' props could not be configured.", e);
            throw initException;
        }

        // Get JobStore Properties
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        String jsClass = cfg.getStringProperty(QuartzInitConfiguration.MQUARTZ_JOB_STORE_CLASS,
                RAMJobStore.class.getName());

        if (jsClass == null) {
            initException = new SchedulerException(
                    "JobStore class not specified. ");
            throw initException;
        }

        try {
            jobStore = (JobStore) loadHelper.loadClass(jsClass).newInstance();
        } catch (Exception e) {
            initException = new SchedulerException("JobStore class '" + jsClass
                    + "' could not be instantiated.", e);
            throw initException;
        }

        //SchedulerDetailsSetter.setDetails(jobStore, schedName, schedInstId);
        SchedulerDetailsSetter.setDetails(jobStore, schedName, null);


        tProps = cfg.getPropertyGroup(QuartzInitConfiguration.MQUARTZ_JOB_STORE_PREFIX,true);
        try {
            setBeanProps(jobStore, tProps);
        } catch (Exception e) {
            initException = new SchedulerException("JobStore class '" + jsClass
                    + "' props could not be configured.", e);
            throw initException;
        }

        // Set up any SchedulerPlugins
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        String[] pluginNames = cfg.getPropertyGroups(QuartzInitConfiguration.MQUARTZ_PLUGIN_PREFIX);
        SchedulerPlugin[] plugins = new SchedulerPlugin[pluginNames.length];
        for (int i = 0; i < pluginNames.length; i++) {
            Properties pp = cfg.getPropertyGroup(QuartzInitConfiguration.MQUARTZ_PLUGIN_PREFIX + "."
                    + pluginNames[i], true);

            String plugInClass = pp.getProperty(QuartzInitConfiguration.MQUARTZ_PLUGIN_CLASS, null);

            if (plugInClass == null) {
                initException = new SchedulerException(
                        "SchedulerPlugin class not specified for plugin '"
                                + pluginNames[i] + "'");
                throw initException;
            }
            SchedulerPlugin plugin;
            try {
                plugin = (SchedulerPlugin)
                        loadHelper.loadClass(plugInClass).newInstance();
            } catch (Exception e) {
                initException = new SchedulerException(
                        "SchedulerPlugin class '" + plugInClass
                                + "' could not be instantiated.", e);
                throw initException;
            }
            try {
                setBeanProps(plugin, pp);
            } catch (Exception e) {
                initException = new SchedulerException(
                        "JobStore SchedulerPlugin '" + plugInClass
                                + "' props could not be configured.", e);
                throw initException;
            }

            plugins[i] = plugin;
        }

        // Set up any JobListeners
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        Class<?>[] strArg = new Class[] { String.class };
        String[] jobListenerNames = cfg.getPropertyGroups(QuartzInitConfiguration.MQUARTZ_JOB_LISTENER_PREFIX);
        JobListener[] jobListeners = new JobListener[jobListenerNames.length];
        for (int i = 0; i < jobListenerNames.length; i++) {
            Properties lp = cfg.getPropertyGroup(QuartzInitConfiguration.MQUARTZ_JOB_LISTENER_PREFIX + "."
                    + jobListenerNames[i], true);

            String listenerClass = lp.getProperty(QuartzInitConfiguration.MQUARTZ_LISTENER_CLASS, null);

            if (listenerClass == null) {
                initException = new SchedulerException(
                        "JobListener class not specified for listener '"
                                + jobListenerNames[i] + "'");
                throw initException;
            }
            JobListener listener = null;
            try {
                listener = (JobListener)
                       loadHelper.loadClass(listenerClass).newInstance();
            } catch (Exception e) {
                initException = new SchedulerException(
                        "JobListener class '" + listenerClass
                                + "' could not be instantiated.", e);
                throw initException;
            }
            try {
                Method nameSetter = null;
                try {
                    nameSetter = listener.getClass().getMethod("setName", strArg);
                }
                catch(NoSuchMethodException ignore) {
                    /* do nothing */
                }
                if(nameSetter != null) {
                    nameSetter.invoke(listener, new Object[] {jobListenerNames[i] } );
                }
                setBeanProps(listener, lp);
            } catch (Exception e) {
                initException = new SchedulerException(
                        "JobListener '" + listenerClass
                                + "' props could not be configured.", e);
                throw initException;
            }
            jobListeners[i] = listener;
        }

        // Set up any TriggerListeners
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        String[] triggerListenerNames = cfg.getPropertyGroups(QuartzInitConfiguration.MQUARTZ_TRIGGER_LISTENER_PREFIX);
        TriggerListener[] triggerListeners = new TriggerListener[triggerListenerNames.length];
        for (int i = 0; i < triggerListenerNames.length; i++) {
            Properties lp = cfg.getPropertyGroup(QuartzInitConfiguration.MQUARTZ_TRIGGER_LISTENER_PREFIX + "."
                    + triggerListenerNames[i], true);

            String listenerClass = lp.getProperty(QuartzInitConfiguration.MQUARTZ_LISTENER_CLASS, null);

            if (listenerClass == null) {
                initException = new SchedulerException(
                        "TriggerListener class not specified for listener '"
                                + triggerListenerNames[i] + "'");
                throw initException;
            }
            TriggerListener listener = null;
            try {
                listener = (TriggerListener)
                       loadHelper.loadClass(listenerClass).newInstance();
            } catch (Exception e) {
                initException = new SchedulerException(
                        "TriggerListener class '" + listenerClass
                                + "' could not be instantiated.", e);
                throw initException;
            }
            try {
                Method nameSetter = null;
                try {
                    nameSetter = listener.getClass().getMethod("setName", strArg);
                }
                catch(NoSuchMethodException ignore) { /* do nothing */ }
                if(nameSetter != null) {
                    nameSetter.invoke(listener, new Object[] {triggerListenerNames[i] } );
                }
                setBeanProps(listener, lp);
            } catch (Exception e) {
                initException = new SchedulerException(
                        "TriggerListener '" + listenerClass
                                + "' props could not be configured.", e);
                throw initException;
            }
            triggerListeners[i] = listener;
        }

        boolean tpInited = false;
        boolean qsInited = false;


        // Get ThreadExecutor Properties
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        String threadExecutorClass = cfg.getStringProperty(QuartzInitConfiguration.MQUARTZ_THREAD_EXECUTOR_CLASS);
        if (threadExecutorClass != null) {
            tProps = cfg.getPropertyGroup(QuartzInitConfiguration.MQUARTZ_THREAD_EXECUTOR, true);
            try {
                threadExecutor = (ThreadExecutor) loadHelper.loadClass(threadExecutorClass).newInstance();
                log.info("Using custom implementation for ThreadExecutor: " + threadExecutorClass);

                setBeanProps(threadExecutor, tProps);
            } catch (Exception e) {
                initException = new SchedulerException(
                        "ThreadExecutor class '" + threadExecutorClass + "' could not be instantiated.", e);
                throw initException;
            }
        } else {
            log.info("Using default implementation for ThreadExecutor");
            threadExecutor = new DefaultThreadExecutor();
        }



        // Fire everything up
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        try {
                
    
            JobRunShellFactory jrsf; // Create correct run-shell factory...

            jrsf = new StdJobRunShellFactory();
    
            QuartzSchedulerResources rsrcs = new QuartzSchedulerResources();
            rsrcs.setName(schedName);
            rsrcs.setThreadName(threadName);
            rsrcs.setJobRunShellFactory(jrsf);
            rsrcs.setMakeSchedulerThreadDaemon(makeSchedulerThreadDaemon);
            rsrcs.setThreadsInheritInitializersClassLoadContext(threadsInheritInitalizersClassLoader);
            rsrcs.setBatchTimeWindow(batchTimeWindow);
            rsrcs.setMaxBatchSize(maxBatchSize);
            rsrcs.setInterruptJobsOnShutdown(interruptJobsOnShutdown);
            rsrcs.setInterruptJobsOnShutdownWithWait(interruptJobsOnShutdownWithWait);
    
            SchedulerDetailsSetter.setDetails(tp, schedName, null);

            rsrcs.setThreadExecutor(threadExecutor);
            threadExecutor.initialize();

            rsrcs.setThreadPool(tp);
            if(tp instanceof SimpleThreadPool) {
                if(threadsInheritInitalizersClassLoader)
                    ((SimpleThreadPool)tp).setThreadsInheritContextClassLoaderOfInitializingThread(threadsInheritInitalizersClassLoader);
            }
            tp.initialize();
            tpInited = true;
    
            rsrcs.setJobStore(jobStore);
    
            // add plugins
            for (int i = 0; i < plugins.length; i++) {
                rsrcs.addSchedulerPlugin(plugins[i]);
            }
    
            qs = new QuartzScheduler(rsrcs, idleWaitTime);
            qsInited = true;
    
            // Create Scheduler ref...
            Scheduler scheduler = instantiate(rsrcs, qs);
    
            // set job factory if specified
            if(jobFactory != null) {
                qs.setJobFactory(jobFactory);
            }
    
            // Initialize plugins now that we have a Scheduler instance.
            for (int i = 0; i < plugins.length; i++) {
                plugins[i].initialize(pluginNames[i], scheduler, loadHelper);
            }
    
            // add listeners
            for (int i = 0; i < jobListeners.length; i++) {
                qs.getListenerManager().addJobListener(jobListeners[i], EverythingMatcher.allJobs());
            }
            for (int i = 0; i < triggerListeners.length; i++) {
                qs.getListenerManager().addTriggerListener(triggerListeners[i], EverythingMatcher.allTriggers());
            }
    
            // set scheduler context data...
            for(Object key: schedCtxtProps.keySet()) {
                String val = schedCtxtProps.getProperty((String) key);    
                scheduler.getContext().put((String)key, val);
            }
    
            // fire up job store, and runshell factory
    
            //jobStore.setInstanceId(schedInstId);
            jobStore.setInstanceName(schedName);
            jobStore.setThreadPoolSize(tp.getPoolSize());
            jobStore.initialize(loadHelper, qs.getSchedulerSignaler());

            jrsf.initialize(scheduler);
            
            qs.initialize();
    
            getLog().info(
                    "Quartz scheduler '" + scheduler.getSchedulerName()
                            + "' initialized from " + propSrc);
    
            getLog().info("Quartz scheduler version: " + qs.getVersion());
    
            // prevents the repository from being garbage collected
            qs.addNoGCObject(schedRep);
    
            schedRep.bind(scheduler);
            return scheduler;
        }
        catch(SchedulerException e) {
            shutdownFromInstantiateException(tp, qs, tpInited, qsInited);
            throw e;
        }
        catch(RuntimeException re) {
            shutdownFromInstantiateException(tp, qs, tpInited, qsInited);
            throw re;
        }
        catch(Error re) {
            shutdownFromInstantiateException(tp, qs, tpInited, qsInited);
            throw re;
        }
    }


    private void shutdownFromInstantiateException(ThreadPool tp, QuartzScheduler qs, boolean tpInited, boolean qsInited) {
        try {
            if(qsInited)
                qs.shutdown(false);
            else if(tpInited)
                tp.shutdown(false);
        } catch (Exception e) {
            getLog().error("Got another exception while shutting down after instantiation exception", e);
        }
    }

    protected Scheduler instantiate(QuartzSchedulerResources rsrcs, QuartzScheduler qs) {

        Scheduler scheduler = new StdScheduler(qs);
        return scheduler;
    }


    private void setBeanProps(Object obj, Properties props)
        throws NoSuchMethodException, IllegalAccessException,
            java.lang.reflect.InvocationTargetException,
            IntrospectionException, SchedulerConfigException {
        props.remove("class");

        BeanInfo bi = Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propDescs = bi.getPropertyDescriptors();
        PropertiesParser pp = new PropertiesParser(props);

        java.util.Enumeration<Object> keys = props.keys();
        while (keys.hasMoreElements()) {
            String name = (String) keys.nextElement();
            String c = name.substring(0, 1).toUpperCase(Locale.US);
            String methName = "set" + c + name.substring(1);

            Method setMeth = getSetMethod(methName, propDescs);

            try {
                if (setMeth == null) {
                    throw new NoSuchMethodException(
                            "No setter for property '" + name + "'");
                }

                Class<?>[] params = setMeth.getParameterTypes();
                if (params.length != 1) {
                    throw new NoSuchMethodException(
                        "No 1-argument setter for property '" + name + "'");
                }
                
                // does the property value reference another property's value? If so, swap to look at its value
                PropertiesParser refProps = pp;
                String refName = pp.getStringProperty(name);
                if(refName != null && refName.startsWith("$@")) {
                    refName =  refName.substring(2);
                    refProps = cfg;
                }
                else
                    refName = name;
                
                if (params[0].equals(int.class)) {
                    setMeth.invoke(obj, new Object[]{Integer.valueOf(refProps.getIntProperty(refName))});
                } else if (params[0].equals(long.class)) {
                    setMeth.invoke(obj, new Object[]{Long.valueOf(refProps.getLongProperty(refName))});
                } else if (params[0].equals(float.class)) {
                    setMeth.invoke(obj, new Object[]{Float.valueOf(refProps.getFloatProperty(refName))});
                } else if (params[0].equals(double.class)) {
                    setMeth.invoke(obj, new Object[]{Double.valueOf(refProps.getDoubleProperty(refName))});
                } else if (params[0].equals(boolean.class)) {
                    setMeth.invoke(obj, new Object[]{Boolean.valueOf(refProps.getBooleanProperty(refName))});
                } else if (params[0].equals(String.class)) {
                    setMeth.invoke(obj, new Object[]{refProps.getStringProperty(refName)});
                } else {
                    throw new NoSuchMethodException(
                            "No primitive-type setter for property '" + name
                                    + "'");
                }
            } catch (NumberFormatException nfe) {
                throw new SchedulerConfigException("Could not parse property '"
                        + name + "' into correct data type: " + nfe.toString());
            }
        }
    }

    private Method getSetMethod(String name,
            PropertyDescriptor[] props) {
        for (int i = 0; i < props.length; i++) {
            Method wMeth = props[i].getWriteMethod();

            if (wMeth != null && wMeth.getName().equals(name)) {
                return wMeth;
            }
        }

        return null;
    }

    private Class<?> loadClass(String className) throws ClassNotFoundException, SchedulerConfigException {

        try {
            ClassLoader cl = findClassloader();
            if(cl != null)
                return cl.loadClass(className);
            throw new SchedulerConfigException("Unable to find a class loader on the current thread or class.");
        } catch (ClassNotFoundException e) {
            if(getClass().getClassLoader() != null)
                return getClass().getClassLoader().loadClass(className);
            throw e;
        }
    }

    private ClassLoader findClassloader() {
        // work-around set context loader for windows-service started jvms (QUARTZ-748)
        if(Thread.currentThread().getContextClassLoader() == null && getClass().getClassLoader() != null) {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        }
        return Thread.currentThread().getContextClassLoader();
    }

    private String getSchedulerName() {
        return cfg.getStringProperty(QuartzInitConfiguration.MQUARTZ_INSTANCE_NAME,
                "QuartzScheduler");
    }

    /**
     * <p>
     * Returns a handle to the Scheduler produced by this factory.
     * </p>
     *
     * <p>
     * If one of the <code>initializeFromProp</code> methods has not be previously
     * called, then the default (no-arg) <code>initializeFromProp()</code> method
     * will be called by this method.
     * </p>
     */
    @Override
    public Scheduler getScheduler() throws SchedulerException {
        if (cfg == null) {
            initialize();
        }

        SchedulerRepository schedRep = SchedulerRepository.getInstance();

        Scheduler sched = schedRep.lookup(getSchedulerName());

        if (sched != null) {
            if (sched.isShutdown()) {
                schedRep.remove(getSchedulerName());
            } else {
                return sched;
            }
        }

        sched = instantiate();

        return sched;
    }

    /**
     * <p>
     * Returns a handle to the default Scheduler, creating it if it does not
     * yet exist.
     * </p>
     *
     * @see #initialize()
     */
    public static Scheduler getDefaultScheduler() throws SchedulerException {
        StdSchedulerFactory fact = new StdSchedulerFactory();

        return fact.getScheduler();
    }

    /**
     * <p>
     * Returns a handle to the Scheduler with the given name, if it exists (if
     * it has already been instantiated).
     * </p>
     */
    @Override
    public Scheduler getScheduler(String schedName) {
        return SchedulerRepository.getInstance().lookup(schedName);
    }

    /**
     * <p>
     * Returns a handle to all known Schedulers (made by any
     * StdSchedulerFactory instance.).
     * </p>
     */
    @Override
    public Collection<Scheduler> getAllSchedulers() {
        return SchedulerRepository.getInstance().lookupAll();
    }
}
