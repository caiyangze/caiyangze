package com.zhentao.controller;

import com.zhentao.dto.WalletDTO;
import com.zhentao.service.TbSignService;
import com.zhentao.service.TbUserWalletService;
import com.zhentao.service.WalletService;
import com.zhentao.utils.JwtService;
import com.zhentao.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 王恒飞
 * @date 2025/7/21
 * @ClassName SignController
 */
@RestController
@RequestMapping("/sign")
@Slf4j
public class SignController {

    @Autowired
    private TbSignService signService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private WalletService walletService;
    @Autowired
    private TbUserWalletService userWalletService;

    /**
     * 用户签到Redis Key前缀
     */
    private static final String USER_SIGN_KEY = "sign:";

    /**
     * 每日签到奖励金币数量
     */
    private static final int DAILY_SIGN_REWARD_COINS = 5;

    /**
     * 用户签到
     * @param token 用户token
     * @return 签到结果
     */
    @PostMapping("")
    public Result sign(@RequestHeader("token") String token) {
        // 1. 验证token
        if (JwtService.verifyToken(token) != 1) {
            return Result.NO_LOGIN();
        }

        // 2. 获取当前登录用户
        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Integer userId = Integer.parseInt(String.valueOf(claimsMap.get("userId")));

        // 3. 获取日期
        LocalDateTime now = LocalDateTime.now();

        // 4. 拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;

        // 5. 获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();

        // 6. 检查今天是否已经签到
        Boolean hasSignedToday = stringRedisTemplate.opsForValue().getBit(key, dayOfMonth - 1);
        if (Boolean.TRUE.equals(hasSignedToday)) {
            return Result.ERROR("今天已经签到过了");
        }

        // 7. 写入Redis SETBIT key offset 1
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);

        // 8. 设置过期时间（保留12个月的数据）
        // 为签到Key设置过期时间，避免数据无限增长
        stringRedisTemplate.expire(key, 365, java.util.concurrent.TimeUnit.DAYS);

        // 9. 签到奖励：给用户钱包添加5个金币
        try {
            WalletDTO walletDTO = new WalletDTO();
            walletDTO.setUserId(Long.valueOf(userId));
            walletDTO.setCoinAmount(DAILY_SIGN_REWARD_COINS); // 签到奖励金币
            walletDTO.setTransactionDesc("每日签到奖励");
            walletDTO.setRelatedId(null); // 签到相关ID可以为空

            Result rechargeResult = walletService.recharge(walletDTO);
            if (rechargeResult.getCode() != 200) {
                // 金币奖励失败，记录日志但不影响签到成功
                log.warn("签到金币奖励失败，用户ID：{}，原因：{}", userId, rechargeResult.getMessage());
            } else {
                log.info("用户{}签到成功，获得{}个金币奖励", userId, DAILY_SIGN_REWARD_COINS);
            }
        } catch (Exception e) {
            // 金币奖励异常，记录日志但不影响签到成功
            log.error("签到金币奖励异常，用户ID：{}，异常：{}", userId, e.getMessage(), e);
        }

        return Result.OK("签到成功，获得" + DAILY_SIGN_REWARD_COINS + "个金币奖励！");
    }

    /**
     * 统计连续签到天数
     * @param token 用户token
     * @return 连续签到天数
     */
    @GetMapping("/count")
    public Result signCount(@RequestHeader("token") String token) {
        // 1. 验证token
        if (JwtService.verifyToken(token) != 1) {
            return Result.NO_LOGIN();
        }

        // 2. 获取当前登录用户
        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Integer userId = Integer.parseInt(String.valueOf(claimsMap.get("userId")));

        // 3. 获取日期
        LocalDateTime now = LocalDateTime.now();

        // 4. 拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;

        // 5. 获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();

        // 6. 从今天开始向前遍历，统计连续签到天数
        int count = 0;
        for (int i = dayOfMonth; i > 0; i--) {
            // 检查第i天是否签到（注意BitMap索引从0开始）
            Boolean hasSigned = stringRedisTemplate.opsForValue().getBit(key, i - 1);
            if (Boolean.TRUE.equals(hasSigned)) {
                count++;
            } else {
                // 遇到未签到的天就停止
                break;
            }
        }

        return Result.OK(count);
    }

    /**
     * 获取本月签到记录
     * @param token 用户token
     * @return 本月签到记录的BitMap
     */
    @GetMapping("/month")
    public Result getMonthSignRecord(@RequestHeader("token") String token) {
        // 1. 验证token
        if (JwtService.verifyToken(token) != 1) {
            return Result.NO_LOGIN();
        }

        // 2. 获取当前登录用户
        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Integer userId = Integer.parseInt(String.valueOf(claimsMap.get("userId")));

        // 3. 获取日期
        LocalDateTime now = LocalDateTime.now();

        // 4. 拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;

        // 5. 获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();

        // 6. 获取本月截止今天为止的所有的签到记录
        // 构建一个表示签到记录的数字
        long signRecord = 0L;
        for (int i = 1; i <= dayOfMonth; i++) {
            Boolean hasSigned = stringRedisTemplate.opsForValue().getBit(key, i - 1);
            if (Boolean.TRUE.equals(hasSigned)) {
                // 如果第i天签到了，就在对应的位上设置为1
                signRecord |= (1L << (i - 1));
            }
        }

        return Result.OK(signRecord);
    }

    /**
     * 检查今天是否已签到
     * @param token 用户token
     * @return 今天是否已签到
     */
    @GetMapping("/today")
    public Result checkTodaySign(@RequestHeader("token") String token) {
        // 1. 验证token
        if (JwtService.verifyToken(token) != 1) {
            return Result.NO_LOGIN();
        }

        // 2. 获取当前登录用户
        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Integer userId = Integer.parseInt(String.valueOf(claimsMap.get("userId")));

        // 3. 获取日期
        LocalDateTime now = LocalDateTime.now();

        // 4. 拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;

        // 5. 获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();

        // 6. 检查今天是否已经签到
        Boolean hasSignedToday = stringRedisTemplate.opsForValue().getBit(key, dayOfMonth - 1);

        return Result.OK(Boolean.TRUE.equals(hasSignedToday));
    }
}
