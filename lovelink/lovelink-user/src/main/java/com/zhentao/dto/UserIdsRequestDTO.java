package com.zhentao.dto;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 用户ID列表请求DTO
 * 
 * @author zhentao
 */
public class UserIdsRequestDTO {
    
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;
    
    public UserIdsRequestDTO() {}
    
    public UserIdsRequestDTO(List<Long> userIds) {
        this.userIds = userIds;
    }
    
    public List<Long> getUserIds() {
        return userIds;
    }
    
    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }
    
    @Override
    public String toString() {
        return "UserIdsRequestDTO{" +
                "userIds=" + userIds +
                '}';
    }
}
