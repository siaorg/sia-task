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

package com.sia.task.admin.vo;


import com.sia.task.quartz.job.trigger.impl.CronTriggerImpl;
import com.sia.task.quartz.utils.TriggerUtils;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TriggerTimesUtil
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/8/26 3:12 下午
 **/
public class TriggerTimesUtil {


    protected static final Map<String, Integer> monthMap = new HashMap<String, Integer>(20);

    protected static final Map<String, Integer> dayMap = new HashMap<String, Integer>(60);

    static {
        monthMap.put("JAN", 0);
        monthMap.put("FEB", 1);
        monthMap.put("MAR", 2);
        monthMap.put("APR", 3);
        monthMap.put("MAY", 4);
        monthMap.put("JUN", 5);
        monthMap.put("JUL", 6);
        monthMap.put("AUG", 7);
        monthMap.put("SEP", 8);
        monthMap.put("OCT", 9);
        monthMap.put("NOV", 10);
        monthMap.put("DEC", 11);

        dayMap.put("SUN", 1);
        dayMap.put("MON", 2);
        dayMap.put("TUE", 3);
        dayMap.put("WED", 4);
        dayMap.put("THU", 5);
        dayMap.put("FRI", 6);
        dayMap.put("SAT", 7);
    }

    public static final int HoursPointOfDay = 24;

    public static final int MinutePointOfHours = 60;

    public static final int SecondPointOfHours = 5;

    private static final int second = 0;
    private static final int minute = 1;
    private static final int hour = 2;
    private static final int day = 3;
    private static final int month = 4;
    private static final int week = 5;

    /**
     * 标识匹配符 *
     * 匹配该域所有合理的任意取值
     * eg. 如在Minutes域使用*, 即表示每分钟都会触发事件
     */
    public static final String sign = "*";

    /**
     * 标识匹配符 -
     * 匹配指定范围的合理时间取值
     * eg. 在Minutes域使用2-5: 则表示第[2，3，4，5]分钟触发
     */
    public static final String line = "-";

    /**
     * 标识匹配符 /
     * 符号前标识起始的合理时间取值, 符号后面标识依次递增的值.
     * eg. 在Minutes域使用5/15:  从第五个分钟开始，每隔15分钟
     */
    public static final String slash = "/";

    /**
     * 标识匹配符 ,
     * 匹配列出的枚举值
     * eg.在Minutes域使用3，6：则表示在第三和第六分钟进行触发
     */
    public static final String comma = ",";

    /**
     * 标识匹配符 ?
     * 只能用在DayOfMonth和DayOfWeek两个域,不支持同时指定星期几和几月参数。指定月份后只能使用？。不能使用*
     * eg. 0 0 10 20 * ? 表示每个月的第20号执行，？不管是星期几；但是不能用* 即【0 0 10 20 * *】
     */
    public static final String questionMark = "?";

    /**
     * 标识匹配符 space
     * Cron表达式是一个字符串，字符串以5或6个空格隔开，分为6或7个域，每一个域代表一个含义，Cron有如下两种语法格式：
     * 1）Seconds Minutes Hours DayOfMonth Month DayOfWeek Year
     * 2）Seconds Minutes Hours DayOfMonth Month DayOfWeek
     * corn从左到右（用空格隔开）：秒 分 小时 月份中的日期 月份 星期中的日期 年份
     * eg. 0 0 10 20 * ?
     */
    public static final String space = " ";

    /**
     * 标识匹配符 #
     * 用于确定每个月第几个星期几，只能出现在DayOfMonth域。
     * eg. 4#2，表示某月的第二个星期三。
     */
    public static final String well = "#";

    /**
     * 标识匹配符 L
     * 表示最后，只能出现在DayOfWeek和DayOfMonth域.
     * 用在day-of-month字段意思是 "这个月最后一天"；
     * 用在 day-of-week字段, 它简单意思是 "7" or "SAT"。 如果在day-of-week字段里和数字联合使用，它的意思就是 "这个月的最后一个星期几"
     * eg. 如果在DayOfWeek域使用5L,意味着在最后的一个星期四触发
     */
    public static final String L = "L";

    /**
     * 标识匹配符 W
     * W ("weekday") 只能用在day-of-month字段。用来描叙最接近指定天的工作日（周一到周五）
     * eg. 在day-of-month字段用“15W”指“最接近这个 月第15天的工作日”，
     * 即如果这个月第15天是周六，那么触发器将会在这个月第14天即周五触发；
     * 如果这个月第15天是周日，那么触发器将会在这个月第 16天即周一触发；
     */
    public static final String W = "W";

    /**
     * 截取Seconds域的表达式
     *
     * @param cronExpression
     * @return
     */
    public static String getSecondExpress(String cronExpression) {
        return cronExpression.split(space)[second];
    }

    /**
     * 截取Minters域的表达式
     *
     * @param cronExpression
     * @return
     */
    public static String getMinterExpress(String cronExpression) {
        return cronExpression.split(space)[minute];
    }

    /**
     * 截取Hours域的表达式
     *
     * @param cronExpression
     * @return
     */
    public static String getHourExpress(String cronExpression) {
        return cronExpression.split(space)[hour];
    }

    /**
     * 截取Day域的表达式
     *
     * @param cronExpression
     * @return
     */
    public static String getDayExpress(String cronExpression) {
        return cronExpression.split(space)[day];
    }

    /**
     * 截取Months域的表达式
     *
     * @param cronExpression
     * @return
     */
    public static String getMonthExpress(String cronExpression) {
        return cronExpression.split(space)[month];
    }

    /**
     * 截取DayOfWeek域的表达式
     *
     * @param cronExpression
     * @return cronExpression
     */
    public static String getDayOfWeekExpress(String cronExpression) {
        return cronExpression.split(space)[week];
    }

    /**
     * 判断当前日期是否被包含在表达式中,仅仅判断指定星期类型时。
     *
     * @param cronExpression
     * @return isContain ? true : false
     */
    public static boolean isContainCurrentDay(String cronExpression) {
        List<Integer> weekDays = new ArrayList<>();
        String dayofWeekExpress = getDayOfWeekExpress(cronExpression);
        if (sign.equals(dayofWeekExpress) || questionMark.equals(dayofWeekExpress)) {
            return true;
        } else if (dayofWeekExpress.contains(slash)) {
            String[] s = dayofWeekExpress.split(slash);
            for (int i = 0; i < Integer.valueOf(s[s.length - 1]); i++) {
                weekDays.add(i);
            }
            Calendar calendar = Calendar.getInstance();
            int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
            if (weekDays.contains(currentDay)) {
                return true;
            }
        } else if (dayofWeekExpress.contains(well)) {
            String[] split = dayofWeekExpress.split(well);
            Calendar calendar = Calendar.getInstance();
            int week = calendar.get(Calendar.WEEK_OF_MONTH);
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            if (split.length == 2 && (Integer.valueOf(split[0]) == week && Integer.valueOf(split[1]) == day - 1)) {
                return true;
            }
        } else if (dayofWeekExpress.contains(L)) {
            Calendar calendar = Calendar.getInstance();
            int week = calendar.get(Calendar.WEEK_OF_MONTH);
            int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
            if (week < 4) {
                return false;
            }
            if (currentDay == Integer.valueOf(dayofWeekExpress.charAt(0))) {
                return true;
            }
        } else if (dayofWeekExpress.contains(comma)) {
            Calendar calendar = Calendar.getInstance();
            int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
            String[] split = dayofWeekExpress.split(comma);
            for (int i = 0; i < split.length; i++) {
                String v = split[i];
                if (v.length() > 1) {
                    if (dayMap.get(v).intValue() == currentDay) {
                        return true;
                    }
                } else {
                    AtomicBoolean flag = new AtomicBoolean(false);
                    dayMap.forEach((k, kv) -> {
                        if (kv.intValue() == Integer.valueOf(v)) {
                            if (Integer.valueOf(kv) == currentDay - 1) {
                                flag.set(true);
                                return;
                            }
                        }
                    });
                    if (flag.get()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    @Deprecated
    public static List<Date> getCountByDay(String cronExpression) throws ParseException {
        CronTriggerImpl cronTrigger = new CronTriggerImpl();
        cronTrigger.setCronExpression(cronExpression);
        Calendar calendar = Calendar.getInstance();
        Date time = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return TriggerUtils.computeFireTimesBetween(cronTrigger, null, time, calendar.getTime());
    }

    @Deprecated
    public static List<Date> getCountByHour(String cronExpression) throws ParseException {
        CronTriggerImpl cronTrigger = new CronTriggerImpl();
        cronTrigger.setCronExpression(cronExpression);
        Calendar calendar = Calendar.getInstance();
        Date time = calendar.getTime();
        calendar.add(Calendar.HOUR, 1);
        return TriggerUtils.computeFireTimesBetween(cronTrigger, null, time, calendar.getTime());
    }

    @Deprecated
    public static List<Date> getCountByMinute(String cronExpression) throws ParseException {
        CronTriggerImpl cronTrigger = new CronTriggerImpl();
        cronTrigger.setCronExpression(cronExpression);
        Calendar calendar = Calendar.getInstance();
        Date time = calendar.getTime();
        calendar.add(Calendar.MINUTE, 1);
        return TriggerUtils.computeFireTimesBetween(cronTrigger, null, time, calendar.getTime());
    }

    /**
     * 返回指定时间域的触发时间单元
     *
     * @param cronMeta cron 表达式中的一个域的阈值
     * @param TimeType 时间域
     */
    public static List<Integer> getTimeUnitByCronMeta(String cronMeta, int TimeType) throws ParseException {
        List<Integer> count = new ArrayList<>();
        if (TriggerTimesUtil.sign.equals(cronMeta)) {
            for (int i = 0; i < TimeType; i++) {
                count.add(i);
            }
        } else if (cronMeta.contains(TriggerTimesUtil.line)) {
            //0-60
            String[] s = cronMeta.split(TriggerTimesUtil.line);
            for (int i = 0; i < Integer.valueOf(s[s.length - 1]); i++) {
                count.add(i);
            }
        } else if (cronMeta.contains(TriggerTimesUtil.slash)) {
            // 0/10  */10  /10
            String[] s = cronMeta.split(TriggerTimesUtil.slash);
            int start = 0;

            if (!"".equals(s[0]) && !TriggerTimesUtil.sign.equals(s[0])) {
                start = Integer.valueOf(s[0]);
            }

            int gap = Integer.valueOf(s[s.length - 1]);
            int size = TimeType / gap;
            if (TimeType % gap != 0) {
                size = size + 1;
            }
            for (int i = 0, v = start; i < size; i++) {
                if (i == 0) {
                    count.add(start);
                } else {
                    v = v + gap;
                    if (v >= TimeType) {
                        break;
                    }
                    count.add(v);
                }
            }
        } else if (cronMeta.contains(TriggerTimesUtil.comma)) {
            //0,60
            String[] s = cronMeta.split(TriggerTimesUtil.comma);
            List<String> list = Arrays.asList(s);
            list.forEach(l -> count.add(Integer.valueOf(l)));
        } else {
            // 0
            count.add(Integer.valueOf(cronMeta));
        }
        return count;
    }

    @Deprecated
    public static List<Date> getCountBySecond(String cronExpression) throws ParseException {
        CronTriggerImpl cronTrigger = buildCronTrigger(cronExpression);
        Calendar calendar = Calendar.getInstance();
        Date time = calendar.getTime();
        calendar.add(Calendar.SECOND, 1);
        return TriggerUtils.computeFireTimesBetween(cronTrigger, null, time, calendar.getTime());
    }

    /**
     * build CronTrigger
     *
     * @param cronExpression 表达式
     * @return CronTriggerImpl
     * @throws ParseException Parse Exception
     */
    private static CronTriggerImpl buildCronTrigger(String cronExpression) throws ParseException {
        CronTriggerImpl cronTrigger = new CronTriggerImpl();
        cronTrigger.setCronExpression(cronExpression);
        return cronTrigger;
    }
}
