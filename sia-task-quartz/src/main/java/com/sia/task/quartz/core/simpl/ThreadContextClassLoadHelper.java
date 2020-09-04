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

package com.sia.task.quartz.core.simpl;

import com.sia.task.quartz.ClassLoadHelper;

import java.io.InputStream;
import java.net.URL;

/**
 * A <code>ClassLoadHelper</code> that uses either the current thread's
 * context class loader (<code>Thread.currentThread().getContextClassLoader().loadClass( .. )</code>).
 * 
 * @see ClassLoadHelper
 * @see InitThreadContextClassLoadHelper
 * @see SimpleClassLoadHelper
 * @see CascadingClassLoadHelper
 * @see LoadingLoaderClassLoadHelper
 * 
 * @author jhouse
 * @author pl47ypus
 */
public class ThreadContextClassLoadHelper implements ClassLoadHelper {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Called to give the ClassLoadHelper a chance to initializeFromProp itself,
     * including the opportunity to "steal" the class loader off of the calling
     * thread, which is the thread that is initializing Quartz.
     */
    public void initialize() {
    }

    /**
     * Return the class with the given name.
     */
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return getClassLoader().loadClass(name);
    }

    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> loadClass(String name, Class<T> clazz)
            throws ClassNotFoundException {
        return (Class<? extends T>) loadClass(name);
    }
    
    /**
     * Finds a resource with a given name. This method returns null if no
     * resource with this name is found.
     * @param name name of the desired resource
     * @return a java.net.URL object
     */
    public URL getResource(String name) {
        return getClassLoader().getResource(name);
    }

    /**
     * Finds a resource with a given name. This method returns null if no
     * resource with this name is found.
     * @param name name of the desired resource
     * @return a java.io.InputStream object
     */
    public InputStream getResourceAsStream(String name) {
        return getClassLoader().getResourceAsStream(name);
    }

    /**
     * Enable sharing of the class-loader with 3rd party.
     *
     * @return the class-loader user be the helper.
     */
    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

}
