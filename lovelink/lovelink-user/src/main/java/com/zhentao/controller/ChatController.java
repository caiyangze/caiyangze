package com.zhentao.controller;

import com.zhentao.dto.ChatMessageDTO;
import com.zhentao.dto.UserIdsRequestDTO;
import com.zhentao.service.ChatService;
import com.zhentao.service.WebSocketSessionManager;
import com.zhentao.utils.Result;
import com.zhentao.utils.ThreadLocalUtil;
import com.zhentao.vo.ChatConversationVO;
import com.zhentao.vo.ChatMessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天控制器
 * 
 * @author zhentao
 */
@RestController
@RequestMapping("/chat")
@Validated
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private WebSocketSessionManager sessionManager;
    
    /**
     * 检查是否可以与指定用户聊天
     */
    @GetMapping("/canChat/{targetUserId}")
    public Result canChat(@PathVariable @NotNull Long targetUserId) {
        Long currentUserId = ThreadLocalUtil.getCurrentUserId();
        if (currentUserId == null) {
            return Result.ERROR("用户未登录");
        }
        boolean canChat = chatService.canChat(currentUserId, targetUserId);
        return Result.OK(canChat);
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    public Result getConversationList() {
        Long currentUserId = ThreadLocalUtil.getCurrentUserId();
        if (currentUserId == null) {
            return Result.ERROR("用户未登录");
        }
        List<ChatConversationVO> conversations = chatService.getConversationList(currentUserId);
        return Result.OK(conversations);
    }

    /**
     * 获取聊天记录
     */
    @GetMapping("/history/{conversationId}")
    public Result getChatHistory(
            @PathVariable @NotNull Long conversationId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Long currentUserId = ThreadLocalUtil.getCurrentUserId();
        if (currentUserId == null) {
            return Result.ERROR("用户未登录");
        }
        List<ChatMessageVO> messages = chatService.getChatHistory(conversationId, currentUserId, page, size);
        return Result.OK(messages);
    }

    /**
     * 发送消息（HTTP接口，主要用于文件上传等场景）
     */
    @PostMapping("/send")
    public Result sendMessage(@RequestBody @Valid ChatMessageDTO messageDTO) {
        try {
            Long currentUserId = ThreadLocalUtil.getCurrentUserId();
            if (currentUserId == null) {
                return Result.ERROR("用户未登录");
            }
            chatService.sendMessage(currentUserId, messageDTO);
            return Result.OK("消息发送成功");
        } catch (Exception e) {
            return Result.ERROR(e.getMessage());
        }
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/read")
    public Result markMessageAsRead(
            @RequestParam @NotNull Long conversationId,
            @RequestParam @NotNull Long messageId) {

        try {
            Long currentUserId = ThreadLocalUtil.getCurrentUserId();
            if (currentUserId == null) {
                return Result.ERROR("用户未登录");
            }
            chatService.markMessageAsRead(conversationId, currentUserId, messageId);
            return Result.OK("标记成功");
        } catch (Exception e) {
            return Result.ERROR(e.getMessage());
        }
    }

    /**
     * 撤回消息
     */
    @PostMapping("/recall/{messageId}")
    public Result recallMessage(@PathVariable @NotNull Long messageId) {
        try {
            Long currentUserId = ThreadLocalUtil.getCurrentUserId();
            if (currentUserId == null) {
                return Result.ERROR("用户未登录");
            }
            boolean success = chatService.recallMessage(messageId, currentUserId);
            if (success) {
                return Result.OK("撤回成功");
            } else {
                return Result.ERROR("撤回失败");
            }
        } catch (Exception e) {
            return Result.ERROR(e.getMessage());
        }
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/conversation/{conversationId}")
    public Result deleteConversation(@PathVariable @NotNull Long conversationId) {
        try {
            Long currentUserId = ThreadLocalUtil.getCurrentUserId();
            if (currentUserId == null) {
                return Result.ERROR("用户未登录");
            }
            boolean success = chatService.deleteConversation(conversationId, currentUserId);
            if (success) {
                return Result.OK("删除成功");
            } else {
                return Result.ERROR("删除失败");
            }
        } catch (Exception e) {
            return Result.ERROR(e.getMessage());
        }
    }

    /**
     * 获取在线状态
     */
    @GetMapping("/online/{userId}")
    public Result getOnlineStatus(@PathVariable @NotNull Long userId) {
        boolean isOnline = sessionManager.isUserOnline(userId);
        return Result.OK(isOnline);
    }

    /**
     * 获取在线用户统计
     */
    @GetMapping("/stats")
    public Result getOnlineStats() {
        int onlineUserCount = sessionManager.getOnlineUserCount();
        int totalConnectionCount = sessionManager.getTotalConnectionCount();

        return Result.OK(new Object() {
            public final int onlineUsers = onlineUserCount;
            public final int totalConnections = totalConnectionCount;
        });
    }

    /**
     * 批量获取用户在线状态
     */
    @PostMapping("/batchOnlineStatus")
    public Result getBatchOnlineStatus(@RequestBody @Valid UserIdsRequestDTO request) {
        try {
            System.out.println("收到批量在线状态请求: " + request);
            List<Long> userIds = request.getUserIds();
            System.out.println("用户ID列表: " + userIds);

            Map<Long, Object> result = new HashMap<>();
            for (Long userId : userIds) {
                boolean isOnline = sessionManager.isUserOnline(userId);
                Map<String, Object> status = new HashMap<>();
                status.put("isOnline", isOnline);
                status.put("lastSeen", isOnline ? null : System.currentTimeMillis());
                result.put(userId, status);
                System.out.println("用户 " + userId + " 在线状态: " + isOnline);
            }
            System.out.println("返回结果: " + result);
            return Result.OK(result);
        } catch (Exception e) {
            System.err.println("获取在线状态异常: " + e.getMessage());
            e.printStackTrace();
            return Result.ERROR("获取在线状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取未读消息数量
     */
    @PostMapping("/unreadCounts")
    public Result getUnreadCounts(@RequestBody @Valid UserIdsRequestDTO request) {
        try {
            System.out.println("收到未读消息数量请求: " + request);

            Long currentUserId = ThreadLocalUtil.getCurrentUserId();
            if (currentUserId == null) {
                System.err.println("用户未登录");
                return Result.ERROR("用户未登录");
            }

            System.out.println("当前用户ID: " + currentUserId);
            List<Long> userIds = request.getUserIds();
            System.out.println("查询用户ID列表: " + userIds);

            Map<Long, Integer> result = chatService.getUnreadCounts(currentUserId, userIds);
            System.out.println("未读消息数量结果: " + result);
            return Result.OK(result);
        } catch (Exception e) {
            System.err.println("获取未读消息数量异常: " + e.getMessage());
            e.printStackTrace();
            return Result.ERROR("获取未读消息数量失败: " + e.getMessage());
        }
    }

    /**
     * 测试接口 - 简单的POST请求测试
     */
    @PostMapping("/test")
    public Result testPost(@RequestBody Map<String, Object> data) {
        System.out.println("收到测试请求: " + data);
        return Result.OK("测试成功: " + data);
    }
}
