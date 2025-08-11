package com.zhentao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhentao.pojo.TbMatchmaker;
import com.zhentao.service.TbMatchmakerService;
import com.zhentao.mapper.TbMatchmakerMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author cyz
* @description 针对表【tb_matchmaker】的数据库操作Service实现 - AI模块专用
* @createDate 2025-08-05 10:53:08
*/
@Service("aiMatchmakerService")
public class TbMatchmakerServiceImpl extends ServiceImpl<TbMatchmakerMapper, TbMatchmaker>
    implements TbMatchmakerService{

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TbMatchmakerServiceImpl.class);

    @Override
    public List<TbMatchmaker> findRecommendedMatchmakers(String userLocation, Integer preferredLevel, Integer limit) {
        try {
            log.info("AI模块推荐红娘 - 推荐所有优质红娘，限制数量：{}", limit);

            QueryWrapper<TbMatchmaker> queryWrapper = new QueryWrapper<>();

            // 只查询正常状态的红娘（这是唯一的硬性条件）
            queryWrapper.eq("matchmaker_status", 1);

            // 不限制地区 - 推荐全平台所有红娘
            // 不限制等级 - 推荐所有等级的红娘
            log.info("推荐策略：全平台红娘，不限制地区和等级");

            // 优化排序策略：确保推荐多样性和质量
            queryWrapper.orderByDesc("matchmaker_level");  // 优先高等级红娘
            queryWrapper.orderByDesc("success_count");     // 成功案例多的红娘
            queryWrapper.orderByDesc("service_years");     // 经验丰富的红娘
            queryWrapper.orderByAsc("created_at");         // 平衡新老红娘

            // 设置查询数量限制
            int queryLimit = 50; // 默认查询更多红娘
            if (limit != null && limit > 0) {
                queryLimit = Math.min(limit * 2, 100); // 候选池是推荐数量的2倍，最多100个
            }
            queryWrapper.last("LIMIT " + queryLimit);

            List<TbMatchmaker> result = this.list(queryWrapper);
            log.info("AI模块成功查询到{}个红娘，准备推荐给用户", result != null ? result.size() : 0);

            return result != null ? result : java.util.Collections.emptyList();

        } catch (Exception e) {
            log.error("AI模块推荐红娘失败", e);
            return java.util.Collections.emptyList();
        }
    }

    @Override
    public TbMatchmaker getMatchmakerDetailById(Long matchmakerId) {
        try {
            if (matchmakerId == null || matchmakerId <= 0) {
                log.warn("AI模块：红娘ID无效：{}", matchmakerId);
                return null;
            }

            log.debug("AI模块查询红娘详情，ID：{}", matchmakerId);

            QueryWrapper<TbMatchmaker> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("matchmaker_id", matchmakerId);
            queryWrapper.eq("matchmaker_status", 1); // 只查询正常状态的红娘

            TbMatchmaker result = this.getOne(queryWrapper);
            if (result == null) {
                log.warn("AI模块：未找到红娘信息或红娘状态异常，ID：{}", matchmakerId);
            } else {
                log.debug("AI模块：成功查询到红娘详情，姓名：{}", result.getRealName());
            }

            return result;

        } catch (Exception e) {
            log.error("AI模块查询红娘详情失败，ID：{}", matchmakerId, e);
            return null;
        }
    }

    @Override
    public List<Long> getUserIdsByMatchmakerIds(List<Long> matchmakerIds) {
        try {
            if (matchmakerIds == null || matchmakerIds.isEmpty()) {
                log.debug("AI模块：红娘ID列表为空");
                return java.util.Collections.emptyList();
            }

            // 过滤无效ID
            List<Long> validIds = matchmakerIds.stream()
                    .filter(id -> id != null && id > 0)
                    .collect(Collectors.toList());

            if (validIds.isEmpty()) {
                log.warn("AI模块：没有有效的红娘ID");
                return java.util.Collections.emptyList();
            }

            log.debug("AI模块查询红娘对应的用户ID，红娘数量：{}", validIds.size());

            QueryWrapper<TbMatchmaker> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("matchmaker_id", validIds);
            queryWrapper.eq("matchmaker_status", 1);
            queryWrapper.select("user_id"); // 只查询user_id字段

            List<TbMatchmaker> matchmakers = this.list(queryWrapper);

            List<Long> userIds = matchmakers.stream()
                    .map(TbMatchmaker::getUserId)
                    .filter(userId -> userId != null && userId > 0)
                    .collect(Collectors.toList());

            log.debug("AI模块查询到{}个有效的用户ID", userIds.size());
            return userIds;

        } catch (Exception e) {
            log.error("AI模块查询红娘对应用户ID失败", e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 从地址中提取城市名称
     * 例如："北京市朝阳区" -> "北京市"
     */
    private String extractCityFromLocation(String location) {
        try {
            if (!StringUtils.hasText(location)) {
                return location;
            }

            String trimmedLocation = location.trim();

            // 简单的城市提取逻辑，可以根据实际需求优化
            if (trimmedLocation.contains("市")) {
                int index = trimmedLocation.indexOf("市");
                return trimmedLocation.substring(0, index + 1);
            } else if (trimmedLocation.contains("省")) {
                int index = trimmedLocation.indexOf("省");
                return trimmedLocation.substring(0, index + 1);
            } else if (trimmedLocation.contains("区")) {
                // 对于直辖市的区，尝试提取市级信息
                if (trimmedLocation.startsWith("北京") || trimmedLocation.startsWith("上海") ||
                    trimmedLocation.startsWith("天津") || trimmedLocation.startsWith("重庆")) {
                    return trimmedLocation.substring(0, 2) + "市";
                }
            } else if (trimmedLocation.contains("县")) {
                // 处理县级地区
                int index = trimmedLocation.indexOf("县");
                if (index > 0) {
                    return trimmedLocation.substring(0, index + 1);
                }
            }

            return trimmedLocation;

        } catch (Exception e) {
            log.warn("AI模块提取城市名称失败，原地址：{}", location, e);
            return location;
        }
    }

}




