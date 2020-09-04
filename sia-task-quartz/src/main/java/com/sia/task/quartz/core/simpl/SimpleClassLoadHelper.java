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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * A <code>ClassLoadHelper</code> that simply calls <code>Class.forName(..)</code>.
 * 
 * @see ClassLoadHelper
 * @see ThreadContextClassLoadHelper
 * @see CascadingClassLoadHelper
 * @see LoadingLoaderClassLoadHelper
 *
 *
 * @author @see Quartz
 * @data 2019-05-11 11:17
 * @version V1.0.0
 **/
public class SimpleClassLoadHelper implements ClassLoadHelper {

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
        return Class.forName(name);
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
        // To follow the same behavior of Class.forName(...) I had to play
        // dirty (Supported by Sun, IBM & BEA JVMs)
        try {
            // Get a reference to this class' class-loader
            ClassLoader cl = this.getClass().getClassLoader();
            // Create a method instance representing the protected
            // getCallerClassLoader method of class ClassLoader
            Method mthd = ClassLoader.class.getDeclaredMethod(
                    "getCallerClassLoader", new Class<?>[0]);
            // Make the method accessible.
            AccessibleObject.setAccessible(new AccessibleObject[] {mthd}, true);
            // Try to get the caller's class-loader
            return (ClassLoader)mthd.invoke(cl, new Object[0]);
        } catch (Throwable all) {
            // Use this class' class-loader
            return this.getClass().getClassLoader();
        }
    }

}
