package com.sia.task.executordemo;
import com.sia.hunter.annotation.OnlineTask;
import com.sia.hunter.helper.JSONHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ExecutorController {

    @OnlineTask(description = "success,无入参",enableSerial=true)
    @RequestMapping(value = "/success-noparam", method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    @CrossOrigin(methods = { RequestMethod.POST }, origins = "*")
    @ResponseBody
    public String taskOne() {
        Map<String, String> info = new HashMap<String, String>();
        info.put("result", "success-noparam");
        info.put("status", "success");
        System.out.println("调用taskOne任务成功");

        return JSONHelper.toString(info);
    }

    @OnlineTask(description = "success,有入参",enableSerial=true)
    @RequestMapping(value = "/success-param", method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    @CrossOrigin(methods = { RequestMethod.POST }, origins = "*")
    @ResponseBody
    public String taskTwo(@RequestBody String json) {
        Map<String, String> info = new HashMap<String, String>();
        info.put("result", "success-param"+"入参是："+json);
        info.put("status", "success");
        System.out.println("调用taskTwo任务成功");

        return JSONHelper.toString(info);
    }

}
