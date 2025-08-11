package com.zhentao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhentao.dto.CommentCreateDTO;
import com.zhentao.dto.MomentCreateDTO;
import com.zhentao.dto.MomentQueryDTO;
import com.zhentao.dto.MomentUpdateDTO;
import com.zhentao.mapper.MomentMapper;
import com.zhentao.mapper.TbCommentMapper;
import com.zhentao.mapper.TbLikeMapper;
import com.zhentao.mapper.TbMomentMediaMapper;
import com.zhentao.mapper.TbUserMapper;
import com.zhentao.pojo.TbComment;
import com.zhentao.pojo.TbLike;
import com.zhentao.pojo.TbMoment;
import com.zhentao.pojo.TbMomentMedia;
import com.zhentao.pojo.TbUser;
import com.zhentao.service.MomentService;
import com.zhentao.utils.MinioUtil;
import com.zhentao.utils.Result;
import com.zhentao.vo.CommentVO;
import com.zhentao.vo.MomentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 动态服务实现类
 * @author zhentao
 * @date 2025/7/21
 */
@Slf4j
@Service
public class MomentServiceImpl implements MomentService {

    @Autowired
    private MomentMapper momentMapper;

    @Autowired
    private TbMomentMediaMapper momentMediaMapper;

    @Autowired
    private TbLikeMapper likeMapper;

    @Autowired
    private TbCommentMapper commentMapper;

    @Autowired
    private TbUserMapper userMapper;

    @Autowired
    private MinioUtil minioUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createMoment(MomentCreateDTO createDTO, Long userId) {
        try {
            // 1. 创建动态基本信息
            TbMoment moment = new TbMoment();
            moment.setUserId(userId);
            moment.setContent(createDTO.getContent());
            moment.setLocation(createDTO.getLocation());
            moment.setVisibility(createDTO.getVisibility());
            moment.setLikeCount(0);
            moment.setCommentCount(0);
            moment.setViewCount(0);
            moment.setStatus(1);
            moment.setCreatedAt(new Date());
            moment.setUpdatedAt(new Date());

            // 2. 保存动态
            int result = momentMapper.insert(moment);
            if (result <= 0) {
                return Result.ERROR("发布动态失败");
            }

            // 3. 处理媒体文件上传
            if (createDTO.getMediaFiles() != null && !createDTO.getMediaFiles().isEmpty()) {
                for (int i = 0; i < createDTO.getMediaFiles().size(); i++) {
                    MultipartFile file = createDTO.getMediaFiles().get(i);
                    if (!file.isEmpty()) {
                        // 上传文件到MinIO
                        String mediaUrl = minioUtil.uploadFile(file, "moment");

                        // 保存媒体信息
                        TbMomentMedia media = new TbMomentMedia();
                        media.setMomentId(moment.getMomentId());
                        media.setMediaUrl(mediaUrl);
                        media.setSortOrder(i + 1);

                        // 根据文件类型设置媒体类型
                        String contentType = file.getContentType();
                        if (contentType != null) {
                            if (contentType.startsWith("image/")) {
                                media.setMediaType(1); // 图片
                            } else if (contentType.startsWith("video/")) {
                                media.setMediaType(2); // 视频
                            } else if (contentType.startsWith("audio/")) {
                                media.setMediaType(3); // 音频
                            } else {
                                media.setMediaType(1); // 默认图片
                            }
                        } else {
                            media.setMediaType(1); // 默认图片
                        }

                        momentMediaMapper.insert(media);
                    }
                }
            }

            // 4. 处理前端传递的图片URL列表
            if (createDTO.getImageUrls() != null && !createDTO.getImageUrls().isEmpty()) {
                for (int i = 0; i < createDTO.getImageUrls().size(); i++) {
                    String imageUrl = createDTO.getImageUrls().get(i);
                    if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                        // 保存媒体信息
                        TbMomentMedia media = new TbMomentMedia();
                        media.setMomentId(moment.getMomentId());
                        media.setMediaUrl(imageUrl);
                        media.setMediaType(1); // 图片
                        media.setSortOrder(i + 1);

                        momentMediaMapper.insert(media);
                    }
                }
            }

            log.info("用户{}发布动态成功，动态ID：{}", userId, moment.getMomentId());
            return Result.OK(moment);

        } catch (Exception e) {
            log.error("发布动态失败：", e);
            return Result.ERROR("发布动态失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateMoment(MomentUpdateDTO updateDTO, Long userId) {
        try {
            // 1. 查询动态是否存在且属于当前用户
            TbMoment existingMoment = momentMapper.selectById(updateDTO.getMomentId());
            if (existingMoment == null) {
                return Result.ERROR("动态不存在");
            }

            if (!existingMoment.getUserId().equals(userId)) {
                return Result.ERROR("无权限修改此动态");
            }

            // 2. 更新动态信息
            LambdaUpdateWrapper<TbMoment> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(TbMoment::getMomentId, updateDTO.getMomentId())
                    .eq(TbMoment::getUserId, userId);

            if (updateDTO.getContent() != null) {
                updateWrapper.set(TbMoment::getContent, updateDTO.getContent());
            }
            if (updateDTO.getLocation() != null) {
                updateWrapper.set(TbMoment::getLocation, updateDTO.getLocation());
            }
            if (updateDTO.getVisibility() != null) {
                updateWrapper.set(TbMoment::getVisibility, updateDTO.getVisibility());
            }
            updateWrapper.set(TbMoment::getUpdatedAt, new Date());

            int result = momentMapper.update(null, updateWrapper);
            if (result <= 0) {
                return Result.ERROR("更新动态失败");
            }

            // 3. 返回更新后的动态
            TbMoment updatedMoment = momentMapper.selectById(updateDTO.getMomentId());
            log.info("用户{}更新动态成功，动态ID：{}", userId, updateDTO.getMomentId());
            return Result.OK(updatedMoment);

        } catch (Exception e) {
            log.error("更新动态失败：", e);
            return Result.ERROR("更新动态失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteMoment(Long momentId, Long userId) {
        try {
            // 1. 查询动态是否存在且属于当前用户
            TbMoment existingMoment = momentMapper.selectById(momentId);
            if (existingMoment == null) {
                return Result.ERROR("动态不存在");
            }

            if (!existingMoment.getUserId().equals(userId)) {
                return Result.ERROR("无权限删除此动态");
            }

            // 2. 软删除动态（设置状态为0）
            LambdaUpdateWrapper<TbMoment> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(TbMoment::getMomentId, momentId)
                    .eq(TbMoment::getUserId, userId)
                    .set(TbMoment::getStatus, 0)
                    .set(TbMoment::getUpdatedAt, new Date());

            int result = momentMapper.update(null, updateWrapper);
            if (result <= 0) {
                return Result.ERROR("删除动态失败");
            }

            log.info("用户{}删除动态成功，动态ID：{}", userId, momentId);
            return Result.OK();

        } catch (Exception e) {
            log.error("删除动态失败：", e);
            return Result.ERROR("删除动态失败：" + e.getMessage());
        }
    }

    @Override
    public Result getMomentDetail(Long momentId, Long userId) {
        try {
            // 1. 查询动态基本信息
            TbMoment moment = momentMapper.selectById(momentId);
            if (moment == null || moment.getStatus() == 0) {
                return Result.ERROR("动态不存在");
            }

            // 2. 检查可见性权限
            if (!checkMomentVisibility(moment, userId)) {
                return Result.ERROR("无权限查看此动态");
            }

            // 3. 构建返回对象
            MomentVO momentVO = buildMomentVO(moment, userId);

            // 4. 增加浏览数（异步处理，不影响查询性能）
            if (!moment.getUserId().equals(userId)) {
                incrementViewCountInternal(momentId);
            }

            return Result.OK(momentVO);

        } catch (Exception e) {
            log.error("查询动态详情失败：", e);
            return Result.ERROR("查询动态详情失败：" + e.getMessage());
        }
    }

    @Override
    public Result getMomentList(MomentQueryDTO queryDTO, Long currentUserId) {
        try {
            Page<MomentVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

            // 如果是查询自己的动态
            if (queryDTO.getOnlyMine() && currentUserId != null) {
                queryDTO.setUserId(currentUserId);
            }

            List<MomentVO> moments = momentMapper.selectMomentPage(page,
                    queryDTO.getUserId(),
                    queryDTO.getVisibility(),
                    queryDTO.getStatus(),
                    currentUserId);

            // 填充额外信息
            moments = moments.stream().map(moment -> {
                fillMomentExtraInfo(moment, currentUserId);
                return moment;
            }).collect(Collectors.toList());

            page.setRecords(moments);
            return Result.OK(page);

        } catch (Exception e) {
            log.error("查询动态列表失败：", e);
            return Result.ERROR("查询动态列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result getPublicMoments(Integer pageNum, Integer pageSize, Long currentUserId) {
        try {
            Page<MomentVO> page = new Page<>(pageNum, pageSize);

            List<MomentVO> moments = momentMapper.selectPublicMoments(page, currentUserId);

            // 填充额外信息
            moments = moments.stream().map(moment -> {
                fillMomentExtraInfo(moment, currentUserId);
                return moment;
            }).collect(Collectors.toList());

            page.setRecords(moments);
            return Result.OK(page);

        } catch (Exception e) {
            log.error("查询公开动态失败：", e);
            return Result.ERROR("查询公开动态失败：" + e.getMessage());
        }
    }

    @Override
    public Result getUserMoments(Integer pageNum, Integer pageSize, Long userId) {
        try {
            Page<MomentVO> page = new Page<>(pageNum, pageSize);

            List<MomentVO> moments = momentMapper.selectUserMoments(page, userId);

            // 填充额外信息
            moments = moments.stream().map(moment -> {
                fillMomentExtraInfo(moment, userId);
                moment.setIsMine(true); // 用户自己的动态
                return moment;
            }).collect(Collectors.toList());

            page.setRecords(moments);
            return Result.OK(page);

        } catch (Exception e) {
            log.error("查询用户动态失败：", e);
            return Result.ERROR("查询用户动态失败：" + e.getMessage());
        }
    }

    @Override
    public Result getUserPublicMoments(Integer pageNum, Integer pageSize, Long userId, Long currentUserId) {
        try {
            Page<MomentVO> page = new Page<>(pageNum, pageSize);

            List<MomentVO> moments = momentMapper.selectUserPublicMoments(page, userId, currentUserId);

            // 填充额外信息
            moments = moments.stream().map(moment -> {
                fillMomentExtraInfo(moment, currentUserId);
                // 判断是否是当前用户自己的动态
                moment.setIsMine(currentUserId != null && currentUserId.equals(moment.getUserId()));
                return moment;
            }).collect(Collectors.toList());

            page.setRecords(moments);
            return Result.OK(page);

        } catch (Exception e) {
            log.error("查询用户公开动态失败：", e);
            return Result.ERROR("查询用户公开动态失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateMomentVisibility(Long momentId, Integer visibility, Long userId) {
        try {
            // 1. 查询动态是否存在且属于当前用户
            TbMoment existingMoment = momentMapper.selectById(momentId);
            if (existingMoment == null) {
                return Result.ERROR("动态不存在");
            }

            if (!existingMoment.getUserId().equals(userId)) {
                return Result.ERROR("无权限修改此动态");
            }

            // 2. 更新可见性
            LambdaUpdateWrapper<TbMoment> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(TbMoment::getMomentId, momentId)
                    .eq(TbMoment::getUserId, userId)
                    .set(TbMoment::getVisibility, visibility)
                    .set(TbMoment::getUpdatedAt, new Date());

            int result = momentMapper.update(null, updateWrapper);
            if (result <= 0) {
                return Result.ERROR("更新可见性失败");
            }

            log.info("用户{}更新动态可见性成功，动态ID：{}，可见性：{}", userId, momentId, visibility);
            return Result.OK();

        } catch (Exception e) {
            log.error("更新动态可见性失败：", e);
            return Result.ERROR("更新动态可见性失败：" + e.getMessage());
        }
    }

    /**
     * 检查动态可见性权限
     */
    private boolean checkMomentVisibility(TbMoment moment, Long currentUserId) {
        // 如果是自己的动态，可以查看
        if (currentUserId != null && moment.getUserId().equals(currentUserId)) {
            return true;
        }

        // 如果是公开动态，可以查看
        if (moment.getVisibility() == 1) {
            return true;
        }

        // 如果是仅关注者可见，需要检查关注关系（这里简化处理，后续可以扩展）
        if (moment.getVisibility() == 2) {
            // TODO: 检查是否关注了该用户
            return currentUserId != null;
        }

        // 仅自己可见
        return false;
    }

    /**
     * 构建动态VO对象
     */
    private MomentVO buildMomentVO(TbMoment moment, Long currentUserId) {
        MomentVO momentVO = new MomentVO();
        BeanUtils.copyProperties(moment, momentVO);

        // 填充额外信息
        fillMomentExtraInfo(momentVO, currentUserId);

        return momentVO;
    }

    /**
     * 填充动态额外信息
     */
    private void fillMomentExtraInfo(MomentVO momentVO, Long currentUserId) {
        // 1. 查询用户信息
        if (momentVO.getNickname() == null || momentVO.getAvatarUrl() == null) {
            // 如果用户信息为空，则查询用户表
            TbUser user = userMapper.selectById(momentVO.getUserId());
            if (user != null) {
                momentVO.setNickname(user.getNickname());
                momentVO.setAvatarUrl(user.getAvatarUrl());
            }
        }

        // 2. 查询媒体文件
        LambdaQueryWrapper<TbMomentMedia> mediaWrapper = new LambdaQueryWrapper<>();
        mediaWrapper.eq(TbMomentMedia::getMomentId, momentVO.getMomentId())
                .orderByAsc(TbMomentMedia::getSortOrder);
        List<TbMomentMedia> mediaList = momentMediaMapper.selectList(mediaWrapper);
        momentVO.setMediaList(mediaList);

        // 3. 检查是否已点赞
        if (currentUserId != null) {
            LambdaQueryWrapper<TbLike> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(TbLike::getUserId, currentUserId)
                    .eq(TbLike::getTargetId, momentVO.getMomentId())
                    .eq(TbLike::getTargetType, 1); // 1表示动态
            TbLike like = likeMapper.selectOne(likeWrapper);
            momentVO.setIsLiked(like != null);

            // 4. 检查是否是自己的动态
            momentVO.setIsMine(momentVO.getUserId().equals(currentUserId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result likeMoment(Long momentId, Long userId) {
        try {
            // 1. 检查动态是否存在
            TbMoment moment = momentMapper.selectById(momentId);
            if (moment == null || moment.getStatus() == 0) {
                return Result.ERROR("动态不存在");
            }

            // 2. 检查是否已点赞
            LambdaQueryWrapper<TbLike> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(TbLike::getUserId, userId)
                    .eq(TbLike::getTargetId, momentId)
                    .eq(TbLike::getTargetType, 1); // 1表示动态
            TbLike existingLike = likeMapper.selectOne(likeWrapper);

            if (existingLike != null) {
                // 取消点赞
                likeMapper.deleteById(existingLike.getLikeId());

                // 减少点赞数
                LambdaUpdateWrapper<TbMoment> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(TbMoment::getMomentId, momentId)
                        .setSql("like_count = GREATEST(like_count - 1, 0)");
                momentMapper.update(null, updateWrapper);

                log.info("用户{}取消点赞动态{}成功", userId, momentId);
                return Result.OK("取消点赞成功");
            } else {
                // 添加点赞
                TbLike like = new TbLike();
                like.setUserId(userId);
                like.setTargetId(momentId);
                like.setTargetType(1); // 1表示动态
                like.setCreatedAt(new Date());
                likeMapper.insert(like);

                // 增加点赞数
                LambdaUpdateWrapper<TbMoment> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(TbMoment::getMomentId, momentId)
                        .setSql("like_count = like_count + 1");
                momentMapper.update(null, updateWrapper);

                log.info("用户{}点赞动态{}成功", userId, momentId);
                return Result.OK("点赞成功");
            }

        } catch (Exception e) {
            log.error("点赞操作失败：", e);
            return Result.ERROR("点赞操作失败：" + e.getMessage());
        }
    }

    @Override
    public Result incrementViewCount(Long momentId) {
        try {
            // 检查动态是否存在
            TbMoment moment = momentMapper.selectById(momentId);
            if (moment == null || moment.getStatus() == 0) {
                return Result.ERROR("动态不存在");
            }

            // 增加浏览数
            LambdaUpdateWrapper<TbMoment> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(TbMoment::getMomentId, momentId)
                    .setSql("view_count = view_count + 1");
            momentMapper.update(null, updateWrapper);

            return Result.OK("浏览次数增加成功");

        } catch (Exception e) {
            log.error("增加浏览次数失败：", e);
            return Result.ERROR("增加浏览次数失败：" + e.getMessage());
        }
    }

    @Override
    public Result getMomentComments(Long momentId, Integer pageNum, Integer pageSize, Long currentUserId) {
        try {
            // 1. 检查动态是否存在
            TbMoment moment = momentMapper.selectById(momentId);
            if (moment == null || moment.getStatus() == 0) {
                return Result.ERROR("动态不存在");
            }

            // 2. 分页查询评论
            Page<CommentVO> page = new Page<>(pageNum, pageSize);

            // 查询顶级评论（parentId为null）
            LambdaQueryWrapper<TbComment> commentWrapper = new LambdaQueryWrapper<>();
            commentWrapper.eq(TbComment::getTargetId, momentId)
                    .eq(TbComment::getTargetType, 1) // 1表示动态
                    .eq(TbComment::getStatus, 1)
                    .isNull(TbComment::getParentId)
                    .orderByDesc(TbComment::getCreatedAt);

            Page<TbComment> commentPage = new Page<>(pageNum, pageSize);
            commentPage = commentMapper.selectPage(commentPage, commentWrapper);

            // 3. 转换为VO并填充用户信息
            List<CommentVO> commentVOs = commentPage.getRecords().stream().map(comment -> {
                CommentVO commentVO = new CommentVO();
                BeanUtils.copyProperties(comment, commentVO);

                // 查询用户信息
                TbUser user = userMapper.selectById(comment.getUserId());
                if (user != null) {
                    commentVO.setNickname(user.getNickname());
                    commentVO.setAvatarUrl(user.getAvatarUrl());
                } else {
                    commentVO.setNickname("用户" + comment.getUserId());
                    commentVO.setAvatarUrl("");
                }

                // 检查是否已点赞（如果需要的话）
                if (currentUserId != null) {
                    // TODO: 检查评论点赞状态
                    commentVO.setIsLiked(false);
                }

                return commentVO;
            }).collect(Collectors.toList());

            page.setRecords(commentVOs);
            page.setTotal(commentPage.getTotal());
            page.setPages(commentPage.getPages());
            page.setCurrent(commentPage.getCurrent());
            page.setSize(commentPage.getSize());

            return Result.OK(page);

        } catch (Exception e) {
            log.error("获取评论列表失败：", e);
            return Result.ERROR("获取评论列表失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addMomentComment(CommentCreateDTO commentDTO, Long userId) {
        try {
            // 1. 检查动态是否存在
            TbMoment moment = momentMapper.selectById(commentDTO.getMomentId());
            if (moment == null || moment.getStatus() == 0) {
                return Result.ERROR("动态不存在");
            }

            // 2. 创建评论
            TbComment comment = new TbComment();
            comment.setTargetId(commentDTO.getMomentId());
            comment.setTargetType(1); // 1表示动态
            comment.setUserId(userId);
            comment.setContent(commentDTO.getContent());
            comment.setParentId(commentDTO.getParentId());
            comment.setReplyUserId(commentDTO.getReplyUserId());
            comment.setLikeCount(0);
            comment.setStatus(1);
            comment.setCreatedAt(new Date());
            comment.setUpdatedAt(new Date());

            int result = commentMapper.insert(comment);
            if (result <= 0) {
                return Result.ERROR("添加评论失败");
            }

            // 3. 增加动态的评论数
            LambdaUpdateWrapper<TbMoment> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(TbMoment::getMomentId, commentDTO.getMomentId())
                    .setSql("comment_count = comment_count + 1");
            momentMapper.update(null, updateWrapper);

            log.info("用户{}评论动态{}成功，评论ID：{}", userId, commentDTO.getMomentId(), comment.getCommentId());
            return Result.OK(comment);

        } catch (Exception e) {
            log.error("添加评论失败：", e);
            return Result.ERROR("添加评论失败：" + e.getMessage());
        }
    }

    /**
     * 增加浏览数（私有方法）
     */
    private void incrementViewCountInternal(Long momentId) {
        try {
            LambdaUpdateWrapper<TbMoment> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(TbMoment::getMomentId, momentId)
                    .setSql("view_count = view_count + 1");
            momentMapper.update(null, updateWrapper);
        } catch (Exception e) {
            log.warn("增加浏览数失败：", e);
        }
    }
}
