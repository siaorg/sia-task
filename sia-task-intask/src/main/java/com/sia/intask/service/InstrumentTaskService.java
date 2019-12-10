package com.sia.intask.service;

import com.sia.core.curator.Curator4Scheduler;
import com.sia.core.entity.BasicJob;
import com.sia.core.helper.DateFormatHelper;
import com.sia.core.helper.StringHelper;
import com.sia.core.mapper.BasicJobMapper;
import com.sia.core.mapper.JobLogMapper;
import com.sia.core.mapper.PortalStatisticsMapper;
import com.sia.core.mapper.TaskLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author jinghuali
 */
@Service
public class InstrumentTaskService {

    @Autowired
    private BasicJobMapper basicJobMapper;

    @Autowired
    private Curator4Scheduler curator4Scheduler;

    @Autowired
    private PortalStatisticsMapper portalStatisticsMapper;

    @Autowired
    private TaskLogMapper taskLogMapper;

    @Autowired
    private JobLogMapper jobLogMapper;

    /**
     * 从skyworld_portal_stat表中获取最近的时间
     * @return
     */
    public String getNearestTime(){
        Date nearTime = portalStatisticsMapper.getNearestTime();
        return nearTime == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nearTime);
    }

    /**
     * 从jobLog中获取时间区间JOB执行数量、成功数量、失败数量
     * 日报数据统计
     * @param startTime
     * @param endTime
     * @return
     */
    public List<Map<String, Object>> selectCallExpFinFromJobLogForReport(String startTime, String endTime){
        Map<String, Object> param = new HashMap<>(4);
        param.put("startTime", startTime);
        param.put("endTime", endTime);

        return portalStatisticsMapper.selectCallExpFinFromJobLogForReport(param);
    }

    /**
     * 插入首页统计数据
     * @param param
     * @return
     */
    public int insertPortalStatistics(Map<String, Object> param){
        return portalStatisticsMapper.insertPortalStatistics(param);
    }

    /**
     * 获取数据库当前时间
     * @return
     */
    public String getDbTime(){
        return portalStatisticsMapper.getDbTime();
    }

    public Map<String, Integer> getAllCount(){
        return portalStatisticsMapper.getAllCount();
    }

    public List<Map<String, Object>> getNewJobInfo(String startTime,  String endTime){
        Map<String, Object> param = new HashMap<>(4);
        param.put("startTime", startTime);
        param.put("endTime", endTime);

        return portalStatisticsMapper.getNewJobInfo(param);
    }

    public List<Map<String, Object>> getNewTaskInfo(String startTime,  String endTime){
        Map<String, Object> param = new HashMap<>(4);
        param.put("startTime", startTime);
        param.put("endTime", endTime);

        return portalStatisticsMapper.getNewTaskInfo(param);
    }

    public List<Map<String, Object>> selectProjectInfo(String startTime,  String endTime){
        Map<String, Object> param = new HashMap<>(4);
        param.put("startTime", startTime);
        param.put("endTime", endTime);

        return portalStatisticsMapper.selectProjectInfo(param);
    }

    public List<Map<String, Object>> selectExpJobs(String startTime,  String endTime){
        Map<String, Object> param = new HashMap<>(4);
        param.put("startTime", startTime);
        param.put("endTime", endTime);

        return portalStatisticsMapper.selectExpJobs(param);
    }

    public Map<String, Object> getActiveJobNum(){
        Map<String, Object> param = new HashMap<>(4);
        param.put("roleNames", null);
        List<BasicJob> jobs = basicJobMapper.selectByJobKeyAndJobGroupList(param);
        updateJobs(jobs);

        Map<String, Object> res = new HashMap<>(16);
        List<String> groups = jobs.stream().map(job -> job.getJobGroup()).distinct().collect(Collectors.toList());
        groups.forEach(group->{
            long count = jobs.stream().filter(job -> group.equals(job.getJobGroup())).filter(job -> job.getJobDesc() != null).count();
            res.put(group, count);
        });

        long activeJobNum = jobs.stream().filter(basicJob -> basicJob.getJobDesc() != null).count();
        res.put("activeJobNum", activeJobNum);
        return res;
    }

    private void updateJobs(List<BasicJob> jobs){
        for (BasicJob basicJob : jobs) {
            List<String> jobScheduler = curator4Scheduler.getJobScheduler(basicJob.getJobGroup(), basicJob.getJobKey());
            basicJob.setTriggerInstance(StringHelper.join(jobScheduler, ","));
            String jobStatus = curator4Scheduler.getJobStatus(basicJob.getJobGroup(), basicJob.getJobKey());
            basicJob.setJobDesc(jobStatus);
        }
    }

    public List<Map<String, Object>> getExpTaskInfo(String startTime, String endTime){
        Map<String, Object> param = new HashMap<>(4);
        param.put("startTime", startTime);
        param.put("endTime", endTime);

        return portalStatisticsMapper.getExpTaskInfo(param);
    }


    /**
     * 删除日志 向前N天
     * @param displacement 天数的位移数
     */
    public int deleteJobLogByDate(int displacement) {
        Date date = new Date();
        Map<String, String> taskMap = new HashMap<>(2);
        taskMap.put("create_time", DateFormatHelper.getFormatByDay(date,displacement));
        return jobLogMapper.deleteJobLogByDate(taskMap);
    }

    /**
     * 删除日志 向前N天
     *
     * @param displacement 天数的位移数
     */
    public int deleteTaskLogByDate(int displacement) {
        Date date = new Date();
        Map<String, String> taskMap = new HashMap<>(2);
        taskMap.put("create_time", DateFormatHelper.getFormatByDay(date, displacement));
        return taskLogMapper.deleteTaskLogByDate(taskMap);
    }
    /**
     * 从jobLog中获取时间区间JOB执行数量、成功数量、失败数量
     * 首页数据统计定时任务
     * @param startTime
     * @param endTime
     * @return
     */
    public List<Map<String, Object>> selectCallExpFinFromJobLogForPage(String startTime,  String endTime){
        Map<String, Object> param = new HashMap<>(4);
        param.put("startTime", startTime);
        param.put("endTime", endTime);

        return portalStatisticsMapper.selectCallExpFinFromJobLogForPage(param);
    }

}
