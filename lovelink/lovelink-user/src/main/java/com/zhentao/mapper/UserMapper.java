package com.zhentao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhentao.pojo.TbUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 * 
 * @author zhentao
 */
@Mapper
public interface UserMapper extends BaseMapper<TbUser> {
    
} 