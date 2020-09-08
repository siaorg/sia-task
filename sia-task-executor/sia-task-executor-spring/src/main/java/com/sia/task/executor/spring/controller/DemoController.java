package com.sia.task.executor.spring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class DemoController {

    @RequestMapping("test1")
    @ResponseBody
    public String demo(){
        System.out.println("do something");
        return "Hello World!";
    }
}
