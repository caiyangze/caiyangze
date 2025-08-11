package com.zhentao.service;

import com.zhentao.dto.WalletDTO;
import com.zhentao.utils.Result;

/**
 * 钱包服务接口
 *
 * @author zhentao
 */
public interface WalletService {

    /**
     * 获取用户钱包信息
     *
     * @param userId 用户ID
     * @return 钱包信息
     */
    Result getWalletInfo(Long userId);

    /**
     * 获取钱包交易记录
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param type     交易类型（可选）
     * @return 交易记录列表
     */
    Result getTransactions(Long userId, Integer pageNum, Integer pageSize, Integer type);

    /**
     * 虚拟币充值
     *
     * @param walletDTO 充值信息
     * @return 充值结果
     */
    Result recharge(WalletDTO walletDTO);

    /**
     * 虚拟币消费
     *
     * @param walletDTO 消费信息
     * @return 消费结果
     */
    Result consume(WalletDTO walletDTO);

    /**
     * 虚拟币消费（支持优惠券）
     *
     * @param walletDTO 消费信息（包含优惠券信息）
     * @return 消费结果
     */
    Result consumeWithVoucher(WalletDTO walletDTO);

    /**
     * 现金收入
     *
     * @param walletDTO 收入信息
     * @return 收入结果
     */
    Result income(WalletDTO walletDTO);

    /**
     * 现金提现申请
     *
     * @param walletDTO 提现信息
     * @return 提现申请结果
     */
    Result withdraw(WalletDTO walletDTO);

    /**
     * 获取钱包统计信息
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    Result getStatistics(Long userId);

    /**
     * 创建用户钱包（注册时调用）
     *
     * @param userId 用户ID
     * @return 创建结果
     */
    Result createWallet(Long userId);

    /**
     * 检查钱包状态
     *
     * @param userId 用户ID
     * @return 钱包状态检查结果
     */
    Result checkWalletStatus(Long userId);

    /**
     * 冻结/解冻钱包
     *
     * @param userId 用户ID
     * @param status 钱包状态：0-冻结，1-正常
     * @return 操作结果
     */
    Result updateWalletStatus(Long userId, Integer status);
}
