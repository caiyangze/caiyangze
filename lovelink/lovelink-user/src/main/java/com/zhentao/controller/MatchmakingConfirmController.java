package com.zhentao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhentao.dto.MatchmakingConfirmDTO;
import com.zhentao.dto.WalletDTO;
import com.zhentao.pojo.TbMatchmaker;
import com.zhentao.pojo.TbMatchmakerOrder;
import com.zhentao.pojo.TbMatchmakingRequest;
import com.zhentao.pojo.TbUser;
import com.zhentao.service.TbMatchmakingRequestService;
import com.zhentao.service.TbMatchmakerService;
import com.zhentao.service.TbMatchmakerOrderService;
import com.zhentao.service.TbUserService;
import com.zhentao.service.WalletService;
import com.zhentao.utils.JwtService;
import com.zhentao.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 牵线申请确认控制器（目标用户确认）
 * @author zhentao
 */
@RestController
@RequestMapping("/user/matchmaking/confirm")
@CrossOrigin("*")
public class MatchmakingConfirmController {
    
    @Autowired
    private TbMatchmakingRequestService matchmakingRequestService;

    @Autowired
    private TbUserService userService;

    @Autowired
    private TbMatchmakerService matchmakerService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private TbMatchmakerOrderService matchmakerOrderService;

    /**
     * 获取待确认的牵线申请列表
     * @param pageNum 页码
     * @param pageSize 页大小
     * @param token 用户token
     * @return 待确认申请列表
     */
    @GetMapping("/pending")
    public Result getPendingRequests(
            @RequestParam(defaultValue = "1") Integer pageNum,
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
        
        // 3. 查询待确认的申请（状态为1，且目标用户是当前用户）
        LambdaQueryWrapper<TbMatchmakingRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbMatchmakingRequest::getTargetUserId, userId)
               .eq(TbMatchmakingRequest::getRequestStatus, 1) // 红娘已接受，待目标用户确认
               .orderByDesc(TbMatchmakingRequest::getUpdatedAt);
        
        // 4. 分页查询
        Page<TbMatchmakingRequest> page = new Page<>(pageNum, pageSize);
        IPage<TbMatchmakingRequest> result = matchmakingRequestService.page(page, wrapper);

        // 5. 为每个申请补充申请用户信息，返回包含用户详细信息的Map结构
        List<Map<String, Object>> requestList = result.getRecords().stream().map(request -> {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("request", request);

            // 获取申请用户信息
            TbUser applicantUser = userService.getById(request.getUserId());
            if (applicantUser != null) {
                applicantUser.setPassword(null); // 清除敏感信息
                requestMap.put("applicantUser", applicantUser);
            }

            // 获取红娘信息（如果有）
            if (request.getMatchmakerId() != null) {
                System.out.println("DEBUG: 开始查询红娘信息，matchmakerId: " + request.getMatchmakerId());
                try {
                    // 第一步：通过matchmakerId查询红娘表
                    TbMatchmaker matchmaker = matchmakerService.getById(request.getMatchmakerId());
                    System.out.println("DEBUG: 查询红娘表结果: " + (matchmaker != null ? matchmaker.toString() : "null"));

                    if (matchmaker != null) {
                        requestMap.put("matchmaker", matchmaker);

                        // 第二步：通过红娘表中的userId查询用户表
                        if (matchmaker.getUserId() != null) {
                            System.out.println("DEBUG: 开始查询红娘对应的用户信息，userId: " + matchmaker.getUserId());
                            TbUser matchmakerUser = userService.getById(matchmaker.getUserId());
                            System.out.println("DEBUG: 查询用户表结果: " + (matchmakerUser != null ? matchmakerUser.getNickname() : "null"));

                            if (matchmakerUser != null) {
                                matchmakerUser.setPassword(null);
                                requestMap.put("matchmakerUser", matchmakerUser);
                            }
                        } else {
                            System.out.println("DEBUG: 红娘表中的userId为空");
                        }
                    } else {
                        System.out.println("DEBUG: 红娘表中未找到matchmakerId=" + request.getMatchmakerId() + "的记录");
                    }
                } catch (Exception e) {
                    System.out.println("DEBUG: 查询红娘信息时发生异常: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("DEBUG: matchmakerId为空，跳过红娘信息查询");
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
     * 获取我的牵线申请历史（作为目标用户）
     * @param pageNum 页码
     * @param pageSize 页大小
     * @param status 状态筛选（可选）
     * @param token 用户token
     * @return 申请历史列表
     */
    @GetMapping("/history")
    public Result getConfirmHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
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
        
        // 3. 查询作为目标用户的所有申请（只显示红娘已处理的申请，状态 >= 1）
        LambdaQueryWrapper<TbMatchmakingRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbMatchmakingRequest::getTargetUserId, userId)
               .ge(TbMatchmakingRequest::getRequestStatus, 1); // 只显示红娘已处理的申请（状态 >= 1：已接受或已拒绝）

        if (status != null) {
            wrapper.eq(TbMatchmakingRequest::getRequestStatus, status);
        }

        wrapper.orderByDesc(TbMatchmakingRequest::getUpdatedAt);
        
        // 4. 分页查询
        Page<TbMatchmakingRequest> page = new Page<>(pageNum, pageSize);
        IPage<TbMatchmakingRequest> result = matchmakingRequestService.page(page, wrapper);

        // 5. 为每个申请补充申请用户信息，返回包含用户详细信息的Map结构
        List<Map<String, Object>> requestList = result.getRecords().stream().map(request -> {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("request", request);

            // 获取申请用户信息
            TbUser applicantUser = userService.getById(request.getUserId());
            if (applicantUser != null) {
                applicantUser.setPassword(null); // 清除敏感信息
                requestMap.put("applicantUser", applicantUser);
            }

            // 获取红娘信息（如果有）
            if (request.getMatchmakerId() != null) {
                try {
                    // 第一步：通过matchmakerId查询红娘表
                    TbMatchmaker matchmaker = matchmakerService.getById(request.getMatchmakerId());

                    if (matchmaker != null) {
                        requestMap.put("matchmaker", matchmaker);

                        // 第二步：通过红娘表中的userId查询用户表
                        if (matchmaker.getUserId() != null) {
                            TbUser matchmakerUser = userService.getById(matchmaker.getUserId());

                            if (matchmakerUser != null) {
                                matchmakerUser.setPassword(null);
                                requestMap.put("matchmakerUser", matchmakerUser);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("DEBUG: 查询红娘信息时发生异常: " + e.getMessage());
                    e.printStackTrace();
                }
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
     * 测试WebSocket连接
     * @return 测试结果
     */
    @GetMapping("/test/websocket")
    public Result testWebSocket() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "WebSocket测试端点");
        result.put("wsUrl", "ws://localhost:9001/ws/chat");
        result.put("sockjsUrl", "ws://localhost:9001/ws/chat-sockjs");
        result.put("timestamp", System.currentTimeMillis());
        return Result.OK(result);
    }

    /**
     * 测试红娘数据查询
     * @return 红娘数据
     */
    @GetMapping("/test/matchmaker")
    public Result testMatchmaker() {
        try {
            // 查询所有红娘数据
            List<TbMatchmaker> matchmakers = matchmakerService.list();
            System.out.println("DEBUG: 数据库中的红娘数量: " + matchmakers.size());

            Map<String, Object> result = new HashMap<>();
            result.put("matchmakerCount", matchmakers.size());
            result.put("matchmakers", matchmakers);

            // 如果有红娘数据，测试查询用户信息
            if (!matchmakers.isEmpty()) {
                TbMatchmaker firstMatchmaker = matchmakers.get(0);
                System.out.println("DEBUG: 第一个红娘信息: " + firstMatchmaker);

                if (firstMatchmaker.getUserId() != null) {
                    TbUser user = userService.getById(firstMatchmaker.getUserId());
                    System.out.println("DEBUG: 红娘对应的用户信息: " + (user != null ? user.getNickname() : "null"));
                    result.put("firstMatchmakerUser", user);
                }
            }

            return Result.OK(result);
        } catch (Exception e) {
            System.out.println("DEBUG: 测试红娘数据时发生异常: " + e.getMessage());
            e.printStackTrace();
            return Result.ERROR("测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 确认或拒绝牵线申请
     * @param dto 确认信息
     * @param token 用户token
     * @return 处理结果
     */
    @PostMapping("/handle")
    public Result handleConfirmation(@RequestBody MatchmakingConfirmDTO dto, @RequestHeader("token") String token) {
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
        
        // 3. 查询申请记录
        TbMatchmakingRequest request = matchmakingRequestService.getById(dto.getRequestId());
        if (request == null) {
            return Result.ERROR("申请记录不存在");
        }
        
        // 4. 验证是否是目标用户
        if (!request.getTargetUserId().equals(userId)) {
            return Result.ERROR("您无权处理此申请");
        }
        
        // 5. 验证申请状态
        if (request.getRequestStatus() != 1) {
            return Result.ERROR("申请状态不正确，无法处理");
        }
        
        // 6. 更新申请状态
        if (dto.getAction() == 1) {
            // 同意申请
            request.setRequestStatus(4); // 双方都同意，可以安排约会
        } else {
            // 拒绝申请
            request.setRequestStatus(3); // 目标用户已拒绝
            if (dto.getRejectReason() != null && !dto.getRejectReason().trim().isEmpty()) {
                request.setRejectReason(dto.getRejectReason());
            }

            // 目标用户拒绝时，需要进行退款
            try {
                // 查找对应的订单进行退款
                LambdaQueryWrapper<TbMatchmakerOrder> orderWrapper = new LambdaQueryWrapper<>();
                orderWrapper.eq(TbMatchmakerOrder::getUserId, request.getUserId())
                           .eq(TbMatchmakerOrder::getMatchmakerId, request.getMatchmakerId())
                           .eq(TbMatchmakerOrder::getOrderStatus, 1) // 已支付状态
                           .orderByDesc(TbMatchmakerOrder::getCreatedAt)
                           .last("LIMIT 1"); // 获取最新的一条记录

                TbMatchmakerOrder order = matchmakerOrderService.getOne(orderWrapper);
                if (order != null) {
                    // 进行退款
                    BigDecimal refundAmount = order.getPayAmount() != null ? order.getPayAmount() : order.getAmount();
                    if (refundAmount != null) {
                        WalletDTO refundDTO = new WalletDTO();
                        refundDTO.setUserId(request.getUserId());
                        refundDTO.setCoinAmount(refundAmount.intValue());
                        refundDTO.setTransactionDesc("目标用户拒绝申请退款 - " + order.getOrderNo());
                        refundDTO.setRelatedId("USER_REJECT_REFUND_" + order.getOrderNo());

                        Result refundResult = walletService.recharge(refundDTO);
                        if (refundResult.getCode() == 200) {
                            // 更新订单状态为已退款
                            order.setOrderStatus(3); // 3-已退款
                            order.setUpdatedAt(new Date());
                            matchmakerOrderService.updateById(order);

                            System.out.println("目标用户拒绝申请，退款成功: " + refundAmount + "虚拟币");
                        } else {
                            System.err.println("目标用户拒绝申请，退款失败: " + refundResult.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("目标用户拒绝申请，退款处理异常: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        request.setUpdatedAt(new Date());
        
        // 7. 保存更新
        boolean success = matchmakingRequestService.updateById(request);
        if (success) {
            if (dto.getAction() == 1) {
                return Result.OK("您已同意该牵线申请");
            } else {
                return Result.OK("您已拒绝该牵线申请，服务费用已自动退还给申请用户");
            }
        } else {
            return Result.ERROR("处理失败，请重试");
        }
    }
    
    /**
     * 获取待确认申请数量
     * @param token 用户token
     * @return 待确认数量
     */
    @GetMapping("/count")
    public Result getPendingCount(@RequestHeader("token") String token) {
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
        
        // 3. 统计待确认申请数量
        LambdaQueryWrapper<TbMatchmakingRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbMatchmakingRequest::getTargetUserId, userId)
               .eq(TbMatchmakingRequest::getRequestStatus, 1);
        
        long count = matchmakingRequestService.count(wrapper);
        
        return Result.OK(count);
    }
}
