package com.zhentao.store;

import com.zhentao.bean.ChatMessages;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class MongoChatMemoryStore implements ChatMemoryStore {

    @Autowired
   private MongoTemplate mongoTemplate;
    //获取聊天记忆
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);
        ChatMessages chatMessages= mongoTemplate.findOne(query, ChatMessages.class);
        if (chatMessages == null){
            return new LinkedList<>();
        }
        String contentJson = chatMessages.getContent();

        return ChatMessageDeserializer.messagesFromJson(contentJson);
    }
    //插入或更新聊天记忆
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);
        Update update = new Update();
        String s = ChatMessageSerializer.messagesToJson(list);
        update.set("content", s);

        //根据query条件能查询出文档，则修改文档；否则新增文档
        mongoTemplate.upsert(query, update, ChatMessages.class);
    }
    //删除聊天记忆
    @Override
    public void deleteMessages(Object memoryId) {
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);
        mongoTemplate.remove(query, ChatMessages.class);
    }
}
