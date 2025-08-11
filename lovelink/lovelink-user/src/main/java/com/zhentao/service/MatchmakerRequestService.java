package com.zhentao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhentao.dto.MatchmakerRequestDto;
import com.zhentao.dto.MatchmakingRequestListDTO;

import com.zhentao.vo.MatchmakingRequestVO;

public interface MatchmakerRequestService {
    Page<MatchmakingRequestVO> getRequestList(MatchmakingRequestListDTO dto);
}
