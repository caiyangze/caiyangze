package com.zhentao.controller;

import com.zhentao.service.DateReminderService;
import com.zhentao.utils.JwtService;
import com.zhentao.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 约会提醒控制器
 */
@RestController
@RequestMapping("/user/date")
public class DateReminderController {

    @Autowired
    private DateReminderService dateReminderService;

    /**
     * 获取用户的约会提醒列表
     */
    @GetMapping("/reminders")
    public Result getReminders(@RequestHeader("token") String token) {
        // 1. 验证token
        int verifyResult = JwtService.verifyToken(token);
        if (verifyResult != 1) {
            return Result.NO_LOGIN();
        }

        // 2. 获取当前用户ID
        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Object userIdObj = claimsMap.get("userId");
        if (!(userIdObj instanceof Integer)) {
            return Result.NO_LOGIN();
        }
        Long userId = Long.valueOf((Integer) userIdObj);

        try {
            // 3. 获取提醒列表
            List<Map<String, Object>> reminders = dateReminderService.getUserReminders(userId);
            return Result.OK(reminders);
        } catch (Exception e) {
            return Result.error("获取提醒列表失败：" + e.getMessage());
        }
    }

    /**
     * 标记提醒为已读
     */
    @PostMapping("/reminders/{reminderId}/read")
    public Result markReminderAsRead(@PathVariable Long reminderId,
                                   @RequestHeader("token") String token) {
        // 1. 验证token
        int verifyResult = JwtService.verifyToken(token);
        if (verifyResult != 1) {
            return Result.NO_LOGIN();
        }

        // 2. 获取当前用户ID
        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Object userIdObj = claimsMap.get("userId");
        if (!(userIdObj instanceof Integer)) {
            return Result.NO_LOGIN();
        }
        Long userId = Long.valueOf((Integer) userIdObj);

        try {
            // 3. 标记为已读
            boolean success = dateReminderService.markAsRead(reminderId, userId);
            if (success) {
                return Result.success("标记成功");
            } else {
                return Result.error("标记失败");
            }
        } catch (Exception e) {
            return Result.error("标记失败：" + e.getMessage());
        }
    }

    /**
     * 标记所有提醒为已读
     */
    @PostMapping("/reminders/read-all")
    public Result markAllRemindersAsRead(@RequestHeader("token") String token) {
        // 1. 验证token
        int verifyResult = JwtService.verifyToken(token);
        if (verifyResult != 1) {
            return Result.NO_LOGIN();
        }

        // 2. 获取当前用户ID
        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Object userIdObj = claimsMap.get("userId");
        if (!(userIdObj instanceof Integer)) {
            return Result.NO_LOGIN();
        }
        Long userId = Long.valueOf((Integer) userIdObj);

        try {
            // 3. 标记所有为已读
            int count = dateReminderService.markAllAsRead(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            return Result.OK(result);
        } catch (Exception e) {
            return Result.error("标记失败：" + e.getMessage());
        }
    }

    /**
     * 获取未读提醒数量
     */
    @GetMapping("/reminders/unread-count")
    public Result getUnreadCount(@RequestHeader("token") String token) {
        // 1. 验证token
        int verifyResult = JwtService.verifyToken(token);
        if (verifyResult != 1) {
            return Result.NO_LOGIN();
        }

        // 2. 获取当前用户ID
        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Object userIdObj = claimsMap.get("userId");
        if (!(userIdObj instanceof Integer)) {
            return Result.NO_LOGIN();
        }
        Long userId = Long.valueOf((Integer) userIdObj);

        try {
            // 3. 获取未读数量
            int unreadCount = dateReminderService.getUnreadCount(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("unreadCount", unreadCount);
            return Result.OK(result);
        } catch (Exception e) {
            return Result.error("获取未读数量失败：" + e.getMessage());
        }
    }
}
