package com.zhentao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhentao.dto.ChatMessageDTO;
import com.zhentao.mapper.*;
import com.zhentao.pojo.*;
import com.zhentao.service.ChatService;
import com.zhentao.vo.ChatConversationVO;
import com.zhentao.vo.ChatMessageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天服务实现类
 * 
 * @author zhentao
 */
@Service
public class ChatServiceImpl implements ChatService {
    
    @Autowired
    private TbFollowMapper followMapper;
    
    @Autowired
    private TbChatConversationMapper conversationMapper;
    
    @Autowired
    private TbChatMessageMapper messageMapper;
    
    @Autowired
    private TbChatMessageStatusMapper messageStatusMapper;
    
    @Autowired
    private TbUserMapper userMapper;
    
    @Override
    public boolean canChat(Long userId1, Long userId2) {
        // 检查两个用户是否互相关注
        QueryWrapper<TbFollow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId1)
                   .eq("followed_user_id", userId2)
                   .eq("follow_status", 1);
        TbFollow follow1 = followMapper.selectOne(queryWrapper);
        
        queryWrapper.clear();
        queryWrapper.eq("user_id", userId2)
                   .eq("followed_user_id", userId1)
                   .eq("follow_status", 1);
        TbFollow follow2 = followMapper.selectOne(queryWrapper);
        
        return follow1 != null && follow2 != null;
    }
    
    @Override
    @Transactional
    public TbChatConversation getOrCreateConversation(Long userId1, Long userId2) {
        // 确保user1Id < user2Id
        Long user1Id = Math.min(userId1, userId2);
        Long user2Id = Math.max(userId1, userId2);
        
        // 查找现有会话
        QueryWrapper<TbChatConversation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user1_id", user1Id)
                   .eq("user2_id", user2Id);
        TbChatConversation conversation = conversationMapper.selectOne(queryWrapper);
        
        if (conversation == null) {
            // 创建新会话
            conversation = new TbChatConversation();
            conversation.setUser1Id(user1Id);
            conversation.setUser2Id(user2Id);
            conversation.setConversationType(1); // 私聊
            conversation.setUser1UnreadCount(0);
            conversation.setUser2UnreadCount(0);
            conversation.setUser1Deleted(0);
            conversation.setUser2Deleted(0);
            conversation.setStatus(1);
            conversation.setCreatedAt(new Date());
            conversation.setUpdatedAt(new Date());
            
            conversationMapper.insert(conversation);
        }
        
        return conversation;
    }
    
    @Override
    @Transactional
    public TbChatMessage sendMessage(Long senderId, ChatMessageDTO messageDTO) {
        // 检查是否可以聊天
        if (!canChat(senderId, messageDTO.getReceiverId())) {
            throw new RuntimeException("您和对方还不是好友，无法发送消息");
        }
        
        // 获取或创建会话
        TbChatConversation conversation = getOrCreateConversation(senderId, messageDTO.getReceiverId());
        
        // 创建消息
        TbChatMessage message = new TbChatMessage();
        BeanUtils.copyProperties(messageDTO, message);
        message.setConversationId(conversation.getConversationId());
        message.setSenderId(senderId);
        message.setIsRecalled(0);
        message.setIsDeleted(0);
        message.setCreatedAt(new Date());
        message.setUpdatedAt(new Date());
        
        messageMapper.insert(message);
        
        // 更新会话信息
        conversation.setLastMessageId(message.getMessageId());
        conversation.setLastMessageTime(new Date());
        conversation.setLastMessageContent(getMessagePreview(message));
        
        // 更新未读消息数
        if (senderId.equals(conversation.getUser1Id())) {
            conversation.setUser2UnreadCount(conversation.getUser2UnreadCount() + 1);
        } else {
            conversation.setUser1UnreadCount(conversation.getUser1UnreadCount() + 1);
        }
        
        conversation.setUpdatedAt(new Date());
        conversationMapper.updateById(conversation);
        
        // 创建消息状态记录
        TbChatMessageStatus messageStatus = new TbChatMessageStatus();
        messageStatus.setMessageId(message.getMessageId());
        messageStatus.setUserId(messageDTO.getReceiverId());
        messageStatus.setIsRead(0);
        messageStatus.setIsDelivered(0);
        messageStatus.setCreatedAt(new Date());
        messageStatus.setUpdatedAt(new Date());
        
        messageStatusMapper.insert(messageStatus);
        
        return message;
    }
    
    @Override
    public List<ChatConversationVO> getConversationList(Long userId) {
        // 查询用户的所有会话
        QueryWrapper<TbChatConversation> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper.eq("user1_id", userId).or().eq("user2_id", userId))
                   .orderByDesc("last_message_time");

        List<TbChatConversation> conversations = conversationMapper.selectList(queryWrapper);
        List<ChatConversationVO> result = new ArrayList<>();

        for (TbChatConversation conversation : conversations) {
            // 检查用户是否删除了会话
            boolean isDeleted = (userId.equals(conversation.getUser1Id()) && conversation.getUser1Deleted() == 1) ||
                               (userId.equals(conversation.getUser2Id()) && conversation.getUser2Deleted() == 1);
            if (isDeleted) {
                continue;
            }

            ChatConversationVO vo = new ChatConversationVO();
            vo.setConversationId(conversation.getConversationId());
            vo.setLastMessageId(conversation.getLastMessageId());
            vo.setLastMessageTime(conversation.getLastMessageTime());
            vo.setLastMessageContent(conversation.getLastMessageContent());
            vo.setStatus(conversation.getStatus());
            vo.setCreatedAt(conversation.getCreatedAt());

            // 确定对方用户ID
            Long otherUserId = userId.equals(conversation.getUser1Id()) ?
                              conversation.getUser2Id() : conversation.getUser1Id();
            vo.setOtherUserId(otherUserId);

            // 获取对方用户信息
            TbUser otherUser = userMapper.selectById(otherUserId);
            if (otherUser != null) {
                vo.setOtherUserNickname(otherUser.getNickname());
                vo.setOtherUserAvatar(otherUser.getAvatarUrl());
            }

            // 获取未读消息数
            Integer unreadCount = userId.equals(conversation.getUser1Id()) ?
                                 conversation.getUser1UnreadCount() : conversation.getUser2UnreadCount();
            vo.setUnreadCount(unreadCount);

            result.add(vo);
        }

        return result;
    }

    @Override
    public List<ChatMessageVO> getChatHistory(Long conversationId, Long userId, Integer page, Integer size) {
        // 验证用户是否有权限查看此会话
        TbChatConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null ||
            (!userId.equals(conversation.getUser1Id()) && !userId.equals(conversation.getUser2Id()))) {
            throw new RuntimeException("无权限查看此会话");
        }

        // 查询消息记录
        QueryWrapper<TbChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", conversationId)
                   .eq("is_deleted", 0)
                   .orderByDesc("created_at")
                   .last("LIMIT " + (page - 1) * size + ", " + size);

        List<TbChatMessage> messages = messageMapper.selectList(queryWrapper);
        List<ChatMessageVO> result = new ArrayList<>();

        for (TbChatMessage message : messages) {
            ChatMessageVO vo = new ChatMessageVO();
            BeanUtils.copyProperties(message, vo);

            // 获取发送者信息
            TbUser sender = userMapper.selectById(message.getSenderId());
            if (sender != null) {
                vo.setSenderNickname(sender.getNickname());
                vo.setSenderAvatar(sender.getAvatarUrl());
            }

            // 获取消息已读状态
            QueryWrapper<TbChatMessageStatus> statusQuery = new QueryWrapper<>();
            statusQuery.eq("message_id", message.getMessageId())
                      .eq("user_id", userId);
            TbChatMessageStatus status = messageStatusMapper.selectOne(statusQuery);
            vo.setIsRead(status != null ? status.getIsRead() : 0);

            result.add(vo);
        }

        return result;
    }

    @Override
    @Transactional
    public void markMessageAsRead(Long conversationId, Long userId, Long messageId) {
        // 更新消息状态为已读
        UpdateWrapper<TbChatMessageStatus> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("message_id", messageId)
                    .eq("user_id", userId)
                    .set("is_read", 1)
                    .set("read_time", new Date())
                    .set("updated_at", new Date());

        messageStatusMapper.update(null, updateWrapper);

        // 更新会话未读消息数
        TbChatConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation != null) {
            if (userId.equals(conversation.getUser1Id()) && conversation.getUser1UnreadCount() > 0) {
                conversation.setUser1UnreadCount(conversation.getUser1UnreadCount() - 1);
            } else if (userId.equals(conversation.getUser2Id()) && conversation.getUser2UnreadCount() > 0) {
                conversation.setUser2UnreadCount(conversation.getUser2UnreadCount() - 1);
            }
            conversation.setUpdatedAt(new Date());
            conversationMapper.updateById(conversation);
        }
    }

    @Override
    @Transactional
    public boolean recallMessage(Long messageId, Long userId) {
        TbChatMessage message = messageMapper.selectById(messageId);
        if (message == null || !userId.equals(message.getSenderId())) {
            return false;
        }

        // 检查是否超过撤回时间限制（2分钟）
        long timeDiff = System.currentTimeMillis() - message.getCreatedAt().getTime();
        if (timeDiff > 2 * 60 * 1000) {
            throw new RuntimeException("消息发送超过2分钟，无法撤回");
        }

        // 标记消息为已撤回
        message.setIsRecalled(1);
        message.setRecallTime(new Date());
        message.setUpdatedAt(new Date());

        return messageMapper.updateById(message) > 0;
    }

    @Override
    @Transactional
    public boolean deleteConversation(Long conversationId, Long userId) {
        TbChatConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            return false;
        }

        // 标记用户删除会话
        if (userId.equals(conversation.getUser1Id())) {
            conversation.setUser1Deleted(1);
            conversation.setUser1UnreadCount(0);
        } else if (userId.equals(conversation.getUser2Id())) {
            conversation.setUser2Deleted(1);
            conversation.setUser2UnreadCount(0);
        } else {
            return false;
        }

        conversation.setUpdatedAt(new Date());
        return conversationMapper.updateById(conversation) > 0;
    }

    /**
     * 获取消息预览内容
     */
    private String getMessagePreview(TbChatMessage message) {
        switch (message.getMessageType()) {
            case 1: // 文本
                return message.getContent().length() > 50 ?
                       message.getContent().substring(0, 50) + "..." :
                       message.getContent();
            case 2: // 图片
                return "[图片]";
            case 3: // 语音
                return "[语音]";
            case 4: // 视频
                return "[视频]";
            case 5: // 文件
                return "[文件]";
            case 6: // 系统消息
                return message.getContent();
            default:
                return "[未知消息]";
        }
    }

    @Override
    public Map<Long, Integer> getUnreadCounts(Long currentUserId, List<Long> userIds) {
        Map<Long, Integer> result = new HashMap<>();

        try {
            System.out.println("开始获取未读消息数量 - 当前用户: " + currentUserId + ", 查询用户: " + userIds);

            for (Long userId : userIds) {
                // 查找与该用户的会话
                QueryWrapper<TbChatConversation> conversationQuery = new QueryWrapper<>();
                conversationQuery.and(wrapper ->
                    wrapper.and(w -> w.eq("user1_id", currentUserId).eq("user2_id", userId))
                           .or(w -> w.eq("user1_id", userId).eq("user2_id", currentUserId))
                );

                TbChatConversation conversation = conversationMapper.selectOne(conversationQuery);
                System.out.println("用户 " + userId + " 的会话: " + (conversation != null ? "存在" : "不存在"));

                if (conversation != null) {
                    // 直接从会话表获取未读消息数量，更高效
                    int unreadCount = 0;
                    if (currentUserId.equals(conversation.getUser1Id())) {
                        unreadCount = conversation.getUser1UnreadCount() != null ? conversation.getUser1UnreadCount() : 0;
                        System.out.println("用户 " + userId + " 作为user2，当前用户未读数: " + unreadCount);
                    } else if (currentUserId.equals(conversation.getUser2Id())) {
                        unreadCount = conversation.getUser2UnreadCount() != null ? conversation.getUser2UnreadCount() : 0;
                        System.out.println("用户 " + userId + " 作为user1，当前用户未读数: " + unreadCount);
                    }

                    result.put(userId, unreadCount);
                } else {
                    System.out.println("用户 " + userId + " 没有会话，未读数设为0");
                    result.put(userId, 0);
                }
            }

            System.out.println("未读消息数量查询完成: " + result);
        } catch (Exception e) {
            System.err.println("获取未读消息数量异常: " + e.getMessage());
            e.printStackTrace();
            // 如果出错，返回所有用户未读数为0
            for (Long userId : userIds) {
                result.put(userId, 0);
            }
        }

        return result;
    }
}
