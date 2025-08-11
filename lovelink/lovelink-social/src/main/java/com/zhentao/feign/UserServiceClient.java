package com.zhentao.feign;

import com.zhentao.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 用户服务 Feign 客户端
 * @author zhentao
 * @date 2025/8/3
 */
@FeignClient(name = "lovelink-user", url = "${feign.client.lovelink-user.url:http://localhost:9001}")
public interface UserServiceClient {
    
    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/user/{userId}")
    Result getUserById(@PathVariable("userId") Long userId);
    
    /**
     * 批量获取用户信息
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    @PostMapping("/user/batch")
    Result getUsersByIds(@RequestBody List<Long> userIds);
    
    /**
     * 获取用户详细信息（包含profile）
     * @param userId 用户ID
     * @return 用户详细信息
     */
    @GetMapping("/user/{userId}/detail")
    Result getUserDetail(@PathVariable("userId") Long userId);
    
    /**
     * 获取用户标签
     * @param userId 用户ID
     * @return 用户标签列表
     */
    @GetMapping("/user/{userId}/tags")
    Result getUserTags(@PathVariable("userId") Long userId);
}
