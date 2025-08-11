package com.zhentao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhentao.pojo.TbDateArrangement;
import com.zhentao.service.DateReminderService;
import com.zhentao.service.TbDateArrangementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 约会提醒服务实现
 */
@Service
public class DateReminderServiceImpl implements DateReminderService {

    @Autowired
    private TbDateArrangementService dateArrangementService;

    // 模拟提醒数据存储（实际项目中应该使用数据库表）
    private static final Map<Long, List<Map<String, Object>>> USER_REMINDERS = new HashMap<>();
    private static Long reminderIdCounter = 1L;

    @Override
    public List<Map<String, Object>> getUserReminders(Long userId) {
        // 获取用户的约会安排
        QueryWrapper<TbDateArrangement> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> 
            wrapper.eq("user_id", userId).or().eq("target_user_id", userId)
        );
        queryWrapper.orderByDesc("created_at");
        
        List<TbDateArrangement> arrangements = dateArrangementService.list(queryWrapper);
        
        List<Map<String, Object>> reminders = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (TbDateArrangement arrangement : arrangements) {
            LocalDateTime dateTime = arrangement.getDateTime().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
            
            // 根据约会状态和时间生成不同类型的提醒
            Map<String, Object> reminder = createReminderFromArrangement(arrangement, userId, now);
            if (reminder != null) {
                reminders.add(reminder);
            }
        }
        
        // 添加存储的自定义提醒
        List<Map<String, Object>> customReminders = USER_REMINDERS.getOrDefault(userId, new ArrayList<>());
        reminders.addAll(customReminders);
        
        // 按时间排序
        reminders.sort((a, b) -> {
            String dateTimeA = (String) a.get("dateTime");
            String dateTimeB = (String) b.get("dateTime");
            if (dateTimeA == null || dateTimeB == null) return 0;
            return dateTimeB.compareTo(dateTimeA); // 降序
        });
        
        return reminders;
    }

    @Override
    public boolean markAsRead(Long reminderId, Long userId) {
        List<Map<String, Object>> userReminders = USER_REMINDERS.getOrDefault(userId, new ArrayList<>());
        
        for (Map<String, Object> reminder : userReminders) {
            if (reminderId.equals(reminder.get("id"))) {
                reminder.put("isRead", true);
                return true;
            }
        }
        
        return false;
    }

    @Override
    public int markAllAsRead(Long userId) {
        List<Map<String, Object>> userReminders = USER_REMINDERS.getOrDefault(userId, new ArrayList<>());
        int count = 0;
        
        for (Map<String, Object> reminder : userReminders) {
            if (!Boolean.TRUE.equals(reminder.get("isRead"))) {
                reminder.put("isRead", true);
                count++;
            }
        }
        
        return count;
    }

    @Override
    public int getUnreadCount(Long userId) {
        List<Map<String, Object>> userReminders = USER_REMINDERS.getOrDefault(userId, new ArrayList<>());
        
        return (int) userReminders.stream()
            .filter(reminder -> !Boolean.TRUE.equals(reminder.get("isRead")))
            .count();
    }

    @Override
    public boolean createReminder(Long userId, Long arrangementId, String type, String title, String message, String dateTime) {
        Map<String, Object> reminder = new HashMap<>();
        reminder.put("id", reminderIdCounter++);
        reminder.put("arrangementId", arrangementId);
        reminder.put("type", type);
        reminder.put("title", title);
        reminder.put("message", message);
        reminder.put("dateTime", dateTime);
        reminder.put("isRead", false);
        reminder.put("isUrgent", false);
        reminder.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        USER_REMINDERS.computeIfAbsent(userId, k -> new ArrayList<>()).add(reminder);
        return true;
    }

    /**
     * 根据约会安排创建提醒
     */
    private Map<String, Object> createReminderFromArrangement(TbDateArrangement arrangement, Long userId, LocalDateTime now) {
        LocalDateTime dateTime = arrangement.getDateTime().toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDateTime();
        
        Map<String, Object> reminder = new HashMap<>();
        reminder.put("id", arrangement.getArrangementId());
        reminder.put("arrangementId", arrangement.getArrangementId());
        reminder.put("dateTime", dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        reminder.put("location", arrangement.getDateLocation());
        reminder.put("isRead", false);
        reminder.put("createdAt", arrangement.getCreatedAt().toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // 根据约会状态和时间确定提醒类型
        switch (arrangement.getArrangementStatus()) {
            case 0: // 待确认
                reminder.put("type", "CONFIRMATION_REMINDER");
                reminder.put("title", "约会待确认");
                reminder.put("message", "您有一个约会邀请待确认，请及时处理");
                reminder.put("isUrgent", true);
                break;
                
            case 1: // 已确认
                long hoursUntilDate = java.time.Duration.between(now, dateTime).toHours();
                
                if (hoursUntilDate <= 1 && hoursUntilDate > 0) {
                    reminder.put("type", "DATE_REMINDER");
                    reminder.put("title", "约会即将开始");
                    reminder.put("message", "您的约会将在1小时内开始，请做好准备");
                    reminder.put("isUrgent", true);
                } else if (hoursUntilDate <= 24 && hoursUntilDate > 1) {
                    reminder.put("type", "DATE_REMINDER");
                    reminder.put("title", "约会提醒");
                    reminder.put("message", "您明天有一个约会安排，请提前做好准备");
                    reminder.put("isUrgent", false);
                } else if (hoursUntilDate < 0 && hoursUntilDate > -72) {
                    // 约会已过去但未反馈
                    reminder.put("type", "FEEDBACK_REMINDER");
                    reminder.put("title", "约会反馈提醒");
                    reminder.put("message", "您的约会已结束，请为这次约会提供反馈评价");
                    reminder.put("isUrgent", false);
                } else {
                    return null; // 不需要提醒
                }
                break;
                
            case 2: // 已取消
                reminder.put("type", "CANCELLATION");
                reminder.put("title", "约会已取消");
                reminder.put("message", "约会已被取消：" + (arrangement.getCancelReason() != null ? arrangement.getCancelReason() : "未知原因"));
                reminder.put("status", "cancelled");
                break;
                
            case 3: // 已完成
                reminder.put("type", "HISTORY");
                reminder.put("title", "约会已完成");
                reminder.put("message", "约会已成功完成");
                reminder.put("status", "completed");
                break;
                
            default:
                return null;
        }
        
        return reminder;
    }
}
