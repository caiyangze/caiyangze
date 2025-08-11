package com.zhentao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhentao.pojo.TbMoment;
import com.zhentao.vo.MomentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 动态Mapper
 * @author zhentao
 * @date 2025/7/21
 */
@Mapper
public interface MomentMapper extends BaseMapper<TbMoment> {
    
    /**
     * 分页查询动态列表（带用户信息）
     * @param page 分页对象
     * @param userId 查询指定用户的动态（可为空）
     * @param visibility 可见范围（可为空）
     * @param status 状态
     * @param currentUserId 当前登录用户ID
     * @return 动态列表
     */
    @Select("<script>" +
            "SELECT " +
            "m.moment_id, m.user_id, m.content, m.location, m.visibility, " +
            "m.like_count, m.comment_count, m.view_count, m.status, " +
            "m.created_at, m.updated_at, " +
            "u.nickname, u.avatar_url " +
            "FROM tb_moment m " +
            "LEFT JOIN tb_user u ON m.user_id = u.user_id " +
            "WHERE m.status = #{status} " +
            "<if test='userId != null'> AND m.user_id = #{userId} </if> " +
            "<if test='visibility != null'> AND m.visibility = #{visibility} </if> " +
            "ORDER BY m.created_at DESC" +
            "</script>")
    List<MomentVO> selectMomentPage(Page<MomentVO> page, 
                                   @Param("userId") Long userId,
                                   @Param("visibility") Integer visibility,
                                   @Param("status") Integer status,
                                   @Param("currentUserId") Long currentUserId);
    
    /**
     * 查询公开动态列表
     * @param page 分页对象
     * @param currentUserId 当前登录用户ID
     * @return 公开动态列表
     */
    @Select("SELECT " +
            "m.moment_id, m.user_id, m.content, m.location, m.visibility, " +
            "m.like_count, m.comment_count, m.view_count, m.status, " +
            "m.created_at, m.updated_at, " +
            "u.nickname, u.avatar_url " +
            "FROM tb_moment m " +
            "LEFT JOIN tb_user u ON m.user_id = u.user_id " +
            "WHERE m.status = 1 AND m.visibility = 1 " +
            "ORDER BY m.created_at DESC")
    List<MomentVO> selectPublicMoments(Page<MomentVO> page, @Param("currentUserId") Long currentUserId);
    
    /**
     * 查询用户自己的动态列表
     * @param page 分页对象
     * @param userId 用户ID
     * @return 用户动态列表
     */
    @Select("SELECT " +
            "m.moment_id, m.user_id, m.content, m.location, m.visibility, " +
            "m.like_count, m.comment_count, m.view_count, m.status, " +
            "m.created_at, m.updated_at, " +
            "u.nickname, u.avatar_url " +
            "FROM tb_moment m " +
            "LEFT JOIN tb_user u ON m.user_id = u.user_id " +
            "WHERE m.user_id = #{userId} AND m.status = 1 " +
            "ORDER BY m.created_at DESC")
    List<MomentVO> selectUserMoments(Page<MomentVO> page, @Param("userId") Long userId);

    /**
     * 查询指定用户的公开动态列表
     * @param page 分页对象
     * @param userId 用户ID
     * @param currentUserId 当前登录用户ID
     * @return 用户公开动态列表
     */
    @Select("SELECT " +
            "m.moment_id, m.user_id, m.content, m.location, m.visibility, " +
            "m.like_count, m.comment_count, m.view_count, m.status, " +
            "m.created_at, m.updated_at, " +
            "u.nickname, u.avatar_url " +
            "FROM tb_moment m " +
            "LEFT JOIN tb_user u ON m.user_id = u.user_id " +
            "WHERE m.user_id = #{userId} AND m.status = 1 AND m.visibility = 1 " +
            "ORDER BY m.created_at DESC")
    List<MomentVO> selectUserPublicMoments(Page<MomentVO> page,
                                          @Param("userId") Long userId,
                                          @Param("currentUserId") Long currentUserId);
}
