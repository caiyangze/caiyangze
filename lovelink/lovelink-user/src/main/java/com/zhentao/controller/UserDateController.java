package com.zhentao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhentao.pojo.TbDateArrangement;
import com.zhentao.pojo.TbUser;
import com.zhentao.service.DateNotificationService;
import com.zhentao.service.TbDateArrangementService;
import com.zhentao.service.TbUserService;
import com.zhentao.utils.JwtService;
import com.zhentao.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户约会控制器
 * @author zhentao
 */
@RestController
@RequestMapping("/user/date")
@Validated
@Slf4j
public class UserDateController {

    @Autowired
    private TbDateArrangementService dateArrangementService;

    @Autowired
    private TbUserService userService;

    @Autowired
    private DateNotificationService dateNotificationService;

    /**
     * 获取我的约会安排列表
     */
    @GetMapping("/my-arrangements")
    public Result getMyDateArrangements(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false) Integer arrangementStatus,
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
            // 3. 构建查询条件
            QueryWrapper<TbDateArrangement> queryWrapper = new QueryWrapper<>();
            queryWrapper.and(wrapper -> 
                wrapper.eq("user_id", userId).or().eq("target_user_id", userId)
            );
            
            if (arrangementStatus != null) {
                queryWrapper.eq("arrangement_status", arrangementStatus);
            }
            
            queryWrapper.orderByDesc("created_at");

            // 4. 分页查询
            Page<TbDateArrangement> page = new Page<>(pageNum, pageSize);
            Page<TbDateArrangement> resultPage = dateArrangementService.page(page, queryWrapper);

            // 5. 构建返回结果
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("total", resultPage.getTotal());
            resultMap.put("pages", resultPage.getPages());
            resultMap.put("current", resultPage.getCurrent());
            resultMap.put("size", resultPage.getSize());
            resultMap.put("data", resultPage.getRecords());

            return Result.OK(resultMap.get("data"));

        } catch (Exception e) {
            log.error("获取约会安排列表失败", e);
            return Result.ERROR("获取约会安排列表失败");
        }
    }

    /**
     * 获取约会安排详情
     */
    @GetMapping("/arrangement/{arrangementId}")
    public Result getDateArrangementDetail(@PathVariable Long arrangementId,
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
            // 3. 查询约会安排
            TbDateArrangement arrangement = dateArrangementService.getById(arrangementId);
            if (arrangement == null) {
                return Result.ERROR("约会安排不存在");
            }

            // 4. 验证用户权限
            if (!arrangement.getUserId().equals(userId) && !arrangement.getTargetUserId().equals(userId)) {
                return Result.ERROR("您无权查看此约会安排");
            }

            return Result.OK(arrangement);

        } catch (Exception e) {
            log.error("获取约会安排详情失败", e);
            return Result.ERROR("获取约会安排详情失败");
        }
    }

    /**
     * 确认约会安排
     */
    @PostMapping("/confirm/{arrangementId}")
    public Result confirmDateArrangement(@PathVariable Long arrangementId,
                                       @RequestParam Integer confirm, // 1-确认，2-拒绝
                                       @RequestParam(required = false) String rejectReason, // 拒绝原因（可选）
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

        // 3. 验证确认状态参数
        if (confirm == null || (confirm != 1 && confirm != 2)) {
            return Result.ERROR("确认状态参数错误");
        }

        try {
            // 4. 查询约会安排
            TbDateArrangement arrangement = dateArrangementService.getById(arrangementId);
            if (arrangement == null) {
                return Result.ERROR("约会安排不存在");
            }

            // 5. 验证用户是否有权限确认此约会
            boolean isUserA = arrangement.getUserId().equals(userId);
            boolean isUserB = arrangement.getTargetUserId().equals(userId);
            
            if (!isUserA && !isUserB) {
                return Result.ERROR("您无权确认此约会安排");
            }

            // 6. 检查是否已经确认过
            if (isUserA && arrangement.getUserAConfirm() != 0) {
                return Result.ERROR("您已经确认过此约会安排");
            }
            if (isUserB && arrangement.getUserBConfirm() != 0) {
                return Result.ERROR("您已经确认过此约会安排");
            }

            // 7. 更新确认状态
            if (isUserA) {
                arrangement.setUserAConfirm(confirm);
            } else {
                arrangement.setUserBConfirm(confirm);
            }
            arrangement.setUpdatedAt(new Date());

            // 8. 检查约会最终状态
            boolean bothConfirmed = false;
            boolean anyRejected = false;
            
            if (arrangement.getUserAConfirm() != 0 && arrangement.getUserBConfirm() != 0) {
                // 双方都已回应
                if (arrangement.getUserAConfirm() == 1 && arrangement.getUserBConfirm() == 1) {
                    // 双方都确认
                    arrangement.setArrangementStatus(1); // 已确认
                    bothConfirmed = true;
                } else {
                    // 有人拒绝
                    arrangement.setArrangementStatus(2); // 已取消
                    anyRejected = true;

                    // 设置拒绝原因
                    if (confirm == 2 && rejectReason != null && !rejectReason.trim().isEmpty()) {
                        arrangement.setCancelReason("用户拒绝：" + rejectReason.trim());
                    } else {
                        arrangement.setCancelReason("用户拒绝约会安排");
                    }
                }
            }

            // 9. 保存更新
            boolean success = dateArrangementService.updateById(arrangement);
            if (!success) {
                return Result.ERROR("确认失败，请重试");
            }

            // 10. 发送通知
            try {
                Long otherUserId = isUserA ? arrangement.getTargetUserId() : arrangement.getUserId();
                
                if (bothConfirmed) {
                    // 双方都确认，发送最终确定通知
                    dateNotificationService.sendDateFinalizedNotification(
                        arrangement.getUserId(), 
                        arrangement.getTargetUserId(), 
                        arrangement
                    );
                } else if (anyRejected) {
                    // 有人拒绝，发送取消通知
                    dateNotificationService.sendDateCancelledNotification(
                        arrangement.getUserId(), 
                        arrangement.getTargetUserId(), 
                        "用户拒绝约会安排"
                    );
                } else {
                    // 只有一方确认，通知另一方
                    dateNotificationService.sendDateConfirmationNotification(
                        otherUserId, 
                        userId, 
                        confirm == 1, 
                        arrangement
                    );
                }
                
            } catch (Exception e) {
                log.error("发送约会确认通知失败", e);
            }

            // 11. 返回结果
            String message;
            if (confirm == 1) {
                if (bothConfirmed) {
                    message = "约会确认成功！双方都已确认，约会正式确定";
                } else {
                    message = "确认成功，等待对方确认";
                }
            } else {
                message = "已拒绝约会安排";
            }

            return Result.OK(message);

        } catch (Exception e) {
            log.error("确认约会安排失败", e);
            return Result.ERROR("确认约会安排失败");
        }
    }

    /**
     * 获取约会统计信息
     */
    @GetMapping("/statistics")
    public Result getDateStatistics(@RequestHeader("token") String token) {
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
            // 3. 统计各状态的约会数量
            QueryWrapper<TbDateArrangement> baseWrapper = new QueryWrapper<>();
            baseWrapper.and(wrapper -> 
                wrapper.eq("user_id", userId).or().eq("target_user_id", userId)
            );

            // 待确认数量
            QueryWrapper<TbDateArrangement> pendingWrapper = baseWrapper.clone();
            pendingWrapper.eq("arrangement_status", 0);
            long pendingCount = dateArrangementService.count(pendingWrapper);

            // 已确认数量
            QueryWrapper<TbDateArrangement> confirmedWrapper = baseWrapper.clone();
            confirmedWrapper.eq("arrangement_status", 1);
            long confirmedCount = dateArrangementService.count(confirmedWrapper);

            // 已完成数量
            QueryWrapper<TbDateArrangement> completedWrapper = baseWrapper.clone();
            completedWrapper.eq("arrangement_status", 3);
            long completedCount = dateArrangementService.count(completedWrapper);

            // 总数量
            long totalCount = dateArrangementService.count(baseWrapper);

            // 4. 构建返回结果
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("pendingCount", pendingCount);
            statistics.put("confirmedCount", confirmedCount);
            statistics.put("completedCount", completedCount);
            statistics.put("totalCount", totalCount);

            return Result.OK(statistics);

        } catch (Exception e) {
            log.error("获取约会统计信息失败", e);
            return Result.ERROR("获取约会统计信息失败");
        }
    }
}
