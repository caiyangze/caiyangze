package com.zhentao.controller;

import com.zhentao.dto.WalletDTO;
import com.zhentao.service.WalletService;
import com.zhentao.utils.JwtService;
import com.zhentao.utils.Result;
import com.zhentao.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 钱包控制器
 *
 * @author zhentao
 */
@RestController
@RequestMapping("/wallet")
@CrossOrigin("*")
@Slf4j
@Validated
public class WalletController {

    @Autowired
    private WalletService walletService;

    /**
     * 获取用户钱包信息
     *
     * @param token 用户令牌
     * @return 钱包信息
     */
    @GetMapping("/info")
    public Result getWalletInfo(@RequestHeader("token") String token) {
        try {
            // 验证token
            if (JwtService.verifyToken(token) != 1) {
                return Result.NO_LOGIN();
            }

            // 获取用户ID
            Long userId = ThreadLocalUtil.getCurrentUserId();
            if (userId == null) {
                Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
                userId = Long.valueOf(String.valueOf(claimsMap.get("userId")));
            }

            return walletService.getWalletInfo(userId);
        } catch (Exception e) {
            log.error("获取钱包信息失败", e);
            return Result.ERROR("获取钱包信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取钱包交易记录
     *
     * @param token    用户令牌
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param type     交易类型（可选）
     * @return 交易记录列表
     */
    @GetMapping("/transactions")
    public Result getTransactions(@RequestHeader("token") String token,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "20") Integer pageSize,
                                  @RequestParam(required = false) Integer type) {
        try {
            // 验证token
            if (JwtService.verifyToken(token) != 1) {
                return Result.NO_LOGIN();
            }

            // 获取用户ID
            Long userId = ThreadLocalUtil.getCurrentUserId();
            if (userId == null) {
                Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
                userId = Long.valueOf(String.valueOf(claimsMap.get("userId")));
            }

            return walletService.getTransactions(userId, pageNum, pageSize, type);
        } catch (Exception e) {
            log.error("获取交易记录失败", e);
            return Result.ERROR("获取交易记录失败：" + e.getMessage());
        }
    }

    /**
     * 虚拟币充值
     *
     * @param token      用户令牌
     * @param walletDTO  充值信息
     * @return 充值结果
     */
    @PostMapping("/recharge")
    public Result recharge(@RequestHeader("token") String token,
                          @RequestBody @Validated WalletDTO walletDTO) {
        try {
            // 验证token
            if (JwtService.verifyToken(token) != 1) {
                return Result.NO_LOGIN();
            }

            // 获取用户ID
            Long userId = ThreadLocalUtil.getCurrentUserId();
            if (userId == null) {
                Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
                userId = Long.valueOf(String.valueOf(claimsMap.get("userId")));
            }

            walletDTO.setUserId(userId);
            return walletService.recharge(walletDTO);
        } catch (Exception e) {
            log.error("虚拟币充值失败", e);
            return Result.ERROR("虚拟币充值失败：" + e.getMessage());
        }
    }

    /**
     * 内部服务调用的虚拟币充值（不需要token验证）
     * 用于红娘服务等内部服务的退款操作
     *
     * @param walletDTO  充值信息
     * @return 充值结果
     */
    @PostMapping("/recharge/internal")
    public Result rechargeInternal(@RequestBody @Validated WalletDTO walletDTO) {
        try {
            // 内部调用，直接执行充值，不需要token验证
            return walletService.recharge(walletDTO);
        } catch (Exception e) {
            log.error("内部充值失败", e);
            return Result.ERROR("内部充值失败：" + e.getMessage());
        }
    }

    /**
     * 虚拟币消费
     *
     * @param token      用户令牌
     * @param walletDTO  消费信息
     * @return 消费结果
     */
    @PostMapping("/consume")
    public Result consume(@RequestHeader("token") String token,
                         @RequestBody @Validated WalletDTO walletDTO) {
        try {
            // 验证token
            if (JwtService.verifyToken(token) != 1) {
                return Result.NO_LOGIN();
            }

            // 获取用户ID
            Long userId = ThreadLocalUtil.getCurrentUserId();
            if (userId == null) {
                Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
                userId = Long.valueOf(String.valueOf(claimsMap.get("userId")));
            }

            walletDTO.setUserId(userId);
            return walletService.consume(walletDTO);
        } catch (Exception e) {
            log.error("虚拟币消费失败", e);
            return Result.ERROR("虚拟币消费失败：" + e.getMessage());
        }
    }

    /**
     * 现金收入
     *
     * @param token      用户令牌
     * @param walletDTO  收入信息
     * @return 收入结果
     */
    @PostMapping("/income")
    public Result income(@RequestHeader("token") String token,
                        @RequestBody @Validated WalletDTO walletDTO) {
        try {
            // 验证token
            if (JwtService.verifyToken(token) != 1) {
                return Result.NO_LOGIN();
            }

            // 获取用户ID
            Long userId = ThreadLocalUtil.getCurrentUserId();
            if (userId == null) {
                Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
                userId = Long.valueOf(String.valueOf(claimsMap.get("userId")));
            }

            walletDTO.setUserId(userId);
            return walletService.income(walletDTO);
        } catch (Exception e) {
            log.error("现金收入失败", e);
            return Result.ERROR("现金收入失败：" + e.getMessage());
        }
    }

    /**
     * 现金提现申请
     *
     * @param token      用户令牌
     * @param walletDTO  提现信息
     * @return 提现申请结果
     */
    @PostMapping("/withdraw")
    public Result withdraw(@RequestHeader("token") String token,
                          @RequestBody @Validated WalletDTO walletDTO) {
        try {
            // 验证token
            if (JwtService.verifyToken(token) != 1) {
                return Result.NO_LOGIN();
            }

            // 获取用户ID
            Long userId = ThreadLocalUtil.getCurrentUserId();
            if (userId == null) {
                Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
                userId = Long.valueOf(String.valueOf(claimsMap.get("userId")));
            }

            walletDTO.setUserId(userId);
            return walletService.withdraw(walletDTO);
        } catch (Exception e) {
            log.error("现金提现申请失败", e);
            return Result.ERROR("现金提现申请失败：" + e.getMessage());
        }
    }

    /**
     * 获取钱包统计信息
     *
     * @param token 用户令牌
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public Result getStatistics(@RequestHeader("token") String token) {
        try {
            // 验证token
            if (JwtService.verifyToken(token) != 1) {
                return Result.NO_LOGIN();
            }

            // 获取用户ID
            Long userId = ThreadLocalUtil.getCurrentUserId();
            if (userId == null) {
                Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
                userId = Long.valueOf(String.valueOf(claimsMap.get("userId")));
            }

            return walletService.getStatistics(userId);
        } catch (Exception e) {
            log.error("获取钱包统计信息失败", e);
            return Result.ERROR("获取钱包统计信息失败：" + e.getMessage());
        }
    }
}
