package com.zhentao.controller;

import com.zhentao.utils.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 测试控制器
 * @author zhentao
 */
@RestController
@RequestMapping("/test")
@CrossOrigin("*")
public class TestController {

    /**
     * 简单的GET测试
     * @return 测试结果
     */
    @GetMapping("/hello")
    public Result hello() {
        return Result.OK("Hello from User Service!");
    }

    /**
     * 简单的POST测试
     * @return 测试结果
     */
    @PostMapping("/hello")
    public Result helloPost(@RequestBody(required = false) Object data) {
        return Result.OK("POST request received! Data: " + data);
    }

    /**
     * 牵线申请路径测试
     * @return 测试结果
     */
    @GetMapping("/matchmaking")
    public Result matchmakingTest() {
        return Result.OK("Matchmaking test endpoint works!");
    }

    /**
     * 牵线申请POST测试
     * @return 测试结果
     */
    @PostMapping("/matchmaking")
    public Result matchmakingPostTest(@RequestBody(required = false) Object data) {
        return Result.OK("Matchmaking POST test works! Data: " + data);
    }
}
