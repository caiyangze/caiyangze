package com.zhentao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhentao.pojo.TbMessage;
import com.zhentao.service.TbMessageService;
import com.zhentao.mapper.TbMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author a2634
* @description 针对表【tb_message】的数据库操作Service实现
* @createDate 2025-07-20 19:55:49
*/
@Service
public class TbMessageServiceImpl extends ServiceImpl<TbMessageMapper, TbMessage>
    implements TbMessageService{

}




