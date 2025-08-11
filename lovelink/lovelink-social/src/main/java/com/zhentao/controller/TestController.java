package com.zhentao.controller;

import com.zhentao.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 *
 * @author zhentao
 * @date 2025/7/21
 */
@RestController
@RequestMapping("/api/social/test")
public class TestController {

    @GetMapping("/hello")
    public Result hello() {
        return Result.OK("Social服务运行正常！");
    }


}
