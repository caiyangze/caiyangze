package com.zhentao.controller;

import com.zhentao.dto.ProfileDTO;
//import com.zhentao.pojo.TbProfile;
import com.zhentao.service.ProfileService;
import com.zhentao.utils.JwtService;
import com.zhentao.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户资料控制器
 *
 * @author zhentao
 */
@RestController
@RequestMapping("/profile")
@CrossOrigin("*")
@Slf4j
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    /**
     * 创建用户资料
     *
     * @param profileDTO 用户资料DTO
     * @param token      用户令牌
     * @return 创建结果
     */
    @PostMapping("/create")
    public Result createProfile(@RequestBody @Validated ProfileDTO profileDTO, @RequestHeader("token") String token) {
        if (JwtService.verifyToken(token) != 1) {
            return Result.NO_LOGIN();
        }

        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Long userId = Long.valueOf(String.valueOf(claimsMap.get("userId")));
        // 设置用户ID
        profileDTO.setUserId(userId);
        return profileService.createProfile(profileDTO);
    }

    /**
     * 更新用户资料
     *
     * @param profileDTO 用户资料DTO
     * @param token      用户令牌
     * @return 更新结果
     */
    @PutMapping("/update")
    public Result updateProfile(@RequestBody @Validated ProfileDTO profileDTO, @RequestHeader("token") String token) {
        if (JwtService.verifyToken(token) != 1) {
            return Result.NO_LOGIN();
        }

        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Long userId = Long.valueOf(String.valueOf(claimsMap.get("userId")));

        // 验证是否为当前用户的资料
        if (!profileService.isProfileOwner(profileDTO.getProfileId(), userId)) {
            return Result.ERROR("无权修改他人资料");
        }

        return profileService.updateProfile(profileDTO);
    }

    /**
     * 获取用户资料详情（包含用户基本信息）
     *
     * @param profileId 资料ID
     * @param token     用户令牌
     * @return 用户资料详情
     */
    @GetMapping("/detail/{profileId}")
    public Result getProfileDetail(@PathVariable Long profileId, @RequestHeader("token") String token) {
        if (JwtService.verifyToken(token) != 1) {
            return Result.NO_LOGIN();
        }

        return profileService.getProfileDetailWithUser(profileId);
    }

    /**
     * 获取当前用户的资料详情
     *
     * @param token 用户令牌
     * @return 用户资料详情
     */
//    @GetMapping("/my")
//    public Result getMyProfile(@RequestHeader("token") String token) {
//        if (JwtService.verifyToken(token) != 1) {
//            return Result.NO_LOGIN();
//        }
//
//        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
//        Long userId = Long.valueOf(String.valueOf(claimsMap.get("userId")));
//
//        return profileService.getProfileByUserId(userId);
//    }

    /**
     * 删除用户资料
     *
     * @param profileId 资料ID
     * @param token     用户令牌
     * @return 删除结果
     */
    @DeleteMapping("/delete/{profileId}")
    public Result deleteProfile(@PathVariable Long profileId, @RequestHeader("token") String token) {
        if (JwtService.verifyToken(token) != 1) {
            return Result.NO_LOGIN();
        }

        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Long userId = Long.valueOf(String.valueOf(claimsMap.get("userId")));

        // 验证是否为当前用户的资料
        if (!profileService.isProfileOwner(profileId, userId)) {
            return Result.ERROR("无权删除他人资料");
        }

        return profileService.deleteProfile(profileId);
    }

    /**
     * 分页查询用户资料列表
     *
     * @param page  页码
     * @param size  每页数量
     * @param token 用户令牌
     * @return 用户资料列表
     */
    @GetMapping("/list")
    public Result listProfiles(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size,
                               @RequestHeader("token") String token) {
        if (JwtService.verifyToken(token) != 1) {
            return Result.NO_LOGIN();
        }

        return profileService.listProfilesWithUserInfo(page, size);
    }

    /**
     * 条件查询用户资料
     *
     * @param condition 查询条件
     * @param token     用户令牌
     * @return 符合条件的用户资料
     */
    @PostMapping("/search")
    public Result searchProfiles(@RequestBody Map<String, Object> condition,
                                 @RequestHeader("token") String token) {
        if (JwtService.verifyToken(token) != 1) {
            return Result.NO_LOGIN();
        }

        return profileService.searchProfiles(condition);
    }

    /**
     * 根据用户ID获取资料详情
     */
    @GetMapping("/user/{userId}")
    public Result getProfileByUserId(@PathVariable Long userId) {
        return profileService.getProfileByUserId(userId);
    }
}