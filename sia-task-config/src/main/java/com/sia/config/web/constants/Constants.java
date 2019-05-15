package com.sia.config.web.constants;


/**
 * Constant public variable
 * @see
 * @author maozhengwei
 * @date 2019-04-27 15:40
 * @version V1.0.0
 **/
public class Constants {

    /**
     * Task source
     */
    public static final String TASK_SOURCE_ZK = "TASK_SOURCE_ZK";
    public static final String TASK_SOURCE_UI = "TASK_SOURCE_UI";

    /**
     * Regular delimiter
     */
    public static final String REGEX_COMMA = ",";
    public static final String REGEX_COLON = ":";
    /**
     * The string composition rules of application group name,
     * application name and jobKey can only contain Numbers, letters, '_' and '-' */
    public static final String REGEX = "^[-A-Za-z0-9-_]+$";

    /**The IP checksum*/
    public static final String IP_REGEX = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                                        + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                                        + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                                        + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";

    /**
     * Log prefix mark
     */
    public static final String LOG_PREFIX = ">>>>>>>>>>";

    /**
     * Operation log prefix tag
     */
    public static final String OPERATION_LOG_PREFIX = "Operation logging<<<<<<<<<<<";
    /**
     * Local IP address： 127.0.0.1:8080
     */
    public static String LOCALHOST;

    /**
     * Administrator role name
     */
    public static final String ADMIN_ROLE = "admin";

}
