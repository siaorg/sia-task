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

package com.sia.task.admin.exception;

import com.sia.task.admin.vo.SiaResponseBody;
import lombok.Data;

/**
 * AmsException
 *
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-09-03 20:54
 * @see
 **/
public @Data
class SiaTaskException extends RuntimeException {

    private String responseBody;

    private static final SiaResponseBody.ResponseCodeEnum EXCEPTION = SiaResponseBody.ResponseCodeEnum.ERROR;

    /**
     * Constructs a new runtime EXCEPTION with {@code null} as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public SiaTaskException() {
        responseBody = SiaResponseBody.failure(EXCEPTION);
    }

    /**
     * Constructs a new runtime EXCEPTION with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public SiaTaskException(String message) {

        responseBody = SiaResponseBody.failure(EXCEPTION.getCode(), message);
    }

    /**
     * Constructs a new runtime EXCEPTION with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this runtime EXCEPTION's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @since 1.4
     */
    public SiaTaskException(String message, Throwable cause) {
        super(message, cause);
        responseBody = SiaResponseBody.failure(EXCEPTION.getCode(), message, cause.getMessage());
    }

    /**
     * Constructs a new runtime EXCEPTION with the specified cause and a
     * detail message of <tt>(cause==null ? null : cause.toString())</tt>
     * (which typically contains the class and detail message of
     * <tt>cause</tt>).  This constructor is useful for runtime exceptions
     * that are little more than wrappers for other throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     * @since 1.4
     */
    public SiaTaskException(Throwable cause) {
        super(cause);
        responseBody = SiaResponseBody.failure(EXCEPTION.getCode(), EXCEPTION.getMessage(), cause.getMessage());
    }

}
