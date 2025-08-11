package com.zhentao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhentao.dto.MatchmakerOrderDTO;
import com.zhentao.dto.MatchmakerOrderQueryDTO;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 红娘服务订单控制器
 * @author zhentao
 */
@RestController
@RequestMapping("/user/matchmaker-order")
@CrossOrigin("*")
public class MatchmakerOrderController {
    
    @Autowired
    private TbMatchmakerOrderService matchmakerOrderService;
    
    @Autowired
    private TbUserService userService;
    
    @Autowired
    private TbMatchmakerService matchmakerService;

    @Autowired
    private TbMatchmakingRequestService matchmakingRequestService;

    @Autowired
    private WalletService walletService;
    
    /**
     * 获取我的订单列表
     * @param queryDTO 查询参数
     * @param token 用户token
     * @return 订单列表
     */
    @PostMapping("/my-orders")
    public Result getMyOrders(@RequestBody MatchmakerOrderQueryDTO queryDTO, @RequestHeader("token") String token) {
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
            LambdaQueryWrapper<TbMatchmakerOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbMatchmakerOrder::getUserId, userId);
            
            // 添加其他查询条件
            if (queryDTO.getOrderStatus() != null) {
                wrapper.eq(TbMatchmakerOrder::getOrderStatus, queryDTO.getOrderStatus());
            }
            if (queryDTO.getServiceType() != null) {
                wrapper.eq(TbMatchmakerOrder::getServiceType, queryDTO.getServiceType());
            }
            if (queryDTO.getPayType() != null) {
                wrapper.eq(TbMatchmakerOrder::getPayType, queryDTO.getPayType());
            }
            if (StringUtils.hasText(queryDTO.getOrderNo())) {
                wrapper.like(TbMatchmakerOrder::getOrderNo, queryDTO.getOrderNo());
            }
            
            // 时间范围查询
            if (StringUtils.hasText(queryDTO.getStartTime())) {
                try {
                    Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(queryDTO.getStartTime());
                    wrapper.ge(TbMatchmakerOrder::getCreatedAt, startTime);
                } catch (ParseException e) {
                    // 忽略时间解析错误
                }
            }
            if (StringUtils.hasText(queryDTO.getEndTime())) {
                try {
                    Date endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(queryDTO.getEndTime());
                    wrapper.le(TbMatchmakerOrder::getCreatedAt, endTime);
                } catch (ParseException e) {
                    // 忽略时间解析错误
                }
            }
            
            // 按创建时间倒序
            wrapper.orderByDesc(TbMatchmakerOrder::getCreatedAt);
            
            // 4. 分页查询
            Page<TbMatchmakerOrder> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
            IPage<TbMatchmakerOrder> orderPage = matchmakerOrderService.page(page, wrapper);
            
            // 5. 转换为DTO
            List<MatchmakerOrderDTO> orderDTOList = orderPage.getRecords().stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());
            
            // 6. 构建返回结果
            Page<MatchmakerOrderDTO> resultPage = new Page<>();
            resultPage.setRecords(orderDTOList);
            resultPage.setTotal(orderPage.getTotal());
            resultPage.setCurrent(orderPage.getCurrent());
            resultPage.setSize(orderPage.getSize());
            resultPage.setPages(orderPage.getPages());
            
            return Result.OK(resultPage);
        } catch (Exception e) {
            return Result.ERROR("查询订单列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取订单详情
     * @param orderId 订单ID
     * @param token 用户token
     * @return 订单详情
     */
    @GetMapping("/detail/{orderId}")
    public Result getOrderDetail(@PathVariable Long orderId, @RequestHeader("token") String token) {
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
            // 3. 查询订单
            TbMatchmakerOrder order = matchmakerOrderService.getById(orderId);
            if (order == null) {
                return Result.ERROR("订单不存在");
            }
            
            // 4. 验证订单归属
            if (!order.getUserId().equals(userId)) {
                return Result.ERROR("无权查看此订单");
            }
            
            // 5. 转换为DTO并返回
            MatchmakerOrderDTO orderDTO = convertToOrderDTO(order);
            return Result.OK(orderDTO);
        } catch (Exception e) {
            return Result.ERROR("查询订单详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 取消订单
     * @param orderId 订单ID
     * @param token 用户token
     * @return 取消结果
     */
    @PostMapping("/cancel/{orderId}")
    @Transactional(rollbackFor = Exception.class)
    public Result cancelOrder(@PathVariable Long orderId, @RequestHeader("token") String token) {
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
            // 3. 查询订单
            TbMatchmakerOrder order = matchmakerOrderService.getById(orderId);
            if (order == null) {
                return Result.ERROR("订单不存在");
            }
            
            // 4. 验证订单归属
            if (!order.getUserId().equals(userId)) {
                return Result.ERROR("无权操作此订单");
            }
            
            // 5. 验证订单状态（只有待支付的订单才能取消）
            if (order.getOrderStatus() != 0) {
                return Result.ERROR("订单状态异常，无法取消");
            }
            
            // 6. 查找并删除对应的牵线申请记录
            // 通过用户ID、红娘ID和状态来查找对应的申请记录
            LambdaQueryWrapper<TbMatchmakingRequest> requestWrapper = new LambdaQueryWrapper<>();
            requestWrapper.eq(TbMatchmakingRequest::getUserId, order.getUserId())
                         .eq(TbMatchmakingRequest::getMatchmakerId, order.getMatchmakerId())
                         .eq(TbMatchmakingRequest::getRequestStatus, -1) // 待支付状态
                         .orderByDesc(TbMatchmakingRequest::getCreatedAt)
                         .last("LIMIT 1"); // 获取最新的一条记录

            TbMatchmakingRequest matchmakingRequest = matchmakingRequestService.getOne(requestWrapper);
            if (matchmakingRequest != null) {
                // 删除牵线申请记录
                boolean deleteRequestSuccess = matchmakingRequestService.removeById(matchmakingRequest.getRequestId());
                if (!deleteRequestSuccess) {
                    return Result.ERROR("删除申请记录失败，请重试");
                }
            }

            // 7. 更新订单状态
            order.setOrderStatus(2); // 2-已取消
            order.setUpdatedAt(new Date());

            boolean success = matchmakerOrderService.updateById(order);
            if (success) {
                return Result.OK("订单已取消，相关申请记录已删除");
            } else {
                return Result.ERROR("取消订单失败，请重试");
            }
        } catch (Exception e) {
            return Result.ERROR("取消订单失败：" + e.getMessage());
        }
    }
    
    /**
     * 转换订单实体为DTO
     * @param order 订单实体
     * @return 订单DTO
     */
    private MatchmakerOrderDTO convertToOrderDTO(TbMatchmakerOrder order) {
        MatchmakerOrderDTO dto = new MatchmakerOrderDTO();
        
        // 基本信息
        dto.setOrderId(order.getOrderId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setMatchmakerId(order.getMatchmakerId());
        dto.setServiceType(order.getServiceType());
        dto.setServiceDesc(order.getServiceDesc());
        dto.setAmount(order.getAmount());
        dto.setPayAmount(order.getPayAmount());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setPayType(order.getPayType());
        dto.setPayTime(order.getPayTime());
        dto.setTransactionId(order.getTransactionId());
        dto.setServiceStartTime(order.getServiceStartTime());
        dto.setServiceEndTime(order.getServiceEndTime());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        
        // 设置文本描述
        dto.setServiceTypeText(getServiceTypeText(order.getServiceType()));
        dto.setPayTypeText(getPayTypeText(order.getPayType()));
        dto.setOrderStatusText(getOrderStatusText(order.getOrderStatus()));
        
        // 设置操作权限
        dto.setCanCancel(order.getOrderStatus() == 0); // 只有待支付的订单可以取消
        dto.setCanPay(order.getOrderStatus() == 0); // 只有待支付的订单可以支付
        dto.setCanRefund(order.getOrderStatus() == 1); // 只有已支付的订单可以申请退款
        
        // 获取用户昵称
        try {
            TbUser user = userService.getById(order.getUserId());
            if (user != null) {
                dto.setUserNickname(user.getNickname());
            }
        } catch (Exception e) {
            // 忽略获取用户信息失败的情况
        }
        
        // 获取红娘昵称
        if (order.getMatchmakerId() != null) {
            try {
                TbMatchmaker matchmaker = matchmakerService.getById(order.getMatchmakerId());
                if (matchmaker != null) {
                    TbUser matchmakerUser = userService.getById(matchmaker.getUserId());
                    if (matchmakerUser != null) {
                        dto.setMatchmakerNickname(matchmakerUser.getNickname());
                    }
                }
            } catch (Exception e) {
                // 忽略获取红娘信息失败的情况
            }
        }
        
        return dto;
    }
    
    /**
     * 获取服务类型文本
     */
    private String getServiceTypeText(Integer serviceType) {
        if (serviceType == null) return "未知";
        switch (serviceType) {
            case 1: return "单次牵线";
            case 2: return "包月服务";
            case 3: return "高端定制";
            default: return "未知";
        }
    }
    
    /**
     * 获取支付方式文本
     */
    private String getPayTypeText(Integer payType) {
        if (payType == null) return "未支付";
        switch (payType) {
            case 1: return "微信支付";
            case 2: return "支付宝";
            case 3: return "虚拟币";
            case 4: return "其他";
            default: return "未知";
        }
    }
    
    /**
     * 支付订单
     * @param orderId 订单ID
     * @param payType 支付方式：1-微信，2-支付宝，3-虚拟币
     * @param token 用户token
     * @return 支付结果
     */
    @PostMapping("/pay/{orderId}")
    @Transactional(rollbackFor = Exception.class)
    public Result payOrder(@PathVariable Long orderId, @RequestParam Integer payType, @RequestHeader("token") String token) {
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
            // 3. 查询订单
            TbMatchmakerOrder order = matchmakerOrderService.getById(orderId);
            if (order == null) {
                return Result.ERROR("订单不存在");
            }

            // 4. 验证订单归属
            if (!order.getUserId().equals(userId)) {
                return Result.ERROR("无权操作此订单");
            }

            // 5. 验证订单状态（只有待支付的订单才能支付）
            if (order.getOrderStatus() != 0) {
                return Result.ERROR("订单状态异常，无法支付");
            }

            // 6. 验证支付方式
            if (payType == null || payType < 1 || payType > 4) {
                return Result.ERROR("支付方式无效");
            }

            // 7. 处理支付（这里简化处理，实际应该调用支付接口）
            boolean paymentSuccess = processPayment(order, payType, userId);
            if (!paymentSuccess) {
                return Result.ERROR("支付失败，请重试");
            }

            // 8. 更新订单状态
            order.setOrderStatus(1); // 1-已支付
            order.setPayType(payType);
            order.setPayTime(new Date());
            order.setTransactionId("TXN" + System.currentTimeMillis()); // 模拟交易号
            order.setServiceStartTime(new Date()); // 服务开始时间
            // 服务结束时间设为30天后
            order.setServiceEndTime(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000));
            order.setUpdatedAt(new Date());

            boolean orderUpdateSuccess = matchmakerOrderService.updateById(order);
            if (!orderUpdateSuccess) {
                return Result.ERROR("支付处理失败，请联系客服");
            }

            // 9. 同步更新对应的牵线申请记录状态
            LambdaQueryWrapper<TbMatchmakingRequest> requestWrapper = new LambdaQueryWrapper<>();
            requestWrapper.eq(TbMatchmakingRequest::getUserId, order.getUserId())
                         .eq(TbMatchmakingRequest::getMatchmakerId, order.getMatchmakerId())
                         .eq(TbMatchmakingRequest::getRequestStatus, -1) // 待支付状态
                         .orderByDesc(TbMatchmakingRequest::getCreatedAt)
                         .last("LIMIT 1"); // 获取最新的一条记录

            TbMatchmakingRequest matchmakingRequest = matchmakingRequestService.getOne(requestWrapper);
            if (matchmakingRequest != null) {
                // 更新申请状态为待处理（红娘可以看到）
                matchmakingRequest.setRequestStatus(0); // 0-待处理
                matchmakingRequest.setUpdatedAt(new Date());

                boolean requestUpdateSuccess = matchmakingRequestService.updateById(matchmakingRequest);
                if (!requestUpdateSuccess) {
                    // 记录日志，但不影响支付成功的结果
                    System.err.println("警告：支付成功但申请记录状态更新失败，申请ID: " + matchmakingRequest.getRequestId());
                }
            } else {
                // 记录日志，但不影响支付成功的结果
                System.err.println("警告：支付成功但未找到对应的申请记录，订单ID: " + order.getOrderId());
            }

            return Result.OK("支付成功，红娘将尽快为您提供服务");
        } catch (Exception e) {
            return Result.ERROR("支付失败：" + e.getMessage());
        }
    }

    /**
     * 处理支付逻辑
     * @param order 订单
     * @param payType 支付方式
     * @param userId 用户ID
     * @return 支付是否成功
     */
    private boolean processPayment(TbMatchmakerOrder order, Integer payType, Long userId) {
        try {
            if (payType == 3) { // 虚拟币支付
                // 使用虚拟币支付
                WalletDTO walletDTO = new WalletDTO();
                walletDTO.setUserId(userId);
                walletDTO.setCoinAmount(order.getPayAmount().intValue()); // 假设1元=1虚拟币
                walletDTO.setTransactionDesc("红娘牵线服务 - " + order.getOrderNo());
                walletDTO.setRelatedId("MATCHMAKER_ORDER_" + order.getOrderNo());

                Result consumeResult = walletService.consume(walletDTO);
                if (consumeResult.getCode() != 200) {
                    return false; // 支付失败
                }

                return true;
            } else {
                // 其他支付方式（微信、支付宝等）
                // 这里应该调用对应的支付接口
                // 暂时返回true模拟支付成功
                return true;
            }
        } catch (Exception e) {
            System.err.println("支付处理失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取订单状态文本
     */
    private String getOrderStatusText(Integer orderStatus) {
        if (orderStatus == null) return "未知";
        switch (orderStatus) {
            case 0: return "待支付";
            case 1: return "已支付";
            case 2: return "已取消";
            case 3: return "已退款";
            case 4: return "已完成";
            default: return "未知";
        }
    }
}
