package com.sia.task.executor.spring.controller;

import com.sia.task.core.http.SiaHttpResponse;
import com.sia.task.hunter.annotation.OnlineTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/task")
@Slf4j
public class TaskController {

    @RequestMapping("/task-0s")
    @OnlineTask(description = "在线任务示例, success 0s", enableSerial = false)
    @ResponseBody
    public String runTask() {
        log.info("run task ---> do something...");
        return SiaHttpResponse.success("res: 在线任务示例, success 0s");
    }
}
