package com.zhentao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhentao.pojo.TbUserProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户资料Mapper接口
 *
 * @author zhentao
 */
@Mapper
public interface ProfileMapper extends BaseMapper<TbUserProfile> {
    // 继承BaseMapper后，已经有基本的CRUD方法，无需额外定义
} 