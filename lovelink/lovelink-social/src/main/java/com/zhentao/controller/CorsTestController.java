package com.zhentao.controller;

import com.zhentao.utils.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 跨域测试控制器
 * 
 * @author zhentao
 */
@RestController
@RequestMapping("/cors-test")
@CrossOrigin(origins = "*", allowCredentials = "true")
public class CorsTestController {

    @GetMapping("/get")
    public Result testGet() {
        return Result.success("Social服务 GET 请求测试成功");
    }

    @PostMapping("/post")
    public Result testPost(@RequestBody(required = false) Object data) {
        return Result.success("Social服务 POST 请求测试成功", data);
    }

    @PutMapping("/put")
    public Result testPut(@RequestBody(required = false) Object data) {
        return Result.success("Social服务 PUT 请求测试成功", data);
    }

    @DeleteMapping("/delete")
    public Result testDelete() {
        return Result.success("Social服务 DELETE 请求测试成功");
    }

    @RequestMapping(value = "/options", method = RequestMethod.OPTIONS)
    public Result testOptions() {
        return Result.success("Social服务 OPTIONS 请求测试成功");
    }
}
