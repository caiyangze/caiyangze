package com.zhentao.service;

import com.zhentao.dto.ChatMessageDTO;
import com.zhentao.pojo.TbChatConversation;
import com.zhentao.pojo.TbChatMessage;
import com.zhentao.vo.ChatConversationVO;
import com.zhentao.vo.ChatMessageVO;

import java.util.List;
import java.util.Map;

/**
 * 聊天服务接口
 * 
 * @author zhentao
 */
public interface ChatService {
    
    /**
     * 检查两个用户是否可以聊天（是否互相关注）
     * 
     * @param userId1 用户1ID
     * @param userId2 用户2ID
     * @return 是否可以聊天
     */
    boolean canChat(Long userId1, Long userId2);
    
    /**
     * 获取或创建会话
     * 
     * @param userId1 用户1ID
     * @param userId2 用户2ID
     * @return 会话信息
     */
    TbChatConversation getOrCreateConversation(Long userId1, Long userId2);
    
    /**
     * 发送消息
     * 
     * @param senderId 发送者ID
     * @param messageDTO 消息DTO
     * @return 消息信息
     */
    TbChatMessage sendMessage(Long senderId, ChatMessageDTO messageDTO);
    
    /**
     * 获取会话列表
     * 
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatConversationVO> getConversationList(Long userId);
    
    /**
     * 获取聊天记录
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 聊天记录
     */
    List<ChatMessageVO> getChatHistory(Long conversationId, Long userId, Integer page, Integer size);
    
    /**
     * 标记消息为已读
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param messageId 消息ID
     */
    void markMessageAsRead(Long conversationId, Long userId, Long messageId);
    
    /**
     * 撤回消息
     * 
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean recallMessage(Long messageId, Long userId);
    
    /**
     * 删除会话
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteConversation(Long conversationId, Long userId);

    /**
     * 获取未读消息数量
     *
     * @param currentUserId 当前用户ID
     * @param userIds 要查询的用户ID列表
     * @return 未读消息数量映射
     */
    Map<Long, Integer> getUnreadCounts(Long currentUserId, List<Long> userIds);
}
