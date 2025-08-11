package com.zhentao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhentao.dto.MatchmakingRequestCreateDTO;
import com.zhentao.dto.MatchmakerInfoDTO;
import com.zhentao.dto.WalletDTO;
import com.zhentao.pojo.TbMatchmakerOrder;
import com.zhentao.pojo.TbMatchmakingRequest;
import com.zhentao.pojo.TbUser;
import com.zhentao.service.*;
import com.zhentao.pojo.TbMatchmaker;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.zhentao.utils.JwtService;
import com.zhentao.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * 牵线申请控制器
 * @author zhentao
 */
@RestController
@RequestMapping("/user/matchmaking")
@CrossOrigin("*")
public class MatchmakingRequestController {
    
    @Autowired
    private TbMatchmakingRequestService matchmakingRequestService;
    
    @Autowired
    private TbUserService userService;

    @Autowired
    private TbMatchmakerOrderService matchmakerOrderService;

    @Autowired
    private TbMatchmakerService matchmakerService;

    @Autowired
    private WalletService walletService;

    /**
     * 测试接口
     * @return 测试结果
     */
    @GetMapping("/test")
    public Result test() {
        try {
            // 测试Service是否正常注入
            if (matchmakingRequestService == null) {
                return Result.ERROR("matchmakingRequestService 注入失败");
            }
            if (userService == null) {
                return Result.ERROR("userService 注入失败");
            }

            // 测试数据库连接
            long count = matchmakingRequestService.count();

            return Result.OK("牵线申请接口正常工作，当前申请记录数：" + count);
        } catch (Exception e) {
            return Result.ERROR("测试失败：" + e.getMessage());
        }
    }

    /**
     * 获取可用红娘列表
     * @param token 用户token
     * @param level 红娘等级筛选（可选）：1-预备红娘，2-正式红娘，3-金牌红娘
     * @return 红娘列表
     */
    @GetMapping("/available-matchmakers")
    public Result getAvailableMatchmakers(@RequestHeader("token") String token,
                                        @RequestParam(required = false) Integer level) {
        // 1. 验证token
        int verifyResult = JwtService.verifyToken(token);
        if (verifyResult != 1) {
            return Result.NO_LOGIN();
        }

        try {
            // 2. 查询所有可用的红娘（状态为1-正常服务）
            LambdaQueryWrapper<TbMatchmaker> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbMatchmaker::getMatchmakerStatus, 1); // 正常服务状态

            // 3. 如果指定了等级，添加等级筛选
            if (level != null && level >= 1 && level <= 3) {
                wrapper.eq(TbMatchmaker::getMatchmakerLevel, level);
            }

            // 4. 排序：按等级降序，然后按成功次数降序
            wrapper.orderByDesc(TbMatchmaker::getMatchmakerLevel)
                   .orderByDesc(TbMatchmaker::getSuccessCount);

            List<TbMatchmaker> availableMatchmakers = matchmakerService.list(wrapper);

            // 5. 转换为DTO并计算相关信息
            List<MatchmakerInfoDTO> matchmakerInfoList = availableMatchmakers.stream()
                .map(this::convertToMatchmakerInfoDTO)
                .collect(Collectors.toList());

            // 6. 标记推荐红娘（前3名）
            for (int i = 0; i < Math.min(3, matchmakerInfoList.size()); i++) {
                matchmakerInfoList.get(i).setRecommended(true);
            }

            return Result.OK(matchmakerInfoList);
        } catch (Exception e) {
            return Result.ERROR("获取红娘列表失败：" + e.getMessage());
        }
    }

    /**
     * 创建付费牵线申请（第一步：创建订单）
     * @param dto 申请信息
     * @param token 用户token
     * @return 订单创建结果
     */
    @PostMapping("/create-paid-request")
    @Transactional(rollbackFor = Exception.class)
    public Result createPaidRequest(@RequestBody MatchmakingRequestCreateDTO dto, @RequestHeader("token") String token) {
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

        // 3. 验证当前用户是否存在
        TbUser currentUser = userService.getById(userId);
        if (currentUser == null) {
            return Result.ERROR("用户不存在");
        }

        // 4. 验证目标用户是否存在
        TbUser targetUser = userService.getById(dto.getTargetUserId());
        if (targetUser == null) {
            return Result.ERROR("目标用户不存在");
        }

        // 5. 检查是否不能向自己发起申请
        if (userId.equals(dto.getTargetUserId())) {
            return Result.ERROR("不能向自己发起牵线申请");
        }

        // 6. 检查是否已经对该用户发起过申请
        LambdaQueryWrapper<TbMatchmakingRequest> requestWrapper = new LambdaQueryWrapper<>();
        requestWrapper.eq(TbMatchmakingRequest::getUserId, userId)
                     .eq(TbMatchmakingRequest::getTargetUserId, dto.getTargetUserId())
                     .in(TbMatchmakingRequest::getRequestStatus, -1, 0, 1, 4); // 待支付、待处理、已接受、可安排约会

        TbMatchmakingRequest existingRequest = matchmakingRequestService.getOne(requestWrapper);
        if (existingRequest != null) {
            return Result.ERROR("您已经向该用户发起过牵线申请，请勿重复申请");
        }

        // 7. 创建红娘服务订单
        TbMatchmakerOrder order = new TbMatchmakerOrder();
        order.setUserId(userId);
        order.setOrderNo("MM" + System.currentTimeMillis()); // 订单编号
        order.setServiceType(1); // 1-单次牵线服务
        order.setServiceDesc("红娘牵线服务：" + currentUser.getNickname() + " → " + targetUser.getNickname());
        order.setAmount(new BigDecimal("199.00")); // 服务费用199元
        order.setPayAmount(new BigDecimal("199.00")); // 实际支付金额
        order.setDiscountAmount(new BigDecimal("0.00")); // 优惠金额
        order.setOrderStatus(0); // 0-待支付
        order.setCreatedAt(new Date());
        order.setUpdatedAt(new Date());

        // 8. 保存订单
        boolean orderSuccess = matchmakerOrderService.save(order);
        if (!orderSuccess) {
            return Result.ERROR("订单创建失败，请重试");
        }

        // 9. 创建牵线申请记录（待支付状态）
        TbMatchmakingRequest request = new TbMatchmakingRequest();
        request.setUserId(userId);
        request.setTargetUserId(dto.getTargetUserId());
        request.setMatchmakerId(null); // 支付成功后分配红娘
        request.setRequestMessage(dto.getRequestMessage() + " [付费申请-订单号:" + order.getOrderNo() + "]");
        request.setRequestStatus(-1); // -1表示待支付状态
        request.setCreatedAt(new Date());
        request.setUpdatedAt(new Date());

        boolean requestSuccess = matchmakingRequestService.save(request);
        if (!requestSuccess) {
            return Result.ERROR("申请记录创建失败，请重试");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getOrderId());
        result.put("orderNo", order.getOrderNo());
        result.put("requestId", request.getRequestId());
        result.put("amount", order.getAmount());
        result.put("targetUserName", targetUser.getNickname());
        result.put("message", "订单创建成功，请完成支付");
        return Result.OK(result);
    }

    /**
     * 创建牵线申请订单（选择红娘后创建订单）
     * @param dto 申请信息
     * @param token 用户token
     * @return 订单创建结果
     */
    @PostMapping("/submit")
    @Transactional(rollbackFor = Exception.class)
    public Result submitRequest(@RequestBody MatchmakingRequestCreateDTO dto, @RequestHeader("token") String token) {
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

        // 3. 验证当前用户是否存在
        TbUser currentUser = userService.getById(userId);
        if (currentUser == null) {
            return Result.ERROR("用户不存在");
        }

        // 4. 验证目标用户是否存在
        TbUser targetUser = userService.getById(dto.getTargetUserId());
        if (targetUser == null) {
            return Result.ERROR("目标用户不存在");
        }

        // 5. 检查是否不能向自己发起申请
        if (userId.equals(dto.getTargetUserId())) {
            return Result.ERROR("不能向自己发起牵线申请");
        }

        // 6. 检查是否已经申请过
        LambdaQueryWrapper<TbMatchmakingRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbMatchmakingRequest::getUserId, userId)
               .eq(TbMatchmakingRequest::getTargetUserId, dto.getTargetUserId())
               .in(TbMatchmakingRequest::getRequestStatus, -1, 0, 1, 4); // 待支付、待处理、已接受、可安排约会

        TbMatchmakingRequest existingRequest = matchmakingRequestService.getOne(wrapper);
        if (existingRequest != null) {
            String statusText = getRequestStatusText(existingRequest.getRequestStatus());
            return Result.ERROR("您已经向该用户发起过牵线申请，当前状态：" + statusText);
        }

        // 7. 确定红娘分配方式
        Long assignedMatchmakerId = null;
        if (dto.getMatchmakerId() != null) {
            // 指定红娘
            assignedMatchmakerId = dto.getMatchmakerId();
            // 验证红娘是否存在且可用
            TbMatchmaker matchmaker = matchmakerService.getById(assignedMatchmakerId);
            if (matchmaker == null || matchmaker.getMatchmakerStatus() != 1) {
                return Result.ERROR("指定的红娘不存在或不可用");
            }
        } else if (dto.getMatchmakerLevel() != null) {
            // 按等级智能分配
            assignedMatchmakerId = assignMatchmakerByLevel(dto.getMatchmakerLevel());
            if (assignedMatchmakerId == null) {
                return Result.ERROR("暂无可用的" + getMatchmakerLevelText(dto.getMatchmakerLevel()) + "，请选择其他等级或稍后再试");
            }
        } else {
            // 智能分配（从所有等级中选择）
            assignedMatchmakerId = assignMatchmaker();
            if (assignedMatchmakerId == null) {
                return Result.ERROR("暂无可用红娘，请稍后再试");
            }
        }

        // 8. 创建红娘服务订单
        TbMatchmakerOrder order = createMatchmakerOrder(userId, assignedMatchmakerId, dto);
        boolean orderSuccess = matchmakerOrderService.save(order);
        if (!orderSuccess) {
            return Result.ERROR("订单创建失败，请重试");
        }

        // 9. 创建牵线申请记录（待支付状态）
        TbMatchmakingRequest request = new TbMatchmakingRequest();
        request.setUserId(userId);
        request.setTargetUserId(dto.getTargetUserId());
        request.setMatchmakerId(assignedMatchmakerId);
        request.setRequestMessage(dto.getRequestMessage()); // 保持原始留言，不添加订单号
        request.setRequestStatus(-1); // -1表示待支付状态
        request.setCreatedAt(new Date());
        request.setUpdatedAt(new Date());

        boolean requestSuccess = matchmakingRequestService.save(request);
        if (!requestSuccess) {
            return Result.ERROR("申请记录创建失败，请重试");
        }

        // 10. 返回订单信息，引导用户支付
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getOrderId());
        result.put("orderNo", order.getOrderNo());
        result.put("requestId", request.getRequestId());
        result.put("amount", order.getPayAmount());
        result.put("targetUserName", targetUser.getNickname());

        // 获取红娘信息
        TbMatchmaker assignedMatchmaker = matchmakerService.getById(assignedMatchmakerId);
        if (assignedMatchmaker != null) {
            TbUser matchmakerUser = userService.getById(assignedMatchmaker.getUserId());
            String matchmakerName = matchmakerUser != null ? matchmakerUser.getNickname() : "红娘" + assignedMatchmakerId;
            result.put("matchmakerName", matchmakerName);
            result.put("matchmakerLevel", assignedMatchmaker.getMatchmakerLevel());
            result.put("matchmakerLevelText", getMatchmakerLevelText(assignedMatchmaker.getMatchmakerLevel()));
        }

        result.put("message", "订单创建成功，请完成支付后我们将为您安排专业红娘服务");
        return Result.OK(result);
    }

    /**
     * 支付红娘服务订单（第二步：支付）
     * @param orderId 订单ID
     * @param payType 支付方式：1-微信，2-支付宝，3-虚拟币
     * @param token 用户token
     * @return 支付结果
     */
    @PostMapping("/pay-order/{orderId}")
    @Transactional(rollbackFor = Exception.class)
    public Result payOrder(@PathVariable Long orderId,
                          @RequestParam Integer payType,
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

        // 3. 查询订单
        TbMatchmakerOrder order = matchmakerOrderService.getById(orderId);
        if (order == null) {
            return Result.ERROR("订单不存在");
        }

        // 4. 验证订单归属
        if (!order.getUserId().equals(userId)) {
            return Result.ERROR("无权操作此订单");
        }

        // 5. 验证订单状态
        if (order.getOrderStatus() != 0) {
            return Result.ERROR("订单状态异常，无法支付");
        }

        // 6. 根据支付方式处理
        if (payType == 3) { // 虚拟币支付
            // 使用虚拟币支付
            WalletDTO walletDTO = new WalletDTO();
            walletDTO.setUserId(userId);
            walletDTO.setCoinAmount(order.getPayAmount().intValue()); // 假设1元=1虚拟币
            walletDTO.setTransactionDesc("红娘牵线服务 - " + order.getOrderNo());
            walletDTO.setRelatedId("MATCHMAKER_ORDER_" + order.getOrderNo());

            Result consumeResult = walletService.consume(walletDTO);
            if (consumeResult.getCode() != 200) {
                return consumeResult; // 返回支付失败原因
            }

            // 更新订单状态
            order.setOrderStatus(1); // 1-已支付
            order.setPayType(payType);
            order.setPayTime(new Date());
            order.setTransactionId("WALLET_" + System.currentTimeMillis());
            order.setUpdatedAt(new Date());

        } else {
            // TODO: 集成微信/支付宝支付
            return Result.ERROR("暂不支持此支付方式，请使用虚拟币支付");
        }

        // 7. 保存订单更新
        boolean updateSuccess = matchmakerOrderService.updateById(order);
        if (!updateSuccess) {
            return Result.ERROR("订单更新失败");
        }

        // 8. 支付成功后更新申请状态并分配红娘
        return updateRequestAfterPayment(order.getOrderNo());
    }

    /**
     * 支付成功后更新申请状态并分配红娘
     */
    private Result updateRequestAfterPayment(String orderNo) {
        // 1. 查找对应的申请记录
        LambdaQueryWrapper<TbMatchmakingRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TbMatchmakingRequest::getRequestMessage, orderNo)
               .eq(TbMatchmakingRequest::getRequestStatus, -1); // 待支付状态

        TbMatchmakingRequest request = matchmakingRequestService.getOne(wrapper);
        if (request == null) {
            return Result.ERROR("未找到对应的申请记录");
        }

        // 2. 分配红娘
        Long assignedMatchmakerId = assignMatchmaker();

        // 3. 更新申请状态
        request.setMatchmakerId(assignedMatchmakerId);
        request.setRequestStatus(0); // 0-待处理
        request.setUpdatedAt(new Date());

        boolean success = matchmakingRequestService.updateById(request);
        if (success) {
            return Result.OK("支付成功！专业红娘已为您分配，将在24小时内联系您");
        } else {
            return Result.ERROR("支付成功但状态更新失败，请联系客服");
        }
    }

    /**
     * 获取我发起的牵线申请列表
     * @param token 用户token
     * @return 申请列表
     */
    @GetMapping("/my-requests")
    public Result getMyRequests(@RequestHeader("token") String token) {
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
        
        // 3. 查询我发起的申请
        LambdaQueryWrapper<TbMatchmakingRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbMatchmakingRequest::getUserId, userId)
               .orderByDesc(TbMatchmakingRequest::getCreatedAt);
        
        return Result.OK(matchmakingRequestService.list(wrapper));
    }
    
    /**
     * 取消牵线申请
     * @param requestId 申请ID
     * @param token 用户token
     * @return 取消结果
     */
    @PostMapping("/cancel/{requestId}")
    public Result cancelRequest(@PathVariable Long requestId, @RequestHeader("token") String token) {
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
        TbMatchmakingRequest request = matchmakingRequestService.getById(requestId);
        if (request == null) {
            return Result.ERROR("申请记录不存在");
        }
        
        // 4. 验证是否是本人的申请
        if (!request.getUserId().equals(userId)) {
            return Result.ERROR("无权操作此申请");
        }
        
        // 5. 只有待处理状态的申请才能取消
        if (request.getRequestStatus() != 0) {
            return Result.ERROR("只有待处理状态的申请才能取消");
        }
        
        // 6. 用户主动取消已支付申请，需要进行退款处理
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
                // 进行退款（扣除10%手续费）
                BigDecimal refundAmount = order.getPayAmount() != null ? order.getPayAmount() : order.getAmount();
                if (refundAmount != null) {
                    // 计算退款金额（90%退款，10%作为手续费）
                    BigDecimal actualRefund = refundAmount.multiply(new BigDecimal("0.9"));

                    WalletDTO refundDTO = new WalletDTO();
                    refundDTO.setUserId(request.getUserId());
                    refundDTO.setCoinAmount(actualRefund.intValue());
                    refundDTO.setTransactionDesc("用户主动取消申请退款（扣除10%手续费） - " + order.getOrderNo());
                    refundDTO.setRelatedId("USER_CANCEL_REFUND_" + order.getOrderNo());

                    Result refundResult = walletService.recharge(refundDTO);
                    if (refundResult.getCode() == 200) {
                        // 更新订单状态为已退款
                        order.setOrderStatus(3); // 3-已退款
                        order.setUpdatedAt(new Date());
                        matchmakerOrderService.updateById(order);

                        System.out.println("用户主动取消申请，退款成功: " + actualRefund + "虚拟币（扣除10%手续费）");
                    } else {
                        System.err.println("用户主动取消申请，退款失败: " + refundResult.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("用户主动取消申请，退款处理异常: " + e.getMessage());
            e.printStackTrace();
        }

        // 7. 更新申请状态为已取消
        request.setRequestStatus(2);
        request.setRejectReason("用户主动取消");
        request.setUpdatedAt(new Date());

        boolean success = matchmakingRequestService.updateById(request);
        if (success) {
            return Result.OK("申请已取消，90%费用已退还（扣除10%手续费）");
        } else {
            return Result.ERROR("取消失败，请重试");
        }
    }

    /**
     * 按等级智能分配红娘
     * @param level 红娘等级，如果为null则从所有等级中选择
     * @return 分配的红娘ID，如果没有可用红娘则返回null
     */
    private Long assignMatchmakerByLevel(Integer level) {
        try {
            // 查询指定等级的可用红娘（状态为1-正常服务）
            LambdaQueryWrapper<TbMatchmaker> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbMatchmaker::getMatchmakerStatus, 1);

            // 如果指定了等级，添加等级筛选
            if (level != null && level >= 1 && level <= 3) {
                wrapper.eq(TbMatchmaker::getMatchmakerLevel, level);
            }

            wrapper.orderByDesc(TbMatchmaker::getMatchmakerLevel)
                   .orderByDesc(TbMatchmaker::getSuccessCount);

            List<TbMatchmaker> availableMatchmakers = matchmakerService.list(wrapper);

            if (availableMatchmakers.isEmpty()) {
                return null; // 没有可用的红娘
            }

            // 智能分配：计算每个红娘的综合评分
            TbMatchmaker bestMatchmaker = null;
            double bestScore = -1;

            for (TbMatchmaker matchmaker : availableMatchmakers) {
                // 查询当前处理的申请数量
                LambdaQueryWrapper<TbMatchmakingRequest> requestWrapper = new LambdaQueryWrapper<>();
                requestWrapper.eq(TbMatchmakingRequest::getMatchmakerId, matchmaker.getMatchmakerId())
                             .in(TbMatchmakingRequest::getRequestStatus, 0, 1);
                long currentRequests = matchmakingRequestService.count(requestWrapper);

                // 计算综合评分
                double score = calculateMatchmakerScore(matchmaker, currentRequests);

                if (score > bestScore) {
                    bestScore = score;
                    bestMatchmaker = matchmaker;
                }
            }

            return bestMatchmaker != null ? bestMatchmaker.getMatchmakerId() : null;
        } catch (Exception e) {
            System.err.println("分配红娘失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 自动分配红娘（兼容旧方法）
     * 分配策略：综合考虑红娘等级、成功次数、从业年限和当前工作负载
     * @return 分配的红娘ID，如果没有可用红娘则返回null
     */
    private Long assignMatchmaker() {
        return assignMatchmakerByLevel(null);
    }

    /**
     * 计算红娘的综合评分
     * @param matchmaker 红娘信息
     * @param currentRequests 当前处理的申请数量
     * @return 综合评分（分数越高越优先）
     */
    private double calculateMatchmakerScore(TbMatchmaker matchmaker, long currentRequests) {
        // 基础分数
        double score = 0;

        // 等级权重（30%）：1级=1分，2级=2分，3级=3分
        score += (matchmaker.getMatchmakerLevel() != null ? matchmaker.getMatchmakerLevel() : 1) * 30;

        // 成功次数权重（25%）：每成功一次+0.5分
        score += (matchmaker.getSuccessCount() != null ? matchmaker.getSuccessCount() : 0) * 0.5 * 25;

        // 从业年限权重（20%）：每年+2分
        score += (matchmaker.getServiceYears() != null ? matchmaker.getServiceYears() : 0) * 2 * 20;

        // 工作负载权重（25%）：当前申请数越少分数越高
        // 假设最大处理能力为20个申请，超过则分数为0
        double loadScore = Math.max(0, (20 - currentRequests) / 20.0) * 25;
        score += loadScore;

        return score;
    }

    /**
     * 转换红娘实体为DTO
     * @param matchmaker 红娘实体
     * @return 红娘信息DTO
     */
    private MatchmakerInfoDTO convertToMatchmakerInfoDTO(TbMatchmaker matchmaker) {
        MatchmakerInfoDTO dto = new MatchmakerInfoDTO();
        dto.setMatchmakerId(matchmaker.getMatchmakerId());

        // 通过userId获取用户昵称
        String nickname = "红娘" + matchmaker.getMatchmakerId(); // 默认昵称
        try {
            TbUser user = userService.getById(matchmaker.getUserId());
            if (user != null && user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
                nickname = user.getNickname();
            }
        } catch (Exception e) {
            // 获取失败时使用默认昵称
            System.err.println("获取红娘昵称失败: " + e.getMessage());
        }
        dto.setNickname(nickname);

        dto.setMatchmakerLevel(matchmaker.getMatchmakerLevel());
        dto.setServiceArea(matchmaker.getServiceArea());
        dto.setServiceYears(matchmaker.getServiceYears());
        dto.setSuccessCount(matchmaker.getSuccessCount());
        dto.setIntroduction(matchmaker.getIntroduction());
        dto.setRecommended(false); // 默认不推荐，后续会设置

        // 设置等级文本
        String levelText = "预备红娘";
        if (matchmaker.getMatchmakerLevel() != null) {
            switch (matchmaker.getMatchmakerLevel()) {
                case 1:
                    levelText = "预备红娘";
                    break;
                case 2:
                    levelText = "正式红娘";
                    break;
                case 3:
                    levelText = "金牌红娘";
                    break;
                default:
                    levelText = "预备红娘";
            }
        }
        dto.setMatchmakerLevelText(levelText);

        // 计算当前处理的申请数量
        try {
            LambdaQueryWrapper<TbMatchmakingRequest> requestWrapper = new LambdaQueryWrapper<>();
            requestWrapper.eq(TbMatchmakingRequest::getMatchmakerId, matchmaker.getMatchmakerId())
                         .in(TbMatchmakingRequest::getRequestStatus, 0, 1); // 待处理或已接受
            long currentRequests = matchmakingRequestService.count(requestWrapper);
            dto.setCurrentRequests(currentRequests);

            // 计算综合评分
            double score = calculateMatchmakerScore(matchmaker, currentRequests);
            dto.setScore(score);
        } catch (Exception e) {
            // 计算失败时设置默认值
            dto.setCurrentRequests(0L);
            dto.setScore(0.0);
        }
        return dto;
    }

    /**
     * 创建红娘服务订单
     * @param userId 用户ID
     * @param matchmakerId 红娘ID
     * @param dto 申请DTO
     * @return 订单对象
     */
    private TbMatchmakerOrder createMatchmakerOrder(Long userId, Long matchmakerId, MatchmakingRequestCreateDTO dto) {
        TbMatchmakerOrder order = new TbMatchmakerOrder();

        // 生成订单编号
        String orderNo = "MM" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));

        // 基本信息
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setMatchmakerId(matchmakerId);

        // 服务信息
        order.setServiceType(1); // 1-单次牵线服务

        // 获取红娘信息用于服务描述
        String serviceDesc = "牵线服务 - 专业红娘为您匹配心仪对象";
        try {
            TbUser targetUser = userService.getById(dto.getTargetUserId());
            TbMatchmaker matchmaker = matchmakerService.getById(matchmakerId);
            if (targetUser != null && matchmaker != null) {
                TbUser matchmakerUser = userService.getById(matchmaker.getUserId());
                String matchmakerName = matchmakerUser != null ? matchmakerUser.getNickname() : "红娘" + matchmakerId;
                String levelText = getMatchmakerLevelText(matchmaker.getMatchmakerLevel());
                serviceDesc = String.format("牵线服务 - %s为您匹配 %s",
                    levelText + matchmakerName, targetUser.getNickname());
            }
        } catch (Exception e) {
            // 使用默认描述
            System.err.println("获取服务描述信息失败: " + e.getMessage());
        }
        order.setServiceDesc(serviceDesc);

        // 价格信息（根据红娘等级设置不同价格）
        BigDecimal amount = getServiceAmount(matchmakerId);
        order.setAmount(amount);
        order.setPayAmount(amount);
        order.setDiscountAmount(BigDecimal.ZERO);

        // 订单状态
        order.setOrderStatus(0); // 0-待支付
        order.setPayType(null);
        order.setPayTime(null);
        order.setTransactionId(null);

        // 服务时间（暂时为空，支付后设置）
        order.setServiceStartTime(null);
        order.setServiceEndTime(null);

        // 创建时间
        order.setCreatedAt(new Date());
        order.setUpdatedAt(new Date());

        return order;
    }

    /**
     * 根据红娘等级获取服务价格
     * @param matchmakerId 红娘ID
     * @return 服务价格
     */
    private BigDecimal getServiceAmount(Long matchmakerId) {
        // 现在所有红娘都是统一价格69.9元
        return new BigDecimal("69.90");
    }

    /**
     * 获取申请状态文本
     * @param status 状态码
     * @return 状态文本
     */
    private String getRequestStatusText(Integer status) {
        if (status == null) {
            return "未知状态";
        }
        switch (status) {
            case -1:
                return "待支付";
            case 0:
                return "待处理";
            case 1:
                return "已接受";
            case 2:
                return "已拒绝";
            case 3:
                return "目标用户已拒绝";
            case 4:
                return "可安排约会";
            case 5:
                return "已完成";
            default:
                return "未知状态";
        }
    }

    /**
     * 获取红娘等级文本
     * @param level 等级
     * @return 等级文本
     */
    private String getMatchmakerLevelText(Integer level) {
        if (level == null) {
            return "预备红娘";
        }
        switch (level) {
            case 1:
                return "预备红娘";
            case 2:
                return "正式红娘";
            case 3:
                return "金牌红娘";
            default:
                return "预备红娘";
        }
    }
}
