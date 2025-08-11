package com.zhentao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhentao.pojo.TbUser;
import com.zhentao.service.TbUserService;
import com.zhentao.mapper.TbUserMapper;
import org.springframework.stereotype.Service;

/**
* @author a2634
* @description 针对表【tb_user】的数据库操作Service实现
* @createDate 2025-07-20 19:55:50
*/
@Service
public class TbUserServiceImpl extends ServiceImpl<TbUserMapper, TbUser>
    implements TbUserService{

}




