package com.zhentao.controller;

import com.zhentao.pojo.TbDateFeedback;
import com.zhentao.service.DateFeedbackService;
import com.zhentao.utils.JwtService;
import com.zhentao.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 约会反馈控制器
 */
@RestController
@RequestMapping("/user/date/feedback")
public class DateFeedbackController {

    @Autowired
    private DateFeedbackService dateFeedbackService;

    /**
     * 提交约会反馈
     */
    @PostMapping("/submit")
    public Result submitFeedback(@RequestBody TbDateFeedback feedback,
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

        // 3. 设置反馈用户ID
        feedback.setUserId(userId);

        try {
            // 4. 提交反馈
            boolean success = dateFeedbackService.submitFeedback(feedback);
            if (success) {
                return Result.success("反馈提交成功");
            } else {
                return Result.error("反馈提交失败");
            }
        } catch (Exception e) {
            return Result.error("提交反馈时发生错误：" + e.getMessage());
        }
    }

    /**
     * 获取约会反馈详情
     */
    @GetMapping("/{arrangementId}")
    public Result getFeedback(@PathVariable Long arrangementId,
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
            // 3. 获取反馈信息
            TbDateFeedback feedback = dateFeedbackService.getFeedbackByArrangementAndUser(arrangementId, userId);
            return Result.OK(feedback);
        } catch (Exception e) {
            return Result.error("获取反馈信息失败：" + e.getMessage());
        }
    }

    /**
     * 检查是否已提交反馈
     */
    @GetMapping("/check/{arrangementId}")
    public Result checkFeedbackStatus(@PathVariable Long arrangementId,
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
            // 3. 检查反馈状态
            boolean hasSubmitted = dateFeedbackService.hasFeedbackSubmitted(arrangementId, userId);
            Map<String, Object> result = new HashMap<>();
            result.put("hasSubmitted", hasSubmitted);
            return Result.OK(result);
        } catch (Exception e) {
            return Result.error("检查反馈状态失败：" + e.getMessage());
        }
    }

    /**
     * 获取约会的所有反馈（红娘查看）
     */
    @GetMapping("/all/{arrangementId}")
    public Result getAllFeedback(@PathVariable Long arrangementId,
                                @RequestHeader("token") String token) {
        // 1. 验证token
        int verifyResult = JwtService.verifyToken(token);
        if (verifyResult != 1) {
            return Result.NO_LOGIN();
        }

        try {
            // 2. 获取所有反馈
            Map<String, Object> feedbackData = dateFeedbackService.getAllFeedbackByArrangement(arrangementId);
            return Result.OK(feedbackData);
        } catch (Exception e) {
            return Result.error("获取反馈信息失败：" + e.getMessage());
        }
    }
}
