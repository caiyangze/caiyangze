package com.zhentao.service;

import com.zhentao.pojo.TbMatchmaker;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
* @author cyz
* @description 针对表【tb_matchmaker】的数据库操作Service
* @createDate 2025-08-05 10:53:08
*/
public interface TbMatchmakerService extends IService<TbMatchmaker> {

    /**
     * 根据条件推荐红娘
     * @param userLocation 用户地区（可选，用于地区匹配）
     * @param preferredLevel 偏好的红娘等级（可选，1-预备，2-正式，3-金牌）
     * @param limit 推荐数量限制
     * @return 推荐的红娘列表
     */
    List<TbMatchmaker> findRecommendedMatchmakers(String userLocation, Integer preferredLevel, Integer limit);

    /**
     * 根据红娘ID获取红娘详细信息
     * @param matchmakerId 红娘ID
     * @return 红娘详细信息
     */
    TbMatchmaker getMatchmakerDetailById(Long matchmakerId);

    /**
     * 获取红娘对应的用户ID列表
     * @param matchmakerIds 红娘ID列表
     * @return 用户ID列表
     */
    List<Long> getUserIdsByMatchmakerIds(List<Long> matchmakerIds);

}
