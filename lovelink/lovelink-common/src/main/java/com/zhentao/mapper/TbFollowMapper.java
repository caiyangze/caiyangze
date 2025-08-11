package com.zhentao.mapper;

import com.zhentao.pojo.TbFollow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
* @author a2634
* @description 针对表【tb_follow】的数据库操作Mapper
* @createDate 2025-07-20 19:55:47
* @Entity com.zhentao.pojo.TbFollow
*/
public interface TbFollowMapper extends BaseMapper<TbFollow> {

    /**
     * 查询互相关注的用户
     * @param userId 当前用户ID
     * @return 互相关注的用户列表
     */
    @Select("SELECT DISTINCT " +
            "f1.followed_user_id as other_user_id, " +
            "f1.created_at as follow_time " +
            "FROM tb_follow f1 " +
            "INNER JOIN tb_follow f2 ON " +
            "  f1.user_id = #{userId} " +
            "  AND f1.followed_user_id = f2.user_id " +
            "  AND f2.followed_user_id = #{userId} " +
            "WHERE f1.follow_status = 1 " +
            "  AND f2.follow_status = 1 " +
            "ORDER BY f1.created_at DESC")
    List<Map<String, Object>> selectMutualFollows(@Param("userId") Long userId);

}




