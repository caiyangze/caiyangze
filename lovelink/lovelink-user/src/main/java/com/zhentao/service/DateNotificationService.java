package com.zhentao.service;

import com.zhentao.handler.ChatWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 约会通知服务
 * 
 * @author zhentao
 */
@Service
@Slf4j
public class DateNotificationService {

    @Autowired
    private ChatWebSocketHandler webSocketHandler;

    /**
     * 发送约会安排通知给双方用户
     * 
     * @param userAId 用户A的ID
     * @param userBId 用户B的ID
     * @param dateArrangement 约会安排信息
     */
    public void sendDateArrangementNotification(Long userAId, Long userBId, Object dateArrangement) {
        log.info("发送约会安排通知给用户 {} 和 {}", userAId, userBId);
        
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("type", "DATE_ARRANGEMENT");
        notificationData.put("message", "红娘为您安排了一场约会，请确认是否参加");
        notificationData.put("dateInfo", dateArrangement);
        notificationData.put("needConfirm", true);
        
        // 发送给用户A
        webSocketHandler.sendDateNotification(userAId, "DATE_ARRANGEMENT", notificationData);
        
        // 发送给用户B
        webSocketHandler.sendDateNotification(userBId, "DATE_ARRANGEMENT", notificationData);
        
        log.info("约会安排通知发送完成");
    }

    /**
     * 发送约会确认结果通知
     * 
     * @param userId 要通知的用户ID
     * @param otherUserId 另一方用户ID
     * @param confirmed 是否确认参加
     * @param dateArrangement 约会安排信息
     */
    public void sendDateConfirmationNotification(Long userId, Long otherUserId, boolean confirmed, Object dateArrangement) {
        log.info("发送约会确认结果通知给用户 {}: {}", userId, confirmed ? "已确认" : "已拒绝");
        
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("type", "DATE_CONFIRMATION");
        notificationData.put("otherUserId", otherUserId);
        notificationData.put("confirmed", confirmed);
        notificationData.put("dateInfo", dateArrangement);
        
        if (confirmed) {
            notificationData.put("message", "对方已确认参加约会");
        } else {
            notificationData.put("message", "对方拒绝了约会安排");
        }
        
        webSocketHandler.sendDateNotification(userId, "DATE_CONFIRMATION", notificationData);
    }

    /**
     * 发送约会最终确定通知（双方都确认后）
     * 
     * @param userAId 用户A的ID
     * @param userBId 用户B的ID
     * @param dateArrangement 约会安排信息
     */
    public void sendDateFinalizedNotification(Long userAId, Long userBId, Object dateArrangement) {
        log.info("发送约会最终确定通知给用户 {} 和 {}", userAId, userBId);
        
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("type", "DATE_FINALIZED");
        notificationData.put("message", "约会已确定！双方都已确认参加");
        notificationData.put("dateInfo", dateArrangement);
        notificationData.put("status", "CONFIRMED");
        
        // 发送给双方用户
        webSocketHandler.sendDateNotification(userAId, "DATE_FINALIZED", notificationData);
        webSocketHandler.sendDateNotification(userBId, "DATE_FINALIZED", notificationData);
        
        log.info("约会最终确定通知发送完成");
    }

    /**
     * 发送约会取消通知
     * 
     * @param userAId 用户A的ID
     * @param userBId 用户B的ID
     * @param reason 取消原因
     */
    public void sendDateCancelledNotification(Long userAId, Long userBId, String reason) {
        log.info("发送约会取消通知给用户 {} 和 {}: {}", userAId, userBId, reason);
        
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("type", "DATE_CANCELLED");
        notificationData.put("message", "约会已取消：" + reason);
        notificationData.put("reason", reason);
        notificationData.put("status", "CANCELLED");
        
        // 发送给双方用户
        webSocketHandler.sendDateNotification(userAId, "DATE_CANCELLED", notificationData);
        webSocketHandler.sendDateNotification(userBId, "DATE_CANCELLED", notificationData);
        
        log.info("约会取消通知发送完成");
    }

    /**
     * 发送约会提醒通知
     * 
     * @param userId 用户ID
     * @param dateArrangement 约会安排信息
     * @param reminderType 提醒类型（如：1天前、1小时前）
     */
    public void sendDateReminderNotification(Long userId, Object dateArrangement, String reminderType) {
        log.info("发送约会提醒通知给用户 {}: {}", userId, reminderType);
        
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("type", "DATE_REMINDER");
        notificationData.put("message", "约会提醒：您的约会即将开始（" + reminderType + "）");
        notificationData.put("dateInfo", dateArrangement);
        notificationData.put("reminderType", reminderType);
        
        webSocketHandler.sendDateNotification(userId, "DATE_REMINDER", notificationData);
    }

    /**
     * 发送反馈提醒通知
     *
     * @param userId 用户ID
     * @param dateArrangement 约会安排信息
     */
    public void sendFeedbackReminderNotification(Long userId, Object dateArrangement) {
        log.info("发送反馈提醒通知给用户 {}", userId);

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("type", "FEEDBACK_REMINDER");
        notificationData.put("message", "约会已结束，请为这次约会提供反馈评价");
        notificationData.put("dateInfo", dateArrangement);
        notificationData.put("action", "SUBMIT_FEEDBACK");

        webSocketHandler.sendDateNotification(userId, "FEEDBACK_REMINDER", notificationData);
    }

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(Long userId) {
        return webSocketHandler.isUserOnline(userId);
    }
}
