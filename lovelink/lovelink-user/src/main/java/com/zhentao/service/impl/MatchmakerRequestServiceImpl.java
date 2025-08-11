package com.zhentao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhentao.dto.MatchmakingRequestListDTO;
import com.zhentao.mapper.TbMatchmakingRequestMapper;
import com.zhentao.pojo.TbMatchmakingRequest;
import com.zhentao.service.MatchmakerRequestService;
import com.zhentao.utils.Result;
import com.zhentao.vo.MatchmakingRequestVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchmakerRequestServiceImpl implements MatchmakerRequestService {
    @Autowired
    private TbMatchmakingRequestMapper matchmakingRequestMapper;
    @Override
    public Page<MatchmakingRequestVO> getRequestList(MatchmakingRequestListDTO dto) {

        Page<TbMatchmakingRequest> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        //2.构建查询条件
        LambdaQueryWrapper<TbMatchmakingRequest> wrapper = new LambdaQueryWrapper<>();
        //按用户ID筛选（如果传了）
        if (dto.getUserId()!=null){
            wrapper.eq(TbMatchmakingRequest::getUserId, dto.getUserId());
        }
        //按申请状态筛选（如果传了）
        if (dto.getRequestStatus()!=null){
            wrapper.eq(TbMatchmakingRequest::getRequestStatus, dto.getRequestStatus());
        }
        //按创建时间倒序(小程序列表同通常需要最新的在前面)
        wrapper.orderByDesc(TbMatchmakingRequest::getCreatedAt);
        //3.执行分页查询
        Page<TbMatchmakingRequest> tbMatchmakingRequestPage = matchmakingRequestMapper.selectPage(page, wrapper);

        //4.实体传VO(避免暴露数据库字段，适配小程序端需求)
        List<MatchmakingRequestVO> voList = tbMatchmakingRequestPage.getRecords()
                .stream()
                .map(entity->{
                    MatchmakingRequestVO vo = new MatchmakingRequestVO();
                    BeanUtils.copyProperties(entity, vo);
                    //可额外处理字段（如状态翻译：0待处理，->前端展示更友好）
                    return vo;
                }).collect(Collectors.toList());
        //5.封装分页结果
        Page<MatchmakingRequestVO> result = new Page<>();
        result.setRecords(voList);
        result.setTotal(tbMatchmakingRequestPage.getTotal());
        result.setCurrent(tbMatchmakingRequestPage.getCurrent());
        result.setSize(tbMatchmakingRequestPage.getSize());
        return result;
    }
}
