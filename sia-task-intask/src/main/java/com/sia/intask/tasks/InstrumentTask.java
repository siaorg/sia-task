package com.sia.intask.tasks;

import com.sia.core.constant.Constant;
import com.sia.core.helper.StringHelper;
import com.sia.hunter.annotation.OnlineTask;
import com.sia.hunter.helper.JSONHelper;
import com.sia.intask.email.EmailService;
import com.sia.intask.service.InstrumentTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.regex.Pattern.compile;

/**
 * 工具Task
 * @author jinghuali
 */
@RestController
public class InstrumentTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentTask.class);

    @Autowired
    private InstrumentTaskService instrumentTaskService;

    @Autowired
    private EmailService emailService;

    /**
     * 任务调度日报统计定时任务
     */
    @OnlineTask(description = "任务调度日报统计定时任务")
    @RequestMapping(path = "/dailyStatistics", method = {RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    @CrossOrigin(methods = {RequestMethod.POST}, origins = "*")
    @ResponseBody
    public String runDailyStatistics(@RequestBody String request) throws ParseException, IOException {
        // 从数据库得到当前时间nowTime和24小时之前的时间nowTimeBefore
        String nowTime =  instrumentTaskService.getDbTime();
        Calendar now = Calendar.getInstance();
        Date nowTimeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(nowTime);
        now.setTime(nowTimeDate);
        now.add(Calendar.DAY_OF_MONTH, -1);
        String nowTimeBefore = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now.getTime());

        // 获得job总数和task总数
        Map<String, Integer> allCount = instrumentTaskService.getAllCount();

        // 获取新增job信息和新增task信息
        List<Map<String, Object>> jobInfo = instrumentTaskService.getNewJobInfo(nowTimeBefore, nowTime);
        List<Map<String, Object>> taskInfo = instrumentTaskService.getNewTaskInfo(nowTimeBefore, nowTime);

        // 调度器调度信息
        List<Map<String, Object>> schedulerCall = instrumentTaskService.selectCallExpFinFromJobLogForReport(nowTimeBefore, nowTime);

        // 项目信息
        List<Map<String, Object>> projectInfo = instrumentTaskService.selectProjectInfo(nowTimeBefore, nowTime);

        // 异常job信息-名称
        List<Map<String, Object>> expJobs = instrumentTaskService.selectExpJobs(nowTimeBefore, nowTime);

        // 激活的job数量
        Map<String, Object> groupActiveJobNum = instrumentTaskService.getActiveJobNum();

        // 异常task信息
        List<Map<String, Object>> expTaskInfo = instrumentTaskService.getExpTaskInfo(nowTimeBefore, nowTime);

        InputStream in = new DefaultResourceLoader().getResource("classpath:index.html").getInputStream();
        String htmlStr = inputStream2String(in);

        htmlStr = getJobTaskTotalNum(htmlStr, groupActiveJobNum, allCount);

        htmlStr = getNewJobInfo(htmlStr, jobInfo);
        htmlStr = getNewTaskInfo(htmlStr, taskInfo);

        htmlStr = getJobTaskCallInfo(htmlStr, schedulerCall);
        htmlStr = getSchedulerCallInfo(htmlStr, schedulerCall);

        htmlStr = getExpTaskInfo(htmlStr, expTaskInfo);

        htmlStr = getProjectInfo(htmlStr, projectInfo, expJobs);

        emailService.sendEmail(null, htmlStr, "SIA-TASK日报");

        Map<String, Object> info = new HashMap<String, Object>(4);
        info.put("status", "success");
        info.put("result", "Execution success");
        info.put("desc", "email send success");

        return JSONHelper.toString(info);
    }

    /**
     * 运行统计数据 定时任务
     *
     * @return
     */
    @OnlineTask(description = "清除日志数据定时任务")
    @RequestMapping(path = "/cleanLog", method = {RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    @CrossOrigin(methods = {RequestMethod.POST}, origins = "*")
    @ResponseBody
    public String cleanLog(@RequestBody String request) {
        Map<String, Object> info = new HashMap<String, Object>(4);
        try {
            Pattern compile = compile("[0-9]*");
            Matcher matcher = compile.matcher(request);
            if (matcher.matches()) {
                LOGGER.info(Constant.LOG_PREFIX + "运行统计数据定时任务");
                int deleteJobLog = instrumentTaskService.deleteJobLogByDate(-Integer.valueOf(request));
                int deleteTaskLog = instrumentTaskService.deleteTaskLogByDate(-Integer.valueOf(request));
                info.put("status", "success");
                info.put("result", deleteJobLog + " ：" + deleteTaskLog);
                info.put("desc", "清除日志数据定时任务，最低按天计算");
                return com.sia.core.helper.JSONHelper.toString(info);
            }
            info.put("status", "fail");
            info.put("result", "入参格式不正确");
            info.put("desc", "清除日志数据定时任务，最低按天计算");
        } catch (Exception e) {
            LOGGER.error(Constant.LOG_PREFIX + "cleanLog异常",e);

        }
        return com.sia.core.helper.JSONHelper.toString(info);
    }

    /**
     * 首页数据统计，定时任务
     */
    @OnlineTask(description = "首页数据统计定时任务")
    @RequestMapping(path = "/runPortalStatistics", method = {RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    @CrossOrigin(methods = {RequestMethod.POST}, origins = "*")
    @ResponseBody
    public String runPortalStatistics(@RequestBody String request) throws ParseException {
        String nowTime =  instrumentTaskService.getDbTime();
        String lastTime = instrumentTaskService.getNearestTime();
        if (StringHelper.isEmpty(lastTime)){
            Calendar now = Calendar.getInstance();
            Date nowTimeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(nowTime);
            now.setTime(nowTimeDate);
            now.add(Calendar.MINUTE, -30);
            String nowTimeBefore = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now.getTime());
            lastTime = nowTimeBefore;
        }

        boolean flag = true;
        List<Map<String, Object>> lm = instrumentTaskService.selectCallExpFinFromJobLogForPage(lastTime, nowTime);
        for(Map<String, Object> item : lm){
            Map<String, Object> param = new HashMap<>(item);
            param.put("last_time", nowTime);
            int result = instrumentTaskService.insertPortalStatistics(param);
            if(result != 1){
                LOGGER.error(Constant.LOG_PREFIX + "insert runPortalStatistics error,scheduler: " + param);
                flag = false;
            }
        }

        Map<String, Object> info = new HashMap<String, Object>(4);
        if (flag){
            info.put("status", "success");
            info.put("result", "Execution success");
            info.put("desc", "insert DB success");
        } else {
            info.put("status", "fail");
            info.put("result", "Execution failed");
            info.put("desc", "insert DB error");
        }

        return com.sia.core.helper.JSONHelper.toString(info);
    }


    /*@OnlineTask(description = "修改JOB下线漂移开关")
    @RequestMapping(path = "/enableJobTransfer", method = {RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    @CrossOrigin(methods = {RequestMethod.POST}, origins = "*")
    @ResponseBody
    public String enableJobTransfer(@RequestBody String request) {


        Map<String, Object> info = new HashMap<String, Object>(4);
        try {
            Pattern compile = compile("^[0-1]$");
            Matcher matcher = compile.matcher(request);
            if (matcher.matches()) {
                LOGGER.info(Constant.LOG_PREFIX + "修改JOB下线漂移开关");
                Constants.ENABLE_JOB_TRANSFER = request.equals("1") ? true : false;
                info.put("status", "success");
                info.put("result", Constants.ENABLE_JOB_TRANSFER);
                info.put("desc", "修改JOB下线漂移开关");
                return com.sia.core.helper.JSONHelper.toString(info);
            }
            info.put("status", "fail");
            info.put("result", Constants.ENABLE_JOB_TRANSFER);
            info.put("desc", "入参格式不正确");
        } catch (Exception e) {

        }
        return com.sia.core.helper.JSONHelper.toString(info);
    }*/

    private String getJobTaskTotalNum(String htmlStr, Map<String, Object> groupActiveJobNum, Map<String, Integer> allCount){
        htmlStr = htmlStr.replaceAll("activeJobNum", String.valueOf(groupActiveJobNum.get("activeJobNum")));
        htmlStr = htmlStr.replaceAll("jobTotalNum", String.valueOf(allCount.get("jobAllCount")));
        htmlStr = htmlStr.replaceAll("currentTaskTotalNum", String.valueOf(allCount.get("taskAllCount")));
        return htmlStr;
    }

    private String getNewJobInfo(String htmlStr, List<Map<String, Object>> jobInfo){
        htmlStr = htmlStr.replaceAll("newAddJobNum", String.valueOf(jobInfo.size()));
        String newHtml = "<tr style=\"border: 1px solid #DFDEDE;color:#191818;height:35px;line-height:35px;\">\n" +
                "          <td style=\"padding:6px 10px; line-height: 150%;width:33.3%; color:#000000;\">name-1</td>\n" +
                "          <td style=\"padding:6px 10px; line-height: 150%;width:33.3%; color:#000000;\">name-2</td>\n" +
                "          <td style=\"padding:6px 10px; line-height: 150%;width:33.3%; color:#000000;\">name-3</td>\n" +
                "        </tr>\n";
        StringBuilder newJobHtmlAll = new StringBuilder();
        for (Map<String, Object> job : jobInfo){
            String newJobHtmlTmp = newHtml.replaceAll("name-1", (String)job.get("job_key"));
            newJobHtmlTmp = newJobHtmlTmp.replaceAll("name-2", (String)job.get("job_group"));
            newJobHtmlTmp = newJobHtmlTmp.replaceAll("name-3", (String)job.get("job_alarm_email"));
            newJobHtmlAll.append(newJobHtmlTmp);
        }
        htmlStr = htmlStr.replaceAll("NEW-JOB-INFO", newJobHtmlAll.toString());
        return htmlStr;
    }

    private String getNewTaskInfo(String htmlStr, List<Map<String, Object>> taskInfo){
        htmlStr = htmlStr.replaceAll("newAddTaskNum", String.valueOf(taskInfo.size()));
        String newHtml = "<tr style=\"border: 1px solid #DFDEDE;color:#191818;height:35px;line-height:35px;\">\n" +
                "          <td style=\"padding:6px 10px; line-height: 150%;width:33.3%; color:#000000;\">name-1</td>\n" +
                "          <td style=\"padding:6px 10px; line-height: 150%;width:33.3%; color:#000000;\">name-2</td>\n" +
                "          <td style=\"padding:6px 10px; line-height: 150%;width:33.3%; color:#000000;\">name-3</td>\n" +
                "        </tr>\n";
        StringBuilder newTaskHtmlAll = new StringBuilder();
        for (Map<String, Object> task : taskInfo){
            String newTaskHtmlTmp = newHtml.replaceAll("name-1", (String) task.get("task_key"));
            newTaskHtmlTmp = newTaskHtmlTmp.replaceAll("name-2", (String) task.get("task_app_name"));
            newTaskHtmlTmp = newTaskHtmlTmp.replaceAll("name-3", (String) task.get("task_group_name"));
            newTaskHtmlAll.append(newTaskHtmlTmp);
        }
        htmlStr = htmlStr.replaceAll("NEW-TASK-INFO", newTaskHtmlAll.toString());
        return htmlStr;
    }

    private String getJobTaskCallInfo(String htmlStr, List<Map<String, Object>> schedulerCall){
        htmlStr = htmlStr.replaceAll("jobDispatchNum", String.valueOf(schedulerCall.stream().map(s -> (Long)s.get("job_call_count")).collect(Collectors.summarizingLong(value -> value)).getSum()));
        htmlStr = htmlStr.replaceAll("taskDispatchNum", String.valueOf(schedulerCall.stream().map(s -> (Long)s.get("task_call_count")).collect(Collectors.summarizingLong(value -> value)).getSum()));
        htmlStr = htmlStr.replaceAll("jobErrorNum", String.valueOf(schedulerCall.stream().map(s -> (Long)s.get("job_exception_count")).collect(Collectors.summarizingLong(value -> value)).getSum()));
        htmlStr = htmlStr.replaceAll("taskErrorNum", String.valueOf(schedulerCall.stream().map(s -> (Long)s.get("task_exception_count")).collect(Collectors.summarizingLong(value -> value)).getSum()));
        return htmlStr;
    }

    private String getSchedulerCallInfo(String htmlStr, List<Map<String, Object>> schedulerCall){
        String schedulerHtml = "<tr style=\"border: 1px solid #DFDEDE;color:#191818;height:35px;line-height:35px;\">\n" +
                "          <td style=\"padding:6px 10px; line-height: 150%;width:32%; color:#000000;\">dispatchName</td>\n" +
                "          <td style=\"padding:6px 10px; line-height: 150%;width:17%; color:#000000;\">scheduJobNum</td>\n" +
                "          <td style=\"padding:6px 10px; line-height: 150%;width:17%; color:#000000;\">scheduTaskNum</td>\n" +
                "          <td style=\"padding:6px 10px; line-height: 150%;width:17%; color:#000000;\">scheduJobErrorNum</td>\n" +
                "          <td style=\"padding:6px 10px; line-height: 150%;width:17%; color:#000000;\">scheduTaskErrorNum</td>\n" +
                "        </tr>\n";
        StringBuilder schedulerHtmlAll = new StringBuilder();
        for (Map<String, Object> scheduler : schedulerCall){
            String schedulerTmp = schedulerHtml.replaceAll("dispatchName", (String) scheduler.get("scheduler"));
            schedulerTmp = schedulerTmp.replaceAll("scheduJobNum", String.valueOf(scheduler.get("job_call_count")));
            schedulerTmp = schedulerTmp.replaceAll("scheduTaskNum", String.valueOf(scheduler.get("task_call_count")));
            schedulerTmp = schedulerTmp.replaceAll("scheduJobErrorNum", String.valueOf(scheduler.get("job_exception_count")));
            schedulerTmp = schedulerTmp.replaceAll("scheduTaskErrorNum", String.valueOf(scheduler.get("task_exception_count")));
            schedulerHtmlAll.append(schedulerTmp);
        }
        htmlStr = htmlStr.replaceAll("SCHEDULER-ALL-INFO", schedulerHtmlAll.toString());
        return htmlStr;
    }

    private String getExpTaskInfo(String htmlStr, List<Map<String, Object>> expTaskInfo){
        String expTaskInfoHtml = "<tr style=\"border: 1px solid #DFDEDE;color:#191818;height:35px;line-height:35px;\">\n" +
                "          <td style=\"padding:6px 10px; line-height: 150%;width:17%; color:#000000;\">expTaskKey</td>\n" +
                "          <td style=\"padding:6px 10px; line-height: 150%;width:17%; color:#000000;\">expTaskJobKey</td>\n" +
                "          <td style=\"padding:6px 10px; line-height: 150%;width:17%; color:#000000;\">expTaskCount</td>\n" +
                "        </tr>\n";
        StringBuilder expTaskInfoHtmlAll = new StringBuilder();
        for (Map<String, Object> expTask : expTaskInfo){
            String expTaskTmp = expTaskInfoHtml.replaceAll("expTaskKey", (String)expTask.get("task_key"));
            expTaskTmp = expTaskTmp.replaceAll("expTaskJobKey", (String)expTask.get("job_key"));
            expTaskTmp = expTaskTmp.replaceAll("expTaskCount", String.valueOf(expTask.get("exp_task_count")));
            expTaskInfoHtmlAll.append(expTaskTmp);
        }
        htmlStr = htmlStr.replaceAll("EXP-TASK-INFO", expTaskInfoHtmlAll.toString());
        return htmlStr;
    }

    private String getProjectInfo(String htmlStr, List<Map<String, Object>> projectInfo, List<Map<String, Object>> expJobs){
        String proHtml = "<tr style=\"border: 1px solid #DFDEDE;color:#191818;height:35px;line-height:35px;\">\n" +
                "          <td style=\"padding:6px 10px; line-height: 150%;width:32%; color:#000000;\">projectName</td>\n" +
                "          <td style=\"padding:6px 10px; line-height: 150%;width:17%; color:#000000;\">proJobNum</td>\n" +
                "          <td style=\"padding:6px 10px; line-height: 150%;width:17%; color:#000000;\">proTaskNum</td>\n" +
                "          <td style=\"padding:6px 10px; line-height: 150%;width:17%; color:#000000;\">proDispatchNum</td>\n" +
                "          <td style=\"padding:6px 10px; line-height: 150%;width:17%; color:#000000;\">\n" +
                "            EXCEPTION-JOB-LIST" +
                "          </td>\n" +
                "        </tr>\n";
        String proInnerHtml = "<i style=\"display:block;font-style:normal; color:#CC0033;\">errorJobList</i>\n";
        StringBuilder proHtmlAll = new StringBuilder();
        for (Map<String, Object> project : projectInfo){
            StringBuilder proInnerHtmlAll = new StringBuilder();
            List<Object> errorJobKeys = expJobs.stream().filter(group->project.get("ggroup").equals(group.get("ggroup"))).map(g->g.get("job_key")).collect(Collectors.toList());
            if (errorJobKeys != null){
                for (Object jobKey : errorJobKeys){
                    proInnerHtmlAll.append(proInnerHtml.replaceAll("errorJobList", (String) jobKey));
                }
            }

            String proHtmlTmp = proHtml.replaceAll("projectName", (String) project.get("ggroup"));
            proHtmlTmp = proHtmlTmp.replaceAll("proJobNum", String.valueOf(project.get("job_num")));
            proHtmlTmp = proHtmlTmp.replaceAll("proTaskNum", String.valueOf(project.get("task_num")));
            proHtmlTmp = proHtmlTmp.replaceAll("proDispatchNum", String.valueOf(project.get("call_num")));
            proHtmlTmp = proHtmlTmp.replaceAll("EXCEPTION-JOB-LIST", proInnerHtmlAll.toString());

            proHtmlAll.append(proHtmlTmp);
        }
        htmlStr = htmlStr.replaceAll("PROJECT-ALL-INFO", proHtmlAll.toString());
        return htmlStr;
    }

    private String inputStream2String(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = in.readLine()) != null){
            buffer.append(line);
        }
        return buffer.toString();
    }

}
