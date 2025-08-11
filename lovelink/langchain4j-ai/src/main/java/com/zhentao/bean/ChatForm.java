package com.zhentao.bean;

import lombok.Data;

@Data
public class ChatForm {
    private Long memoryId;//对话id
    private String message;//用户问题

    // 多模态支持字段
    private String messageType;//消息类型：text, voice, image
    private String voiceType;//语音类型（用于语音合成）
    private Boolean needVoiceResponse;//是否需要语音回复
    private String imageUrl;//图片URL（用于图片解析）
    private String audioBase64;//音频Base64（用于语音识别）
    private String imageDescription;//图片描述（用于文生图）

    // 用户信息字段（用于智能推荐）
    private Long userId;//用户ID
    private Integer userGender;//用户性别：0-女，1-男
    private Integer userRole;//用户角色：1-普通用户，2-红娘
    private Integer userAge;//用户年龄
    private String userLocation;//用户所在地区
    private String userInterests;//用户兴趣爱好
    private String recommendationType;//推荐类型：user-推荐异性用户, matchmaker-推荐红娘
}
