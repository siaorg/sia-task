package com.sia.task.admin.service;/*-
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

//package com.sia.task.admin.service;
//
//
//import com.sia.task.admin.timer.SchedulingModel;
//import com.sia.task.admin.vo.TriggerTimesUtil;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//
//public class SchedulingModelTest {
//
//    static String localhost = "192.168.0.1";
//
//    static String[] cronsAll = new String[]{
//            "0 0 9 * * ?",
//            "0 /10 * * * ?",
//            "0 /20 * * * ?",
//            "0 /30 * * * ?",
//            "0 0 9 * * ?",
//            "0 0 14 ? * MON,THU",
//            "0 0 14 ? * MON,THU",
//            "0 0 8 * * ?",
//            "0 30 09 ? * MON,THU",
//            "0 0 12 1 * ? *",
//            "0 0 12 1 * ? *",
//            "0 0 16 * * ?",
//            "0 0 3 * * ? *",
//            "0 0 12 1 * ? *",
//            "0 0 0 1,15 * ? *",
//            "0 0 3 * * ? *",
//            "0 0/20 * * * ?",
//            "0 0 8 * * ? *",
//            "0 0 23 * * ? *",
//            "0 0 23 * * ? *",
//            "0 0 22 * * ? *",
//            "0 0 * * * ? *",
//            "0 5 0 * * ?",
//            "0 0 0 ? * MON",
//            "0 0 23 * * ?",
//            "0 0 */1 * * ?",
//            "0 0 10 * * ?",
//            "0 0 14 * * ?",
//            "0 0 18 * * ?",
//            "0 30 5 * * ?",
//            "0 0 4 * * ?"
//    };
//
//    static String[] crons = new String[]{
//            "0/20 * * * * ?", "0/30 25 * * * ?", "0/30 0/10 * * * ?", "0/30 0/15 * * * ?", "0/30 * * * * ?"
//    };
//
//    static String[] crons1 = new String[]{
//            "0 /10 * * * ?"
//    };
//
//    static String[] crons2 = new String[]{
//            "0/5 * * * * ?", "0/6 * * * * ?", "0/7 * * * * ?"
//    };
//
//    static String[] crons4 = new String[]{
//            "0 0 9 * * ?", "0/5 * * * * ?", "0/6 * * * * ?", "0/7 * * * * ?", "0/8 * * * * ?", "0/9 * * * * ?"
//    };
//
//
//    public static void computerTimers(String[] crons) throws ParseException {
//        String cron = "0 0 14 ? * MON,5";
//
//        String[] cronsv = new String[]{
//                "0/10 0-5 * ? * MON,5"
//        };
//
//        SchedulingModel modelV2 = SchedulingModel.build(localhost);
//        for (int i = 0; i < cronsv.length; i++) {
//            modelV2.computerTimers(cronsv[i]);
//        }
//
//        System.out.println(modelV2);
//    }
//
//
//    public static void main(String[] args) throws ParseException {
//        computerTimers(crons1);
//        //main1();
//    }
//
//    public static void main1() throws ParseException {
//        //String cron = "* * 15,16,17 * * ?";
//        String cron = "0 0 14 ? * MON,THU";
////        List<Date> countByDay = TriggerTimesUtil.getCountByDay(cron);
////        System.out.println("day----------------");
////        System.out.println(countByDay.size());
////        for (Date date : countByDay) {
////            //Calendar calendar = Calendar.getInstance();
////           // calendar.setTime(date);
////           // int i = calendar.get(Calendar.HOUR);
////            //System.out.println(i);
////            System.out.println(new SimpleDateFormat("yy-MM-dd HH:mm:ss").format(date));
////        }
//
//        List<Date> countByHour = TriggerTimesUtil.getCountByHour(cron);
//        System.out.println("hour----------------");
//        System.out.println(countByHour.size());
//        for (Date date : countByHour) {
//            System.out.println(new SimpleDateFormat("yy-MM-dd HH:mm:ss").format(date));
//        }
//        System.out.println("minute----------------");
//        List<Date> countByMinute = TriggerTimesUtil.getCountByMinute(cron);
//        System.out.println(countByMinute.size());
//        for (Date date : countByMinute) {
//            System.out.println(new SimpleDateFormat("yy-MM-dd HH:mm:ss").format(date));
//        }
//    }
//}
