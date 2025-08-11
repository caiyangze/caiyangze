package com.zhentao.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 动态创建DTO
 * @author zhentao
 * @date 2025/7/21
 */
@Data
public class MomentCreateDTO {
    
    /**
     * 动态内容
     */
    @NotBlank(message = "动态内容不能为空")
    @Size(max = 1000, message = "动态内容不能超过1000字符")
    private String content;
    
    /**
     * 位置信息
     */
    @Size(max = 100, message = "位置信息不能超过100字符")
    private String location;
    
    /**
     * 可见范围：1-公开，2-仅关注者可见，3-仅自己可见
     */
    private Integer visibility = 1;
    
    /**
     * 媒体文件列表
     */
    private List<MultipartFile> mediaFiles;

    /**
     * 图片URL列表（用于前端传递已上传的图片URL）
     */
    private List<String> imageUrls;

    /**
     * 话题ID列表
     */
    private List<Long> topicIds;
}
