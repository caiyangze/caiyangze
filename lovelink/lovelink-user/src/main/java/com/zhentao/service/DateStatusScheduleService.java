package com.zhentao.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhentao.pojo.TbDateArrangement;
import com.zhentao.service.TbDateArrangementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.zhentao.pojo.TbMatchmakerOrder;
import com.zhentao.pojo.TbMatchmakingRequest;
import com.zhentao.service.TbMatchmakerOrderService;
import com.zhentao.service.TbMatchmakingRequestService;
import com.zhentao.service.WalletService;
import com.zhentao.dto.WalletDTO;

/**
 * 约会状态自动流转定时任务服务
 */
@Service
@Slf4j
public class DateStatusScheduleService {

    @Autowired
    private TbDateArrangementService dateArrangementService;

    @Autowired
    private DateNotificationService dateNotificationService;

    @Autowired
    private DateReminderService dateReminderService;

    @Autowired
    private TbMatchmakerOrderService matchmakerOrderService;

    @Autowired
    private TbMatchmakingRequestService matchmakingRequestService;

    @Autowired
    private WalletService walletService;

    // 记录已发送的提醒，避免重复发送 (格式: arrangementId_reminderType)
    private static final Set<String> SENT_REMINDERS = new HashSet<>();

    // 记录已处理的退款，避免重复退款 (格式: orderId)
    private static final Set<Long> PROCESSED_REFUNDS = new HashSet<>();

    /**
     * 每小时检查约会状态，发送提醒通知
     * 每小时的第0分钟执行
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkDateReminders() {
        log.info("开始检查约会提醒通知...");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tomorrow = now.plusDays(1);
            LocalDateTime oneHourLater = now.plusHours(1);

            // 查询已确认的约会
            QueryWrapper<TbDateArrangement> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("arrangement_status", 1); // 已确认状态
            List<TbDateArrangement> confirmedDates = dateArrangementService.list(queryWrapper);

            for (TbDateArrangement arrangement : confirmedDates) {
                LocalDateTime dateTime = arrangement.getDateTime().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();

                // 检查是否需要发送24小时提醒
                if (isWithinTimeRange(dateTime, tomorrow.minusMinutes(30), tomorrow.plusMinutes(30))) {
                    String reminderKey = arrangement.getArrangementId() + "_24h";
                    if (!SENT_REMINDERS.contains(reminderKey)) {
                        sendDateReminder(arrangement, "24小时前");
                        createReminderRecord(arrangement, "24小时前");
                        SENT_REMINDERS.add(reminderKey);
                        log.info("已发送24小时提醒 - 约会ID: {}", arrangement.getArrangementId());
                    }
                }

                // 检查是否需要发送1小时提醒
                if (isWithinTimeRange(dateTime, oneHourLater.minusMinutes(15), oneHourLater.plusMinutes(15))) {
                    String reminderKey = arrangement.getArrangementId() + "_1h";
                    if (!SENT_REMINDERS.contains(reminderKey)) {
                        sendDateReminder(arrangement, "1小时前");
                        createReminderRecord(arrangement, "1小时前");
                        SENT_REMINDERS.add(reminderKey);
                        log.info("已发送1小时提醒 - 约会ID: {}", arrangement.getArrangementId());
                    }
                }

                // 检查约会是否已过期但未完成
                if (dateTime.isBefore(now.minusHours(3)) && arrangement.getArrangementStatus() == 1) {
                    String feedbackKey = arrangement.getArrangementId() + "_feedback";
                    if (!SENT_REMINDERS.contains(feedbackKey)) {
                        // 约会时间过去3小时后，提醒用户提交反馈
                        sendFeedbackReminder(arrangement);
                        SENT_REMINDERS.add(feedbackKey);
                        log.info("已发送反馈提醒 - 约会ID: {}", arrangement.getArrangementId());
                    }
                }

                // 检查约会是否已过期很久，自动标记为已完成
                if (dateTime.isBefore(now.minusDays(7)) && arrangement.getArrangementStatus() == 1) {
                    arrangement.setArrangementStatus(3); // 标记为已完成
                    arrangement.setUpdatedAt(new Date());
                    dateArrangementService.updateById(arrangement);
                    log.info("约会 {} 已自动标记为完成（超过7天未处理）", arrangement.getArrangementId());
                }
            }

        } catch (Exception e) {
            log.error("检查约会提醒时发生错误", e);
        }
    }

    /**
     * 每天凌晨2点清理过期的约会数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredDates() {
        log.info("开始清理过期约会数据...");
        
        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            Date cutoffDate = Date.from(sevenDaysAgo.atZone(ZoneId.systemDefault()).toInstant());

            // 查询7天前已完成或已取消的约会
            QueryWrapper<TbDateArrangement> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("arrangement_status", 2, 3) // 已取消或已完成
                       .lt("updated_at", cutoffDate);

            List<TbDateArrangement> expiredDates = dateArrangementService.list(queryWrapper);
            
            if (!expiredDates.isEmpty()) {
                log.info("找到 {} 条过期约会记录", expiredDates.size());
                // 这里可以添加数据归档逻辑，而不是直接删除
                // 例如：移动到历史表或标记为已归档
            }

        } catch (Exception e) {
            log.error("清理过期约会数据时发生错误", e);
        }
    }

    /**
     * 每15分钟检查约会状态更新
     */
    @Scheduled(fixedRate = 900000) // 15分钟 = 900000毫秒
    public void checkDateStatusUpdates() {
        log.debug("检查约会状态更新...");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 查询待确认的约会，检查是否超时
            QueryWrapper<TbDateArrangement> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("arrangement_status", 0); // 待确认状态
            List<TbDateArrangement> pendingDates = dateArrangementService.list(queryWrapper);

            for (TbDateArrangement arrangement : pendingDates) {
                LocalDateTime createdTime = arrangement.getCreatedAt().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
                
                // 如果超过48小时未确认，自动取消
                if (createdTime.isBefore(now.minusHours(48))) {
                    arrangement.setArrangementStatus(2); // 设置为已取消
                    arrangement.setCancelReason("超时未确认，系统自动取消");
                    arrangement.setUpdatedAt(new Date());
                    
                    dateArrangementService.updateById(arrangement);
                    
                    // 发送取消通知
                    dateNotificationService.sendDateCancelledNotification(
                        arrangement.getUserId(),
                        arrangement.getTargetUserId(),
                        arrangement.getCancelReason()
                    );
                    
                    log.info("约会安排 {} 因超时未确认已自动取消", arrangement.getArrangementId());
                }
            }

        } catch (Exception e) {
            log.error("检查约会状态更新时发生错误", e);
        }
    }

    /**
     * 发送约会提醒通知
     */
    private void sendDateReminder(TbDateArrangement arrangement, String reminderType) {
        try {
            // 发送给双方用户
            dateNotificationService.sendDateReminderNotification(
                arrangement.getUserId(), arrangement, reminderType);
            dateNotificationService.sendDateReminderNotification(
                arrangement.getTargetUserId(), arrangement, reminderType);
            
            log.info("已发送约会提醒通知 - 约会ID: {}, 提醒类型: {}", 
                arrangement.getArrangementId(), reminderType);
                
        } catch (Exception e) {
            log.error("发送约会提醒通知失败 - 约会ID: {}", arrangement.getArrangementId(), e);
        }
    }

    /**
     * 发送反馈提醒通知
     */
    private void sendFeedbackReminder(TbDateArrangement arrangement) {
        try {
            // 发送反馈提醒给双方用户
            dateNotificationService.sendFeedbackReminderNotification(
                arrangement.getUserId(), arrangement);
            dateNotificationService.sendFeedbackReminderNotification(
                arrangement.getTargetUserId(), arrangement);
            
            log.info("已发送反馈提醒通知 - 约会ID: {}", arrangement.getArrangementId());
                
        } catch (Exception e) {
            log.error("发送反馈提醒通知失败 - 约会ID: {}", arrangement.getArrangementId(), e);
        }
    }

    /**
     * 创建提醒记录
     */
    private void createReminderRecord(TbDateArrangement arrangement, String reminderType) {
        try {
            String title = "约会提醒";
            String message = "您的约会即将开始（" + reminderType + "）";
            String dateTime = arrangement.getDateTime().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime().toString();

            // 为双方用户创建提醒记录
            dateReminderService.createReminder(
                arrangement.getUserId(),
                arrangement.getArrangementId(),
                "DATE_REMINDER",
                title,
                message,
                dateTime
            );

            dateReminderService.createReminder(
                arrangement.getTargetUserId(),
                arrangement.getArrangementId(),
                "DATE_REMINDER",
                title,
                message,
                dateTime
            );

        } catch (Exception e) {
            log.error("创建提醒记录失败 - 约会ID: {}", arrangement.getArrangementId(), e);
        }
    }

    /**
     * 每小时检查需要退款的订单
     */
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void checkRefundableOrders() {
        log.info("开始检查需要退款的订单...");

        try {
            // 1. 检查红娘超时未处理的订单（48小时全额退款）
            checkMatchmakerTimeoutRefunds();

            // 2. 检查红娘拒绝的订单（全额退款）
            checkMatchmakerRejectionRefunds();

            // 3. 检查目标用户拒绝的订单（部分退款）
            checkTargetUserRejectionRefunds();

            // 4. 检查成功匹配的订单（红娘收入分配）
            checkSuccessfulMatchOrders();

        } catch (Exception e) {
            log.error("检查退款订单失败", e);
        }
    }

    /**
     * 检查红娘超时未处理的退款
     */
    private void checkMatchmakerTimeoutRefunds() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime refundDeadline = now.minusHours(48); // 48小时前
        Date refundDeadlineDate = Date.from(refundDeadline.atZone(ZoneId.systemDefault()).toInstant());

        // 查询需要退款的订单：已支付但红娘超过48小时未处理
        QueryWrapper<TbMatchmakerOrder> orderQuery = new QueryWrapper<>();
        orderQuery.eq("order_status", 1) // 已支付
                 .lt("created_at", refundDeadlineDate); // 创建时间超过48小时

        List<TbMatchmakerOrder> refundableOrders = matchmakerOrderService.list(orderQuery);

        for (TbMatchmakerOrder order : refundableOrders) {
            // 检查是否已经处理过退款
            if (PROCESSED_REFUNDS.contains(order.getOrderId())) {
                continue;
            }

            try {
                // 检查对应的牵线申请状态
                QueryWrapper<TbMatchmakingRequest> requestQuery = new QueryWrapper<>();
                requestQuery.eq("user_id", order.getUserId())
                           .eq("request_status", 0) // 仍在待处理状态
                           .ge("created_at", order.getCreatedAt()) // 订单创建后的申请
                           .orderByDesc("created_at") // 按创建时间倒序
                           .last("LIMIT 1"); // 限制只返回一条记录

                List<TbMatchmakingRequest> requests = matchmakingRequestService.list(requestQuery);

                if (!requests.isEmpty()) {
                    TbMatchmakingRequest request = requests.get(0); // 获取最新的申请
                    // 执行全额退款
                    processFullRefund(order, request, "红娘超时未处理");
                    PROCESSED_REFUNDS.add(order.getOrderId());
                }

            } catch (Exception e) {
                log.error("处理红娘超时退款失败 - 订单ID: {}", order.getOrderId(), e);
                continue; // 继续处理下一个订单
            }
        }

        log.info("红娘超时退款检查完成，处理了 {} 个订单", refundableOrders.size());
    }

    /**
     * 检查红娘拒绝的退款
     */
    private void checkMatchmakerRejectionRefunds() {
        log.info("开始检查红娘拒绝的退款...");

        // 查询红娘拒绝的申请对应的订单
        QueryWrapper<TbMatchmakingRequest> requestQuery = new QueryWrapper<>();
        requestQuery.eq("request_status", 2); // 红娘已拒绝

        List<TbMatchmakingRequest> rejectedRequests = matchmakingRequestService.list(requestQuery);
        log.info("找到 {} 个红娘拒绝的申请", rejectedRequests.size());

        for (TbMatchmakingRequest request : rejectedRequests) {
            try {
                log.info("处理申请ID: {}, 用户ID: {}, 申请状态: {}",
                    request.getRequestId(), request.getUserId(), request.getRequestStatus());

                // 查找对应的订单 - 使用时间范围匹配
                // 查找申请前24小时内的已支付订单
                Date requestTime = request.getCreatedAt();
                Date orderTimeStart = new Date(requestTime.getTime() - 24 * 60 * 60 * 1000); // 24小时前

                QueryWrapper<TbMatchmakerOrder> orderQuery = new QueryWrapper<>();
                orderQuery.eq("user_id", request.getUserId())
                         .eq("order_status", 1) // 已支付状态
                         .between("created_at", orderTimeStart, requestTime) // 订单时间在申请前24小时内
                         .orderByDesc("created_at") // 按创建时间倒序，获取最新的订单
                         .last("LIMIT 1"); // 限制只返回一条记录

                List<TbMatchmakerOrder> orders = matchmakerOrderService.list(orderQuery);
                log.info("为申请ID {} 找到 {} 个匹配的订单", request.getRequestId(), orders.size());

                if (!orders.isEmpty()) {
                    TbMatchmakerOrder order = orders.get(0); // 获取最新的订单
                    log.info("找到订单ID: {}, 状态: {}, 金额: {}",
                        order.getOrderId(), order.getOrderStatus(),
                        order.getPayAmount() != null ? order.getPayAmount() : order.getAmount());

                    if (!PROCESSED_REFUNDS.contains(order.getOrderId())) {
                        log.info("开始处理订单 {} 的全额退款", order.getOrderId());
                        // 红娘拒绝：全额退款（红娘没有提供服务）
                        processFullRefund(order, request, "红娘拒绝申请");
                        PROCESSED_REFUNDS.add(order.getOrderId());
                        log.info("订单 {} 全额退款处理完成", order.getOrderId());
                    } else {
                        log.info("订单 {} 已经处理过退款，跳过", order.getOrderId());
                    }
                } else {
                    log.warn("申请ID {} 没有找到匹配的已支付订单", request.getRequestId());
                }

            } catch (Exception e) {
                log.error("处理红娘拒绝退款失败 - 申请ID: {}", request.getRequestId(), e);
                continue; // 继续处理下一个申请
            }
        }

        log.info("红娘拒绝退款检查完成，共处理 {} 个申请", rejectedRequests.size());
    }

    /**
     * 检查目标用户拒绝的退款
     */
    private void checkTargetUserRejectionRefunds() {
        log.info("开始检查目标用户拒绝的退款...");

        // 查询目标用户拒绝的申请对应的订单
        QueryWrapper<TbMatchmakingRequest> requestQuery = new QueryWrapper<>();
        requestQuery.eq("request_status", 3); // 目标用户已拒绝

        List<TbMatchmakingRequest> rejectedRequests = matchmakingRequestService.list(requestQuery);
        log.info("找到 {} 个目标用户拒绝的申请", rejectedRequests.size());

        for (TbMatchmakingRequest request : rejectedRequests) {
            try {
                log.info("处理申请ID: {}, 用户ID: {}, 申请状态: {}",
                    request.getRequestId(), request.getUserId(), request.getRequestStatus());

                // 查找对应的订单 - 使用时间范围匹配
                // 查找申请前24小时内的已支付订单
                Date requestTime = request.getCreatedAt();
                Date orderTimeStart = new Date(requestTime.getTime() - 24 * 60 * 60 * 1000); // 24小时前

                QueryWrapper<TbMatchmakerOrder> orderQuery = new QueryWrapper<>();
                orderQuery.eq("user_id", request.getUserId())
                         .eq("order_status", 1) // 已支付状态
                         .between("created_at", orderTimeStart, requestTime) // 订单时间在申请前24小时内
                         .orderByDesc("created_at") // 按创建时间倒序，获取最新的订单
                         .last("LIMIT 1"); // 限制只返回一条记录

                List<TbMatchmakerOrder> orders = matchmakerOrderService.list(orderQuery);
                log.info("为申请ID {} 找到 {} 个匹配的订单", request.getRequestId(), orders.size());

                if (!orders.isEmpty()) {
                    TbMatchmakerOrder order = orders.get(0); // 获取最新的订单
                    log.info("找到订单ID: {}, 状态: {}, 金额: {}",
                        order.getOrderId(), order.getOrderStatus(),
                        order.getPayAmount() != null ? order.getPayAmount() : order.getAmount());

                    if (!PROCESSED_REFUNDS.contains(order.getOrderId())) {
                        log.info("开始处理订单 {} 的部分退款", order.getOrderId());
                        // 执行部分退款（70%退给用户，30%给红娘作为服务费）
                        processPartialRefund(order, request, "目标用户拒绝");
                        PROCESSED_REFUNDS.add(order.getOrderId());
                        log.info("订单 {} 部分退款处理完成", order.getOrderId());
                    } else {
                        log.info("订单 {} 已经处理过退款，跳过", order.getOrderId());
                    }
                } else {
                    log.warn("申请ID {} 没有找到匹配的已支付订单", request.getRequestId());
                }

            } catch (Exception e) {
                log.error("处理目标用户拒绝退款失败 - 申请ID: {}", request.getRequestId(), e);
                continue; // 继续处理下一个申请
            }
        }

        log.info("目标用户拒绝退款检查完成，共处理 {} 个申请", rejectedRequests.size());
    }

    /**
     * 检查成功匹配的订单（红娘收入分配）
     */
    private void checkSuccessfulMatchOrders() {
        log.info("开始检查成功匹配的订单...");

        // 查询成功匹配的申请对应的订单
        QueryWrapper<TbMatchmakingRequest> requestQuery = new QueryWrapper<>();
        requestQuery.eq("request_status", 4); // 4-已成功匹配

        List<TbMatchmakingRequest> successfulRequests = matchmakingRequestService.list(requestQuery);
        log.info("找到 {} 个成功匹配的申请", successfulRequests.size());

        for (TbMatchmakingRequest request : successfulRequests) {
            try {
                log.info("处理申请ID: {}, 用户ID: {}, 申请状态: {}",
                    request.getRequestId(), request.getUserId(), request.getRequestStatus());

                // 查找对应的订单
                Date requestTime = request.getCreatedAt();
                Date orderTimeStart = new Date(requestTime.getTime() - 24 * 60 * 60 * 1000); // 24小时前

                QueryWrapper<TbMatchmakerOrder> orderQuery = new QueryWrapper<>();
                orderQuery.eq("user_id", request.getUserId())
                         .eq("order_status", 1) // 已支付状态
                         .between("created_at", orderTimeStart, requestTime)
                         .orderByDesc("created_at")
                         .last("LIMIT 1");

                List<TbMatchmakerOrder> orders = matchmakerOrderService.list(orderQuery);
                log.info("为申请ID {} 找到 {} 个匹配的订单", request.getRequestId(), orders.size());

                if (!orders.isEmpty()) {
                    TbMatchmakerOrder order = orders.get(0);

                    if (!PROCESSED_REFUNDS.contains(order.getOrderId())) {
                        log.info("开始处理订单 {} 的成功匹配收入分配", order.getOrderId());
                        processSuccessfulMatchIncome(order, request);
                        PROCESSED_REFUNDS.add(order.getOrderId());
                        log.info("订单 {} 成功匹配收入分配完成", order.getOrderId());
                    } else {
                        log.info("订单 {} 已经处理过，跳过", order.getOrderId());
                    }
                } else {
                    log.warn("申请ID {} 没有找到匹配的已支付订单", request.getRequestId());
                }

            } catch (Exception e) {
                log.error("处理成功匹配收入分配失败 - 申请ID: {}", request.getRequestId(), e);
                continue;
            }
        }

        log.info("成功匹配收入分配检查完成，共处理 {} 个申请", successfulRequests.size());
    }

    /**
     * 处理成功匹配的收入分配
     */
    private void processSuccessfulMatchIncome(TbMatchmakerOrder order, TbMatchmakingRequest request) {
        try {
            // 1. 更新订单状态为已完成
            order.setOrderStatus(4); // 4-已完成
            order.setServiceEndTime(new Date());
            order.setUpdatedAt(new Date());
            matchmakerOrderService.updateById(order);

            // 2. 成功匹配时的分配：红娘收入70% + 平台收益30%
            if (walletService != null && request.getMatchmakerId() != null) {
                BigDecimal originalAmount = order.getPayAmount() != null ? order.getPayAmount() : order.getAmount();
                BigDecimal matchmakerIncome = originalAmount.multiply(new BigDecimal("0.7")); // 70%给红娘
                BigDecimal platformIncome = originalAmount.multiply(new BigDecimal("0.3")); // 30%平台收益

                // 2.1 给红娘发放70%收入
                WalletDTO matchmakerIncomeDTO = new WalletDTO();
                matchmakerIncomeDTO.setUserId(request.getMatchmakerId());
                matchmakerIncomeDTO.setCoinAmount(matchmakerIncome.intValue());
                matchmakerIncomeDTO.setTransactionDesc("红娘服务收入（成功匹配）");
                matchmakerIncomeDTO.setRelatedId("ORDER_" + order.getOrderId());

                walletService.recharge(matchmakerIncomeDTO);
                log.info("红娘成功匹配收入发放完成 - 红娘ID: {}, 收入金额: {}币",
                    request.getMatchmakerId(), matchmakerIncome.intValue());

                // 2.2 平台收益30%（记录到系统账户或统计表）
                log.info("平台收益 - 订单ID: {}, 收益金额: {}币 (成功匹配)",
                    order.getOrderId(), platformIncome.intValue());
                // TODO: 可以将平台收益记录到专门的平台收益表或系统账户
            }

            log.info("成功匹配收入分配完成 - 订单ID: {}, 红娘收入: {}币, 平台收益: {}币",
                order.getOrderId(),
                (order.getPayAmount() != null ? order.getPayAmount() : order.getAmount()).multiply(new BigDecimal("0.7")).intValue(),
                (order.getPayAmount() != null ? order.getPayAmount() : order.getAmount()).multiply(new BigDecimal("0.3")).intValue());

        } catch (Exception e) {
            log.error("处理成功匹配收入分配失败 - 订单ID: {}", order.getOrderId(), e);
        }
    }

    /**
     * 处理全额退款（红娘超时未处理）
     */
    private void processFullRefund(TbMatchmakerOrder order, TbMatchmakingRequest request, String reason) {
        try {
            // 1. 更新订单状态为已退款
            order.setOrderStatus(3); // 已退款
            order.setServiceEndTime(new Date()); // 使用服务结束时间记录退款时间
            order.setUpdatedAt(new Date());
            matchmakerOrderService.updateById(order);

            // 2. 更新牵线申请状态为已取消
            request.setRequestStatus(6); // 已取消
            request.setRejectReason(reason);
            request.setUpdatedAt(new Date());
            matchmakingRequestService.updateById(request);

            // 3. 全额退款到用户钱包（虚拟币）
            if (walletService != null) {
                BigDecimal refundAmount = order.getPayAmount() != null ? order.getPayAmount() : order.getAmount();

                WalletDTO walletDTO = new WalletDTO();
                walletDTO.setUserId(order.getUserId());
                walletDTO.setCoinAmount(refundAmount.intValue()); // 虚拟币退款
                walletDTO.setTransactionDesc("红娘服务全额退款（" + reason + "）");
                walletDTO.setRelatedId("ORDER_" + order.getOrderId());

                walletService.recharge(walletDTO);
                log.info("虚拟币全额退款完成 - 订单ID: {}, 退款金额: {}币", order.getOrderId(), refundAmount.intValue());
            }

            log.info("全额退款完成 - 订单ID: {}, 金额: {}, 原因: {}",
                order.getOrderId(),
                order.getPayAmount() != null ? order.getPayAmount() : order.getAmount(),
                reason);

        } catch (Exception e) {
            log.error("处理全额退款失败 - 订单ID: {}", order.getOrderId(), e);
        }
    }

    /**
     * 处理部分退款（目标用户拒绝）
     */
    private void processPartialRefund(TbMatchmakerOrder order, TbMatchmakingRequest request, String reason) {
        try {
            // 1. 更新订单状态为已退款
            order.setOrderStatus(3); // 已退款
            order.setServiceEndTime(new Date());
            order.setUpdatedAt(new Date());
            matchmakerOrderService.updateById(order);

            // 2. 更新牵线申请状态为已取消
            request.setRequestStatus(6); // 已取消
            request.setRejectReason(reason + "，已部分退款");
            request.setUpdatedAt(new Date());
            matchmakingRequestService.updateById(request);

            // 3. 目标用户拒绝时的分配：用户退款50% + 红娘收入30% + 平台收益20%
            if (walletService != null) {
                BigDecimal originalAmount = order.getPayAmount() != null ? order.getPayAmount() : order.getAmount();
                BigDecimal userRefund = originalAmount.multiply(new BigDecimal("0.5")); // 50%退款给用户
                BigDecimal matchmakerIncome = originalAmount.multiply(new BigDecimal("0.3")); // 30%给红娘
                BigDecimal platformIncome = originalAmount.multiply(new BigDecimal("0.2")); // 20%平台收益

                // 3.1 给用户退款50%
                WalletDTO userRefundDTO = new WalletDTO();
                userRefundDTO.setUserId(order.getUserId());
                userRefundDTO.setCoinAmount(userRefund.intValue());
                userRefundDTO.setTransactionDesc("红娘服务部分退款（目标用户拒绝）");
                userRefundDTO.setRelatedId("ORDER_" + order.getOrderId());

                walletService.recharge(userRefundDTO);
                log.info("用户部分退款完成 - 订单ID: {}, 退款金额: {}币",
                    order.getOrderId(), userRefund.intValue());

                // 3.2 给红娘发放30%收入
                if (request.getMatchmakerId() != null) {
                    WalletDTO matchmakerIncomeDTO = new WalletDTO();
                    matchmakerIncomeDTO.setUserId(request.getMatchmakerId());
                    matchmakerIncomeDTO.setCoinAmount(matchmakerIncome.intValue());
                    matchmakerIncomeDTO.setTransactionDesc("红娘服务收入（目标用户拒绝）");
                    matchmakerIncomeDTO.setRelatedId("ORDER_" + order.getOrderId());

                    walletService.recharge(matchmakerIncomeDTO);
                    log.info("红娘收入发放完成 - 红娘ID: {}, 收入金额: {}币",
                        request.getMatchmakerId(), matchmakerIncome.intValue());
                }

                // 3.3 平台收益20%（记录到系统账户或统计表）
                log.info("平台收益 - 订单ID: {}, 收益金额: {}币 (目标用户拒绝)",
                    order.getOrderId(), platformIncome.intValue());
                // TODO: 可以将平台收益记录到专门的平台收益表或系统账户
            }

            log.info("部分退款完成 - 订单ID: {}, 原金额: {}, 退款金额: {}, 原因: {}",
                order.getOrderId(),
                order.getPayAmount() != null ? order.getPayAmount() : order.getAmount(),
                (order.getPayAmount() != null ? order.getPayAmount() : order.getAmount()).multiply(new BigDecimal("0.7")),
                reason);

        } catch (Exception e) {
            log.error("处理部分退款失败 - 订单ID: {}", order.getOrderId(), e);
        }
    }

    /**
     * 检查时间是否在指定范围内
     */
    private boolean isWithinTimeRange(LocalDateTime target, LocalDateTime start, LocalDateTime end) {
        return target.isAfter(start) && target.isBefore(end);
    }
}
