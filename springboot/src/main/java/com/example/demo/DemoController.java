package com.example.demo;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {
    Result result = new Result("pong");

    @RequestMapping("/ping")
    public Result hello(){
        return result;
    }
}

class Result{
    String Message;

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public Result(String message) {
        Message = message;
    }
}