package com.zhentao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhentao.dto.CommentCreateDTO;
import com.zhentao.dto.MomentCreateDTO;
import com.zhentao.dto.MomentQueryDTO;
import com.zhentao.dto.MomentUpdateDTO;
import com.zhentao.pojo.TbMoment;
import com.zhentao.utils.Result;
import com.zhentao.vo.MomentVO;

/**
 * 动态服务接口
 * @author zhentao
 * @date 2025/7/21
 */
public interface MomentService {
    
    /**
     * 发布动态
     * @param createDTO 动态创建DTO
     * @param userId 用户ID
     * @return 发布结果
     */
    Result createMoment(MomentCreateDTO createDTO, Long userId);

    /**
     * 更新动态
     * @param updateDTO 动态更新DTO
     * @param userId 用户ID
     * @return 更新结果
     */
    Result updateMoment(MomentUpdateDTO updateDTO, Long userId);

    /**
     * 删除动态
     * @param momentId 动态ID
     * @param userId 用户ID
     * @return 删除结果
     */
    Result deleteMoment(Long momentId, Long userId);

    /**
     * 查询动态详情
     * @param momentId 动态ID
     * @param userId 当前用户ID
     * @return 动态详情
     */
    Result getMomentDetail(Long momentId, Long userId);

    /**
     * 分页查询动态列表
     * @param queryDTO 查询条件
     * @param currentUserId 当前用户ID
     * @return 动态列表
     */
    Result getMomentList(MomentQueryDTO queryDTO, Long currentUserId);

    /**
     * 查询公开动态列表
     * @param pageNum 页码
     * @param pageSize 页大小
     * @param currentUserId 当前用户ID
     * @return 公开动态列表
     */
    Result getPublicMoments(Integer pageNum, Integer pageSize, Long currentUserId);

    /**
     * 查询用户自己的动态列表
     * @param pageNum 页码
     * @param pageSize 页大小
     * @param userId 用户ID
     * @return 用户动态列表
     */
    Result getUserMoments(Integer pageNum, Integer pageSize, Long userId);

    /**
     * 查询指定用户的公开动态列表
     * @param pageNum 页码
     * @param pageSize 页大小
     * @param userId 目标用户ID
     * @param currentUserId 当前用户ID
     * @return 用户公开动态列表
     */
    Result getUserPublicMoments(Integer pageNum, Integer pageSize, Long userId, Long currentUserId);

    /**
     * 更新动态可见性
     * @param momentId 动态ID
     * @param visibility 可见性
     * @param userId 用户ID
     * @return 更新结果
     */
    Result updateMomentVisibility(Long momentId, Integer visibility, Long userId);

    /**
     * 点赞/取消点赞动态
     * @param momentId 动态ID
     * @param userId 用户ID
     * @return 结果
     */
    Result likeMoment(Long momentId, Long userId);

    /**
     * 增加动态浏览次数
     * @param momentId 动态ID
     * @return 结果
     */
    Result incrementViewCount(Long momentId);

    /**
     * 获取动态评论列表
     * @param momentId 动态ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @param currentUserId 当前用户ID
     * @return 结果
     */
    Result getMomentComments(Long momentId, Integer pageNum, Integer pageSize, Long currentUserId);

    /**
     * 添加动态评论
     * @param commentDTO 评论DTO
     * @param userId 用户ID
     * @return 结果
     */
    Result addMomentComment(CommentCreateDTO commentDTO, Long userId);
}
