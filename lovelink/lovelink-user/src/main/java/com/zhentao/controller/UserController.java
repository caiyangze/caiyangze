package com.zhentao.controller;

import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhentao.dto.*;
import com.zhentao.pojo.TbFollow;
import com.zhentao.pojo.TbUser;
import com.zhentao.service.FollowService;
import com.zhentao.service.TbFollowService;
import com.zhentao.service.UserService;
import com.zhentao.utils.JwtService;
import com.zhentao.utils.PasswordEncoder;
import com.zhentao.utils.Result;
import com.zhentao.utils.IpUtils;
import com.zhentao.config.AmapIpLocation;
import io.lettuce.core.dynamic.annotation.Param;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 用户控制器
 *
 * @author zhentao
 */
@RestController
@RequestMapping("/user")
@CrossOrigin("*")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AmapIpLocation amapIpLocation;

    //    验证码登录
    @PostMapping("/sendLoginCode")
    public Result loginVerificationCode(@RequestBody @Validated SendCodeDTO sendCodeDTO) {
        TbUser existUser = userService.findByPhone(sendCodeDTO.getPhone());
        if (existUser != null) {
            Result sendVerificationCode = userService.sendVerificationCode(sendCodeDTO.getPhone());
            if (sendVerificationCode.getCode() == 200) {
                Map<String, Object> map = new HashMap<>();
                map.put("phone", sendCodeDTO.getPhone());
                map.put("userId", existUser.getUserId());
                String token = JwtService.createToken(map);
                return Result.OK(token);
            } else {
                return Result.ERROR("验证码错误");
            }
        }
        log.info("发送验证码请求：{}", sendCodeDTO.getPhone());
        return Result.ERROR("该手机号不存在，请先注册");
    }

    @PostMapping("/loginByCode")
    public Result verifyLoginCode(@RequestBody @Validated LoginCodeDTO verifyCodeDTO, HttpServletRequest request) {
        Result verifyResult = userService.verifyCodeWithResult(verifyCodeDTO.getPhone(), verifyCodeDTO.getCode());
        if (verifyResult.getCode() != 200) {
            return verifyResult;  // 验证码错误，直接返回错误结果
        }
        TbUser user = userService.findByPhone(verifyCodeDTO.getPhone());

        // 异步更新登录信息
        updateLoginInfoAsync(user, request);

        Map<String, Object> map = new HashMap<>();
        map.put("phone", user.getPhone());
        map.put("userId", user.getUserId());
        String token = JwtService.createToken(map);
        return Result.OK(token);
    }

    /**
     * 发送注册验证码
     *
     * @param sendCodeDTO 发送验证码DTO
     * @return 发送结果
     */
    @PostMapping("/sendCode")
    public Result sendVerificationCode(@RequestBody @Validated SendCodeDTO sendCodeDTO) {
        // 1. 校验手机号是否已注册
        TbUser existUser = userService.findByPhone(sendCodeDTO.getPhone());
        if (existUser != null) {
            return Result.ERROR("该手机号已被注册");
        }
        log.info("发送验证码请求：{}", sendCodeDTO.getPhone());
        return userService.sendVerificationCode(sendCodeDTO.getPhone());
    }

    /**
     * 验证验证码并注册用户
     *
     * @param verifyCodeDTO 验证码DTO，包含手机号、验证码和密码
     * @return 验证结果和注册结果
     */
    @PostMapping("/verifyCode")
    public Result verifyCode(@RequestBody @Validated VerifyCodeDTO verifyCodeDTO) {
        log.info("验证验证码并注册请求：{}", verifyCodeDTO.getPhone());

        // 1. 先验证验证码是否正确
        Result verifyResult = userService.verifyCodeWithResult(verifyCodeDTO.getPhone(), verifyCodeDTO.getCode());
        if (verifyResult.getCode() != 200) {
            return verifyResult;  // 验证码错误，直接返回错误结果
        }
//        // 2. 验证码正确，创建注册DTO并设置默认值
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setPhone(verifyCodeDTO.getPhone());
        registerDTO.setPassword(verifyCodeDTO.getPassword());
        Result registerResult = userService.register(registerDTO);
        // 4. 返回注册结果
        if (registerResult.getCode() == 200) {
            TbUser user = (TbUser) registerResult.getData();
            return Result.OK(user);
        } else {
            return registerResult;
        }
    }

    /**
     * 用户注册
     * <p>
     * 注意：由于包含文件上传，这里使用表单提交而不是JSON
     *
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result register(@RequestBody UserRegisterDTO registerDTO) {
        return userService.register(registerDTO);
    }

    /**
     * 用户登录
     *
     * @param loginDTO 登录参数DTO，包含手机号和密码
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        log.info("用户登录请求：{}", loginDTO.getPhone());

        // 1. 根据手机号查询用户
        TbUser user = userService.findByPhone(loginDTO.getPhone());
        if (user == null) {
            return Result.ERROR("用户不存在");
        }

        // 2. 验证密码是否正确
        if (!PasswordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return Result.ERROR("密码错误");
        }
        // 3. 登录成功，异步更新登录时间和IP信息
//        updateLoginInfoAsync(user, request);
        AmapIpLocation amapIpLocation = new AmapIpLocation();
        String provinceByIp = amapIpLocation.getProvinceByIp("");

        // 4. 清除敏感信息
        user.setPassword(null);
        Map<String, Object> map = new HashMap<>();
//        用于查询用户个人信息
        map.put("userId", user.getUserId());
//        用于回显用户手机号。提升用户体验
        map.put("phone", String.valueOf(user.getPhone()));
        String token = JwtService.createToken(map);
        // 5. 返回用户信息
        return Result.OK(token);
    }

    //    获取用户信息
    @PostMapping("/userInfo")
    public Result userInfo(@RequestBody Map<String, String> requestBody) {
        String token = requestBody.get("token");
        if (JwtService.verifyToken(token) == 1) {
            Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
            Integer userId = Integer.parseInt(String.valueOf( claimsMap.get("userId")));
            TbUser userInfo = userService.getById(userId);
            return Result.OK(userInfo);
        }
        return Result.NO_LOGIN();
    }
    @Autowired
    private TbFollowService followService;
    @RequestMapping("fensi")
    public Result fensi(@RequestHeader("token") String token){
        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Integer userId = Integer.parseInt(String.valueOf( claimsMap.get("userId")));
        QueryWrapper<TbFollow> followQueryWrapper=new QueryWrapper<>();
        followQueryWrapper.eq("followed_user_id",userId);
        followQueryWrapper.eq("follow_status",1);
        List<TbFollow> list = followService.list(followQueryWrapper);
        for (TbFollow tbFollow : list){
            TbUser user = userService.getById(tbFollow.getUserId());
            if(user.getAccountStatus()==1) {
                tbFollow.setUser(user);
            }
            QueryWrapper<TbFollow> followQueryWrapper1=new QueryWrapper<>();
            followQueryWrapper1.eq("user_id",userId);
            followQueryWrapper1.eq("followed_user_id",user.getUserId());
            TbFollow follow = followService.getOne(followQueryWrapper1);
            if(follow!=null&&follow.getFollowStatus()==1){
                tbFollow.setIsFollow(2);
            }else{
                tbFollow.setIsFollow(0);
            }
        }
        return Result.OK(list);
    }
    @RequestMapping("guanzhu")
    public Result guanzhu(@RequestHeader("token") String token){
        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Integer userId = Integer.parseInt(String.valueOf( claimsMap.get("userId")));
        QueryWrapper<TbFollow> followQueryWrapper=new QueryWrapper<>();
        followQueryWrapper.eq("user_id",userId);
        followQueryWrapper.eq("follow_status",1);
        List<TbFollow> list = followService.list(followQueryWrapper);
        for (TbFollow tbFollow : list){
            TbUser user = userService.getById(tbFollow.getFollowedUserId());
            if(user.getAccountStatus()==1) {
                tbFollow.setUser(user);
            }
            QueryWrapper<TbFollow> followQueryWrapper1=new QueryWrapper<>();
            followQueryWrapper1.eq("user_id",user.getUserId());
            followQueryWrapper1.eq("followed_user_id",userId);
            TbFollow follow = followService.getOne(followQueryWrapper1);
            if(follow!=null&&follow.getFollowStatus()==1){
                tbFollow.setIsFollow(2);
            }else{
                tbFollow.setIsFollow(1);
            }
        }
        return Result.OK(list);
    }

    /**
     * 心动速配 - 根据性别随机匹配用户
     *
     * @param heartMatchDTO 心动速配请求参数，gender: 0-女 1-男
     * @param token 用户令牌
     * @return 匹配结果
     */
    @PostMapping("/heartMatch")
    public Result heartMatch(@RequestBody HeartMatchDTO heartMatchDTO, @RequestHeader("token") String token) {
        // 验证token
        if (JwtService.verifyToken(token) != 1) {
            return Result.NO_LOGIN();
        }

        // 获取当前用户ID
        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Long currentUserId = Long.valueOf(String.valueOf(claimsMap.get("userId")));

        // 验证性别参数
        Integer gender = heartMatchDTO.getGender();
        if (gender == null || (gender != 0 && gender != 1)) {
            return Result.ERROR("请选择正确的性别");
        }

        log.info("用户{}发起心动速配，匹配性别：{}，跳过钱包扣减：{}",
            currentUserId, gender == 0 ? "女" : "男", heartMatchDTO.getSkipWalletDeduction());

        return userService.randomMatchByGender(gender, currentUserId, heartMatchDTO.getSkipWalletDeduction());
    }

    /**
     * 更新用户省份信息
     * @param requestBody 包含token和province的请求体
     * @return 更新结果
     */
    @PostMapping("/updateProvince")
    public Result updateProvince(@RequestBody Map<String, String> requestBody) {
        String token = requestBody.get("token");
        String province = requestBody.get("province");

        if (token == null || province == null) {
            return Result.ERROR("参数不能为空");
        }

        if (JwtService.verifyToken(token) != 1) {
            return Result.NO_LOGIN();
        }

        try {
            Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
            Long userId = Long.valueOf(String.valueOf(claimsMap.get("userId")));

            TbUser user = userService.getById(userId.intValue());
            if (user == null) {
                return Result.ERROR("用户不存在");
            }

            user.setLastLoginIp(province);
            boolean updated = userService.updateById(user);

            if (updated) {
                log.info("用户{}手动更新省份为: {}", userId, province);
                return Result.OK("省份更新成功");
            } else {
                return Result.ERROR("更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户省份失败: ", e);
            return Result.ERROR("更新失败");
        }
    }

    /**
     * 异步更新用户登录信息（登录时间和IP省份）
     * @param user 用户对象
     * @param request HTTP请求对象
     */
    @Async("taskExecutor")
    private void updateLoginInfoAsync(TbUser user, HttpServletRequest request) {
        // 立即更新登录时间，不依赖IP定位
        updateLoginTimeOnly(user);

        // 异步处理IP定位
        updateIpLocationAsync(user, request);
    }

    /**
     * 立即更新登录时间
     * @param user 用户对象
     */
    private void updateLoginTimeOnly(TbUser user) {
        try {
            // 只更新登录时间，确保快速响应
            user.setLastLoginTime(new Date());
            userService.updateById(user);
            log.info("用户{}登录时间更新成功", user.getUserId());
        } catch (Exception e) {
            log.error("更新用户登录时间失败: ", e);
        }
    }

    /**
     * 异步处理IP定位和省份更新
     * @param user 用户对象
     * @param request HTTP请求对象
     */
    @Async("taskExecutor")
    private void updateIpLocationAsync(TbUser user, HttpServletRequest request) {
        try {
            // 获取客户端IP
            String clientIp = IpUtils.getClientIp(request);
            log.info("异步处理用户{}的IP定位，IP: {}", user.getUserId(), clientIp);

            // 如果不是内网IP，尝试获取省份信息
            if (clientIp != null && !IpUtils.isInternalIp(clientIp)) {
                String province = amapIpLocation.getProvinceByIp(clientIp);
                log.info("高德API返回的省份: {}", province);

                if (province != null && !province.isEmpty()) {
                    // 重新查询用户信息，避免并发问题
                    TbUser currentUser = userService.getById(user.getUserId().intValue());
                    if (currentUser != null) {
                        log.info("更新前用户{}的省份: {}", user.getUserId(), currentUser.getLastLoginIp());
                        currentUser.setLastLoginIp(province);
                        boolean updateResult = userService.updateById(currentUser);
                        log.info("用户{}省份更新结果: {}, 新省份: {}", user.getUserId(), updateResult, province);

                        // 验证更新后的数据
                        TbUser verifyUser = userService.getById(user.getUserId().intValue());
                        log.info("验证更新后用户{}的省份: {}", user.getUserId(), verifyUser.getLastLoginIp());
                    }
                } else {
                    log.warn("无法获取IP{}对应的省份信息", clientIp);
                }
            } else {
                log.info("内网IP或本地IP，跳过省份获取: {}", clientIp);
            }
        } catch (Exception e) {
            log.error("异步更新用户IP省份失败: ", e);
        }
    }
}