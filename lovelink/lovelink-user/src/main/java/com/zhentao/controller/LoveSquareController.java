package com.zhentao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhentao.dto.UserListDto;
import com.zhentao.dto.UserPhotoDto;
import com.zhentao.pojo.*;
import com.zhentao.service.*;
import com.zhentao.utils.JwtService;
import com.zhentao.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author 王恒飞
 * @date 2025/7/18
 * @ClassName LoveSquareController
 */
@RestController
@RequestMapping("/loveSquare")
public class LoveSquareController {
    @Autowired
    UserService userService;
    @Autowired
    TbUserProfileService userProfileService;
    @Autowired
    TbUserTagService userTagService;
    @Autowired
    TbTagService tagService;

    @RequestMapping("list")
    public Result list(@RequestBody UserListDto userListDto, @RequestHeader("token") String token) {
        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Integer userId = (Integer) claimsMap.get("userId");
        Page<TbUser> userPage = new Page<>(userListDto.pageNum, userListDto.getPageSize());
        QueryWrapper<TbUser> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.ne("user_id", userId);
        userQueryWrapper.eq("account_status", 1);
        userQueryWrapper.eq(userListDto.getUserId()!=null,"user_id",userListDto.getUserId());
        userService.page(userPage, userQueryWrapper);
        List<TbUser> userList = userPage.getRecords();
        for (TbUser user : userList) {
            QueryWrapper<TbUserProfile> userProfileQueryWrapper = new QueryWrapper<>();
            userProfileQueryWrapper.eq("user_id", user.getUserId());
            TbUserProfile one = userProfileService.getOne(userProfileQueryWrapper);
            if (one != null) {
                user.setUserProfile(one);
            }
            QueryWrapper<TbUserTag> userTagQueryWrapper = new QueryWrapper<>();
            userTagQueryWrapper.eq("user_id", user.getUserId());
            List<TbUserTag> userTags = userTagService.list(userTagQueryWrapper);
            user.setUserTags(userTags);
            if (userTags != null && userTags.size() > 0) {
                for (TbUserTag userTag : userTags) {
                    QueryWrapper<TbTag> tagQueryWrapper = new QueryWrapper<>();
                    tagQueryWrapper.eq("tag_id", userTag.getTagId());
                    tagQueryWrapper.eq("tag_status", 1);
                    TbTag one1 = tagService.getOne(tagQueryWrapper);
                    if (one1 != null) {
                        userTag.setTagName(one1.getTagName());
                    }
                }
            }
        }
        return Result.OK(userPage);
    }
    @Autowired
    TbUserPhotoService userPhotoService;
    @RequestMapping("findUserPhoto")
    public Result findUserPhoto(@RequestBody UserPhotoDto userPhotoDto) {
        QueryWrapper<TbUserPhoto> userPhotoQueryWrapper=new QueryWrapper<>();
        userPhotoQueryWrapper.eq("user_id",userPhotoDto.getUserId());
        userPhotoQueryWrapper.eq("is_public",1);
        userPhotoQueryWrapper.eq("is_avatar",0);
        userPhotoQueryWrapper.orderByAsc("sort_order");
        Page<TbUserPhoto> page = userPhotoService.page(new Page<>(userPhotoDto.getPageNum(), userPhotoDto.getPageSize()), userPhotoQueryWrapper);
        return Result.OK(page);
    }
}
