package com.zhentao.vo;

import com.zhentao.pojo.TbUser;
import com.zhentao.pojo.TbUserProfile;
import lombok.Data;

/**
 * 用户资料VO，包含用户基本信息和详细资料
 *
 * @author zhentao
 */
@Data
public class ProfileVO {
    
    /**
     * 用户基本信息
     */
    private TbUser user;
    
    /**
     * 用户详细资料
     */
    private TbUserProfile profile;
} 