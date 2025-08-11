package com.zhentao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhentao.dto.MatchmakingRequestHandleDTO;
import com.zhentao.dto.WalletDTO;
import com.zhentao.pojo.TbMatchmaker;
import com.zhentao.pojo.TbMatchmakerOrder;
import com.zhentao.pojo.TbMatchmakingRequest;
import com.zhentao.pojo.TbUser;
import com.zhentao.service.TbMatchmakerOrderService;
import com.zhentao.service.TbMatchmakerService;
import com.zhentao.service.TbMatchmakingRequestService;
import com.zhentao.service.TbUserService;
import com.zhentao.service.WalletService;
import com.zhentao.utils.JwtService;
import com.zhentao.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 红娘牵线申请管理控制器
 * @author zhentao
 */
@RestController
@RequestMapping("/matchmaker/requests")
@CrossOrigin("*")
public class MatchmakingRequestManageController {

    @Autowired
    private TbMatchmakingRequestService matchmakingRequestService;

    @Autowired
    private TbMatchmakerService matchmakerService;

    @Autowired
    private TbUserService userService;

    @Autowired
    private TbMatchmakerOrderService matchmakerOrderService;

    @Autowired
    private WalletService walletService;

    /**
     * 获取红娘的牵线申请列表（分页）
     * @param pageNum 页码
     * @param pageSize 页大小
     * @param status 申请状态（可选）
     * @param token 红娘token
     * @return 申请列表
     */
    @GetMapping("/list")
    public Result getRequestList(
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
        
        // 3. 验证用户是否是红娘
        TbUser user = userService.getById(userId);
        if (user == null || user.getUserRole() != 2) {
            return Result.ERROR("您不是红娘，无权查看申请");
        }
        
        // 4. 查询红娘信息
        LambdaQueryWrapper<TbMatchmaker> matchmakerWrapper = new LambdaQueryWrapper<>();
        matchmakerWrapper.eq(TbMatchmaker::getUserId, userId);
        TbMatchmaker matchmaker = matchmakerService.getOne(matchmakerWrapper);
        if (matchmaker == null) {
            return Result.ERROR("红娘信息不存在");
        }
        
        // 5. 构建查询条件 - 只显示已付费的申请（状态 >= 0）
        LambdaQueryWrapper<TbMatchmakingRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbMatchmakingRequest::getMatchmakerId, matchmaker.getMatchmakerId())
               .ge(TbMatchmakingRequest::getRequestStatus, 0) // 只显示已付费的申请（状态 >= 0）
               .orderByDesc(TbMatchmakingRequest::getCreatedAt);

        // 如果指定了状态，添加状态过滤
        if (status != null) {
            wrapper.eq(TbMatchmakingRequest::getRequestStatus, status);
        }
        
        // 6. 分页查询
        Page<TbMatchmakingRequest> page = new Page<>(pageNum, pageSize);
        IPage<TbMatchmakingRequest> result = matchmakingRequestService.page(page, wrapper);
        
        // 7. 组装返回数据，包含用户信息
        List<Map<String, Object>> requestList = result.getRecords().stream().map(request -> {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("requestId", request.getRequestId());
            requestMap.put("userId", request.getUserId());
            requestMap.put("targetUserId", request.getTargetUserId());
            requestMap.put("requestMessage", request.getRequestMessage());
            requestMap.put("requestStatus", request.getRequestStatus());
            requestMap.put("rejectReason", request.getRejectReason());
            requestMap.put("createdAt", request.getCreatedAt());
            requestMap.put("updatedAt", request.getUpdatedAt());
            
            // 获取申请用户信息
            TbUser applicantUser = userService.getById(request.getUserId());
            if (applicantUser != null) {
                applicantUser.setPassword(null); // 清除敏感信息
                requestMap.put("applicantUser", applicantUser);
            }
            
            // 获取目标用户信息
            TbUser targetUser = userService.getById(request.getTargetUserId());
            if (targetUser != null) {
                targetUser.setPassword(null); // 清除敏感信息
                requestMap.put("targetUser", targetUser);
            }
            
            return requestMap;
        }).collect(Collectors.toList());
        
        // 8. 构建分页结果
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("records", requestList);
        pageResult.put("total", result.getTotal());
        pageResult.put("current", result.getCurrent());
        pageResult.put("size", result.getSize());
        pageResult.put("pages", result.getPages());
        
        return Result.OK(pageResult);
    }

    /**
     * 获取待处理的申请数量
     * @param token 红娘token
     * @return 待处理申请数量
     */
    @GetMapping("/pending-count")
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
        
        // 3. 查询红娘信息
        LambdaQueryWrapper<TbMatchmaker> matchmakerWrapper = new LambdaQueryWrapper<>();
        matchmakerWrapper.eq(TbMatchmaker::getUserId, userId);
        TbMatchmaker matchmaker = matchmakerService.getOne(matchmakerWrapper);
        if (matchmaker == null) {
            return Result.ERROR("红娘信息不存在");
        }
        
        // 4. 查询待处理申请数量（只统计已付费的申请）
        LambdaQueryWrapper<TbMatchmakingRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbMatchmakingRequest::getMatchmakerId, matchmaker.getMatchmakerId())
               .eq(TbMatchmakingRequest::getRequestStatus, 0); // 0-待处理（已付费）
        
        long count = matchmakingRequestService.count(wrapper);
        
        return Result.OK(count);
    }

    /**
     * 处理牵线申请（接受或拒绝）
     * @param dto 处理信息
     * @param token 红娘token
     * @return 处理结果
     */
    @PostMapping("/handle")
    @Transactional(rollbackFor = Exception.class)
    public Result handleRequest(@RequestBody MatchmakingRequestHandleDTO dto, @RequestHeader("token") String token) {
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
            return Result.ERROR("您不是红娘，无权处理申请");
        }
        
        // 4. 查询红娘信息
        LambdaQueryWrapper<TbMatchmaker> matchmakerWrapper = new LambdaQueryWrapper<>();
        matchmakerWrapper.eq(TbMatchmaker::getUserId, userId);
        TbMatchmaker matchmaker = matchmakerService.getOne(matchmakerWrapper);
        if (matchmaker == null) {
            return Result.ERROR("红娘信息不存在");
        }
        
        // 5. 查询申请记录
        TbMatchmakingRequest request = matchmakingRequestService.getById(dto.getRequestId());
        if (request == null) {
            return Result.ERROR("申请记录不存在");
        }
        
        // 6. 验证是否是该红娘的申请
        if (!request.getMatchmakerId().equals(matchmaker.getMatchmakerId())) {
            return Result.ERROR("无权处理此申请");
        }
        
        // 7. 验证申请状态
        if (request.getRequestStatus() != 0) {
            return Result.ERROR("申请已被处理，无法重复操作");
        }
        
        // 8. 如果是拒绝申请，需要进行退款
        if (dto.getAction() == 2) {
            try {
                // 查找对应的订单进行退款
                // 通过用户ID、红娘ID和状态来查找对应的订单
                LambdaQueryWrapper<TbMatchmakerOrder> orderWrapper = new LambdaQueryWrapper<>();
                orderWrapper.eq(TbMatchmakerOrder::getUserId, request.getUserId())
                           .eq(TbMatchmakerOrder::getMatchmakerId, request.getMatchmakerId())
                           .eq(TbMatchmakerOrder::getOrderStatus, 1) // 已支付状态
                           .orderByDesc(TbMatchmakerOrder::getCreatedAt)
                           .last("LIMIT 1"); // 获取最新的一条记录

                TbMatchmakerOrder order = matchmakerOrderService.getOne(orderWrapper);
                System.out.println("查找订单结果: " + (order != null ? "找到订单ID=" + order.getOrderId() : "未找到订单"));
            if (order != null) {
                // 进行退款
                BigDecimal refundAmount = order.getPayAmount() != null ? order.getPayAmount() : order.getAmount();
                if (refundAmount == null) {
                    return Result.ERROR("订单金额异常，无法退款");
                }

                WalletDTO refundDTO = new WalletDTO();
                refundDTO.setUserId(request.getUserId());
                refundDTO.setCoinAmount(refundAmount.intValue());
                refundDTO.setTransactionDesc("红娘拒绝申请退款 - " + order.getOrderNo());
                refundDTO.setRelatedId("REFUND_" + order.getOrderNo());

                // 直接调用钱包服务进行退款（内部服务调用）
                Result refundResult = walletService.recharge(refundDTO);
                if (refundResult.getCode() != 200) {
                    return Result.ERROR("退款失败：" + refundResult.getMessage());
                }

                // 更新订单状态为已退款
                order.setOrderStatus(3); // 3-已退款
                order.setUpdatedAt(new Date());
                matchmakerOrderService.updateById(order);
            }
            } catch (Exception e) {
                System.err.println("退款处理异常: " + e.getMessage());
                e.printStackTrace();
                return Result.ERROR("退款处理失败: " + e.getMessage());
            }
        }

        // 9. 更新申请状态
        request.setRequestStatus(dto.getAction()); // 1-接受，2-拒绝
        if (dto.getAction() == 2 && dto.getRejectReason() != null) {
            request.setRejectReason(dto.getRejectReason());
        }
        request.setUpdatedAt(new Date());

        boolean success = matchmakingRequestService.updateById(request);
        if (success) {
            String actionText = dto.getAction() == 1 ? "接受" : "拒绝";
            String message = "申请已" + actionText;
            if (dto.getAction() == 2) {
                message += "，费用已退还给用户";
            }
            return Result.OK(message);
        } else {
            return Result.ERROR("处理失败，请重试");
        }
    }

    /**
     * 获取申请详情
     * @param requestId 申请ID
     * @param token 红娘token
     * @return 申请详情
     */
    @GetMapping("/detail/{requestId}")
    public Result getRequestDetail(@PathVariable Long requestId, @RequestHeader("token") String token) {
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
        
        // 3. 查询红娘信息
        LambdaQueryWrapper<TbMatchmaker> matchmakerWrapper = new LambdaQueryWrapper<>();
        matchmakerWrapper.eq(TbMatchmaker::getUserId, userId);
        TbMatchmaker matchmaker = matchmakerService.getOne(matchmakerWrapper);
        if (matchmaker == null) {
            return Result.ERROR("红娘信息不存在");
        }
        
        // 4. 查询申请记录
        TbMatchmakingRequest request = matchmakingRequestService.getById(requestId);
        if (request == null) {
            return Result.ERROR("申请记录不存在");
        }
        
        // 5. 验证是否是该红娘的申请
        if (!request.getMatchmakerId().equals(matchmaker.getMatchmakerId())) {
            return Result.ERROR("无权查看此申请");
        }
        
        // 6. 组装详细信息
        Map<String, Object> detail = new HashMap<>();
        detail.put("request", request);
        
        // 获取申请用户详细信息
        TbUser applicantUser = userService.getById(request.getUserId());
        if (applicantUser != null) {
            applicantUser.setPassword(null);
            detail.put("applicantUser", applicantUser);
        }
        
        // 获取目标用户详细信息
        TbUser targetUser = userService.getById(request.getTargetUserId());
        if (targetUser != null) {
            targetUser.setPassword(null);
            detail.put("targetUser", targetUser);
        }
        
        return Result.OK(detail);
    }


}
