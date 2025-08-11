package com.zhentao.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhentao.dto.CommentCreateDTO;
import com.zhentao.dto.MomentCreateDTO;
import com.zhentao.dto.MomentQueryDTO;
import com.zhentao.dto.MomentUpdateDTO;
import com.zhentao.pojo.TbMoment;
import com.zhentao.service.MomentService;
import com.zhentao.utils.JwtService;
import com.zhentao.utils.Result;
import com.zhentao.vo.MomentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 动态控制器
 *
 * @author zhentao
 * @date 2025/7/21
 */
@Slf4j
@RestController
@RequestMapping("/api/social/moment")
public class MomentController {

    @Autowired
    private MomentService momentService;

    /**
     * 发布动态
     */
    @PostMapping("/create")
    public Result createMoment(@RequestBody MomentCreateDTO createDTO,
                               HttpServletRequest request) {
        try {
            // 从token中获取用户ID
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                return Result.NO_LOGIN();
            }

            return momentService.createMoment(createDTO, userId);

        } catch (Exception e) {
            log.error("发布动态失败：", e);
            return Result.ERROR("发布动态失败：" + e.getMessage());
        }
    }

    /**
     * 更新动态
     */
    @PutMapping("/update")
    public Result updateMoment(@Validated @RequestBody MomentUpdateDTO updateDTO,
                               HttpServletRequest request) {
        try {
            // 从token中获取用户ID
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                return Result.NO_LOGIN();
            }

            return momentService.updateMoment(updateDTO, userId);

        } catch (Exception e) {
            log.error("更新动态失败：", e);
            return Result.ERROR("更新动态失败：" + e.getMessage());
        }
    }

    /**
     * 删除动态
     */
    @DeleteMapping("/delete/{momentId}")
    public Result deleteMoment(@PathVariable Long momentId, HttpServletRequest request) {
        try {
            // 从token中获取用户ID
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                return Result.NO_LOGIN();
            }

            return momentService.deleteMoment(momentId, userId);

        } catch (Exception e) {
            log.error("删除动态失败：", e);
            return Result.ERROR("删除动态失败：" + e.getMessage());
        }
    }

    /**
     * 查询动态详情
     */
    @GetMapping("/detail/{momentId}")
    public Result getMomentDetail(@PathVariable Long momentId, HttpServletRequest request) {
        try {
            // 从token中获取用户ID（可为空，游客也可以查看公开动态）
            Long userId = getUserIdFromToken(request);

            return momentService.getMomentDetail(momentId, userId);

        } catch (Exception e) {
            log.error("查询动态详情失败：", e);
            return Result.ERROR("查询动态详情失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询动态列表
     */
    @PostMapping("/list")
    public Result getMomentList(@RequestBody MomentQueryDTO queryDTO,
                                HttpServletRequest request) {
        try {
            // 从token中获取用户ID（可为空）
            Long userId = getUserIdFromToken(request);

            return momentService.getMomentList(queryDTO, userId);

        } catch (Exception e) {
            log.error("查询动态列表失败：", e);
            return Result.ERROR("查询动态列表失败：" + e.getMessage());
        }
    }

    /**
     * 查询公开动态列表
     */
    @GetMapping("/public")
    public Result getPublicMoments(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   HttpServletRequest request) {
        try {
            // 从token中获取用户ID（可为空）
            Long userId = getUserIdFromToken(request);

            return momentService.getPublicMoments(pageNum, pageSize, userId);

        } catch (Exception e) {
            log.error("查询公开动态失败：", e);
            return Result.ERROR("查询公开动态失败：" + e.getMessage());
        }
    }

    /**
     * 查询用户自己的动态列表
     */
    @GetMapping("/mine")
    public Result getUserMoments(@RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 HttpServletRequest request) {
        try {
            // 从token中获取用户ID
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                return Result.NO_LOGIN();
            }

            return momentService.getUserMoments(pageNum, pageSize, userId);

        } catch (Exception e) {
            log.error("查询用户动态失败：", e);
            return Result.ERROR("查询用户动态失败：" + e.getMessage());
        }
    }

    /**
     * 查询指定用户的公开动态列表
     */
    @GetMapping("/user/{userId}")
    public Result getUserPublicMoments(@PathVariable Long userId,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       HttpServletRequest request) {
        try {
            // 从token中获取当前用户ID（可为空）
            Long currentUserId = getUserIdFromToken(request);

            return momentService.getUserPublicMoments(pageNum, pageSize, userId, currentUserId);

        } catch (Exception e) {
            log.error("查询用户公开动态失败：", e);
            return Result.ERROR("查询用户公开动态失败：" + e.getMessage());
        }
    }

    /**
     * 更新动态可见性
     */
    @PutMapping("/visibility/{momentId}")
    public Result updateMomentVisibility(@PathVariable Long momentId,
                                         @RequestParam Integer visibility,
                                         HttpServletRequest request) {
        try {
            // 从token中获取用户ID
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                return Result.NO_LOGIN();
            }

            // 验证可见性参数
            if (visibility < 1 || visibility > 3) {
                return Result.ERROR("可见性参数错误，应为1-3之间的数字");
            }

            return momentService.updateMomentVisibility(momentId, visibility, userId);

        } catch (Exception e) {
            log.error("更新动态可见性失败：", e);
            return Result.ERROR("更新动态可见性失败：" + e.getMessage());
        }
    }

    /**
     * 点赞/取消点赞动态
     */
    @PostMapping("/like/{momentId}")
    public Result likeMoment(@PathVariable Long momentId, HttpServletRequest request) {
        try {
            // 从token中获取用户ID
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                return Result.NO_LOGIN();
            }

            return momentService.likeMoment(momentId, userId);

        } catch (Exception e) {
            log.error("点赞动态失败：", e);
            return Result.ERROR("点赞动态失败：" + e.getMessage());
        }
    }

    /**
     * 增加动态浏览次数
     */
    @PostMapping("/view/{momentId}")
    public Result incrementViewCount(@PathVariable Long momentId) {
        try {
            return momentService.incrementViewCount(momentId);

        } catch (Exception e) {
            log.error("增加浏览次数失败：", e);
            return Result.ERROR("增加浏览次数失败：" + e.getMessage());
        }
    }

    /**
     * 获取动态评论列表
     */
    @GetMapping("/comments/{momentId}")
    public Result getMomentComments(@PathVariable Long momentId,
                                    @RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "20") Integer pageSize,
                                    HttpServletRequest request) {
        try {
            // 从token中获取用户ID（可为空）
            Long userId = getUserIdFromToken(request);

            return momentService.getMomentComments(momentId, pageNum, pageSize, userId);

        } catch (Exception e) {
            log.error("获取评论列表失败：", e);
            return Result.ERROR("获取评论列表失败：" + e.getMessage());
        }
    }

    /**
     * 添加动态评论
     */
    @PostMapping("/comment")
    public Result addMomentComment(@RequestBody CommentCreateDTO commentDTO,
                                   HttpServletRequest request) {
        try {
            // 从token中获取用户ID
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                return Result.NO_LOGIN();
            }

            return momentService.addMomentComment(commentDTO, userId);

        } catch (Exception e) {
            log.error("添加评论失败：", e);
            return Result.ERROR("添加评论失败：" + e.getMessage());
        }
    }

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        try {
            // 从请求头获取token
            String token = request.getHeader("token");
            if (token == null || token.isEmpty()) {
                token = request.getParameter("token");
            }

            if (token != null && !token.isEmpty()) {
                // 验证token并获取用户ID
                return JwtService.getUserIdFromToken(token);
            }

            return null;
        } catch (Exception e) {
            log.warn("获取用户ID失败：", e);
            return null;
        }
    }
}
