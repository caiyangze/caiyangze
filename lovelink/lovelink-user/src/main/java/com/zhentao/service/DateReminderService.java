package com.zhentao.service;

import java.util.List;
import java.util.Map;

/**
 * 约会提醒服务接口
 */
public interface DateReminderService {

    /**
     * 获取用户的约会提醒列表
     * @param userId 用户ID
     * @return 提醒列表
     */
    List<Map<String, Object>> getUserReminders(Long userId);

    /**
     * 标记提醒为已读
     * @param reminderId 提醒ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAsRead(Long reminderId, Long userId);

    /**
     * 标记用户所有提醒为已读
     * @param userId 用户ID
     * @return 标记数量
     */
    int markAllAsRead(Long userId);

    /**
     * 获取用户未读提醒数量
     * @param userId 用户ID
     * @return 未读数量
     */
    int getUnreadCount(Long userId);

    /**
     * 创建约会提醒
     * @param userId 用户ID
     * @param arrangementId 约会安排ID
     * @param type 提醒类型
     * @param title 提醒标题
     * @param message 提醒消息
     * @param dateTime 约会时间
     * @return 是否成功
     */
    boolean createReminder(Long userId, Long arrangementId, String type, String title, String message, String dateTime);
}
