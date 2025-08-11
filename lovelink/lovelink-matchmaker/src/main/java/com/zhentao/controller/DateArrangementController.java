package com.zhentao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhentao.dto.DateArrangementDTO;
import com.zhentao.pojo.TbDateArrangement;
import com.zhentao.pojo.TbMatchmakingRequest;
import com.zhentao.pojo.TbUser;
import com.zhentao.service.DateNotificationService;
import com.zhentao.service.TbDateArrangementService;
import com.zhentao.service.TbMatchmakingRequestService;
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
import java.util.stream.Collectors;

/**
 * 约会安排控制器
 * @author zhentao
 */
@RestController
@RequestMapping("/matchmaker/date")
@Validated
@Slf4j
public class DateArrangementController {

    @Autowired
    private TbDateArrangementService dateArrangementService;

    @Autowired
    private TbMatchmakingRequestService matchmakingRequestService;

    @Autowired
    private TbUserService userService;

    @Autowired
    private DateNotificationService dateNotificationService;

    /**
     * 测试接口
     */
    @GetMapping("/test")
    public Result test() {
        return Result.OK("约会安排服务正常运行");
    }

    /**
     * 获取可安排约会的申请列表（状态为4的申请）
     */
    @GetMapping("/available-requests")
    public Result getAvailableRequests(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize,
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

        // 3. 验证用户是否是红娘
        TbUser user = userService.getById(userId);
        if (user == null || user.getUserRole() != 2) {
            return Result.ERROR("您不是红娘，无权查看此信息");
        }

        // 4. 查询状态为4（双方都同意）且还没有安排约会的申请
        LambdaQueryWrapper<TbMatchmakingRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbMatchmakingRequest::getRequestStatus, 4); // 双方都同意
        // 可以根据需要添加红娘ID筛选
        // wrapper.eq(TbMatchmakingRequest::getMatchmakerId, matchmakerId);
        wrapper.orderByDesc(TbMatchmakingRequest::getUpdatedAt);

        Page<TbMatchmakingRequest> page = new Page<>(pageNum, pageSize);
        IPage<TbMatchmakingRequest> result = matchmakingRequestService.page(page, wrapper);

        // 5. 为每个申请补充用户信息，并检查是否已安排约会
        List<Map<String, Object>> requestList = result.getRecords().stream().map(request -> {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("request", request);

            // 获取申请用户信息
            TbUser applicantUser = userService.getById(request.getUserId());
            if (applicantUser != null) {
                applicantUser.setPassword(null);
                requestMap.put("applicantUser", applicantUser);
            }

            // 获取目标用户信息
            TbUser targetUser = userService.getById(request.getTargetUserId());
            if (targetUser != null) {
                targetUser.setPassword(null);
                requestMap.put("targetUser", targetUser);
            }

            // 检查是否已安排约会
            LambdaQueryWrapper<TbDateArrangement> dateWrapper = new LambdaQueryWrapper<>();
            dateWrapper.eq(TbDateArrangement::getRequestId, request.getRequestId());
            TbDateArrangement existingDate = dateArrangementService.getOne(dateWrapper);
            requestMap.put("hasDateArrangement", existingDate != null);
            if (existingDate != null) {
                requestMap.put("dateArrangement", existingDate);
            }

            return requestMap;
        }).collect(Collectors.toList());

        // 6. 构建分页结果
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("records", requestList);
        pageResult.put("total", result.getTotal());
        pageResult.put("current", result.getCurrent());
        pageResult.put("size", result.getSize());
        pageResult.put("pages", result.getPages());

        return Result.OK(pageResult);
    }

    /**
     * 安排约会
     */
    @PostMapping("/arrange")
    public Result arrangeDate(@RequestBody @Validated DateArrangementDTO dto, @RequestHeader("token") String token) {
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

        // 3. 验证用户是否是红娘
        TbUser user = userService.getById(userId);
        if (user == null || user.getUserRole() != 2) {
            return Result.ERROR("您不是红娘，无权安排约会");
        }

        // 4. 查询申请记录
        TbMatchmakingRequest request = matchmakingRequestService.getById(dto.getRequestId());
        if (request == null) {
            return Result.ERROR("申请记录不存在");
        }

        // 5. 验证申请状态
        if (request.getRequestStatus() != 4) {
            return Result.ERROR("申请状态不正确，无法安排约会");
        }

        // 6. 检查是否已经安排过约会
        LambdaQueryWrapper<TbDateArrangement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbDateArrangement::getRequestId, dto.getRequestId());
        TbDateArrangement existingDate = dateArrangementService.getOne(wrapper);
        if (existingDate != null) {
            return Result.ERROR("该申请已经安排过约会");
        }

        // 7. 创建约会安排
        TbDateArrangement dateArrangement = new TbDateArrangement();
        dateArrangement.setRequestId(dto.getRequestId());
        dateArrangement.setMatchmakerId(request.getMatchmakerId());
        dateArrangement.setUserId(request.getUserId());
        dateArrangement.setTargetUserId(request.getTargetUserId());
        dateArrangement.setDateTime(dto.getDateTime());
        dateArrangement.setDateLocation(dto.getDateLocation());
        dateArrangement.setDateType(dto.getDateType());
        dateArrangement.setDatePlan(dto.getDatePlan());
        dateArrangement.setUserAConfirm(0); // 待确认
        dateArrangement.setUserBConfirm(0); // 待确认
        dateArrangement.setArrangementStatus(0); // 待确认
        dateArrangement.setCreatedAt(new Date());
        dateArrangement.setUpdatedAt(new Date());

        // 8. 保存约会安排
        boolean success = dateArrangementService.save(dateArrangement);
        if (success) {
            // 9. 发送WebSocket通知给双方用户
            try {
                dateNotificationService.sendDateArrangementNotification(
                    request.getUserId(),
                    request.getTargetUserId(),
                    dateArrangement
                );
                log.info("约会安排通知发送成功，安排ID: {}", dateArrangement.getArrangementId());
            } catch (Exception e) {
                log.error("发送约会安排通知失败", e);
                // 通知发送失败不影响约会安排的成功
            }

            // 10. 更新申请状态（注意：不要设为已完成，应该等双方确认后再完成）
            // request.setRequestStatus(5); // 暂时不设为已完成，等双方确认

            return Result.OK("约会安排成功，已通知双方用户确认");
        } else {
            return Result.ERROR("约会安排失败，请重试");
        }
    }

    /**
     * 获取约会安排列表
     */
    @GetMapping("/list")
    public Result getDateArrangements(@RequestParam(defaultValue = "1") Integer pageNum,
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

        // 3. 验证用户是否是红娘
        TbUser user = userService.getById(userId);
        if (user == null || user.getUserRole() != 2) {
            return Result.ERROR("您不是红娘，无权查看此信息");
        }

        // 4. 查询约会安排
        LambdaQueryWrapper<TbDateArrangement> wrapper = new LambdaQueryWrapper<>();
        // 可以根据需要添加红娘ID筛选
        // wrapper.eq(TbDateArrangement::getMatchmakerId, matchmakerId);
        if (arrangementStatus != null) {
            wrapper.eq(TbDateArrangement::getArrangementStatus, arrangementStatus);
        }
        wrapper.orderByDesc(TbDateArrangement::getCreatedAt);

        Page<TbDateArrangement> page = new Page<>(pageNum, pageSize);
        IPage<TbDateArrangement> result = dateArrangementService.page(page, wrapper);

        // 5. 为每个约会安排补充用户信息
        List<Map<String, Object>> dateList = result.getRecords().stream().map(dateArrangement -> {
            Map<String, Object> dateMap = new HashMap<>();
            dateMap.put("dateArrangement", dateArrangement);

            // 获取用户A信息
            TbUser userA = userService.getById(dateArrangement.getUserId());
            if (userA != null) {
                userA.setPassword(null);
                dateMap.put("userA", userA);
            }

            // 获取用户B信息
            TbUser userB = userService.getById(dateArrangement.getTargetUserId());
            if (userB != null) {
                userB.setPassword(null);
                dateMap.put("userB", userB);
            }

            return dateMap;
        }).collect(Collectors.toList());

        // 6. 构建分页结果
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("records", dateList);
        pageResult.put("total", result.getTotal());
        pageResult.put("current", result.getCurrent());
        pageResult.put("size", result.getSize());
        pageResult.put("pages", result.getPages());

        return Result.OK(pageResult);
    }

    /**
     * 用户确认约会安排
     */
    @PostMapping("/confirm/{arrangementId}")
    public Result confirmDateArrangement(@PathVariable Long arrangementId,
                                       @RequestParam Integer confirm, // 1-确认，2-拒绝
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
                arrangement.setCancelReason("用户拒绝约会安排");
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

                // 更新牵线申请状态为已完成
                TbMatchmakingRequest request = matchmakingRequestService.getById(arrangement.getRequestId());
                if (request != null) {
                    request.setRequestStatus(5); // 已完成
                    request.setUpdatedAt(new Date());
                    matchmakingRequestService.updateById(request);
                }

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
    }
}
