package com.zhentao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhentao.dto.WalletDTO;
import com.zhentao.pojo.TbUserWallet;
import com.zhentao.pojo.TbWalletTransaction;
import com.zhentao.pojo.TbVoucher;
import com.zhentao.pojo.TbVoucherOrder;
import com.zhentao.service.TbUserWalletService;
import com.zhentao.service.TbWalletTransactionService;
import com.zhentao.service.TbVoucherService;
import com.zhentao.service.TbVoucherOrderService;
import com.zhentao.service.WalletService;
import com.zhentao.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 钱包服务实现类
 *
 * @author zhentao
 */
@Service
@Slf4j
public class WalletServiceImpl implements WalletService {

    @Autowired
    private TbUserWalletService userWalletService;

    @Autowired
    private TbWalletTransactionService walletTransactionService;

    @Autowired
    private TbVoucherService voucherService;

    @Autowired
    private TbVoucherOrderService voucherOrderService;

    @Override
    public Result getWalletInfo(Long userId) {
        try {
            // 查询用户钱包信息
            LambdaQueryWrapper<TbUserWallet> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbUserWallet::getUserId, userId);
            TbUserWallet wallet = userWalletService.getOne(wrapper);

            if (wallet == null) {
                // 如果钱包不存在，创建一个新的钱包
                createWallet(userId);
                wallet = userWalletService.getOne(wrapper);
            }

            return Result.OK(wallet);
        } catch (Exception e) {
            log.error("获取钱包信息失败，用户ID：{}", userId, e);
            return Result.ERROR("获取钱包信息失败");
        }
    }

    @Override
    public Result getTransactions(Long userId, Integer pageNum, Integer pageSize, Integer type) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<TbWalletTransaction> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbWalletTransaction::getUserId, userId);
            
            if (type != null) {
                wrapper.eq(TbWalletTransaction::getTransactionType, type);
            }
            
            wrapper.orderByDesc(TbWalletTransaction::getCreatedAt);

            // 分页查询
            Page<TbWalletTransaction> page = new Page<>(pageNum, pageSize);
            IPage<TbWalletTransaction> result = walletTransactionService.page(page, wrapper);

            return Result.OK(result);
        } catch (Exception e) {
            log.error("获取交易记录失败，用户ID：{}", userId, e);
            return Result.ERROR("获取交易记录失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result recharge(WalletDTO walletDTO) {
        // 原有的充值逻辑，不需要token验证（用于内部调用）
        return doRecharge(walletDTO);
    }



    /**
     * 执行充值的核心逻辑
     */
    private Result doRecharge(WalletDTO walletDTO) {
        try {
            Long userId = walletDTO.getUserId();
            Integer coinAmount = walletDTO.getCoinAmount();
            
            if (coinAmount == null || coinAmount <= 0) {
                return Result.ERROR("充值数量必须大于0");
            }

            // 获取用户钱包
            LambdaQueryWrapper<TbUserWallet> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbUserWallet::getUserId, userId);
            TbUserWallet wallet = userWalletService.getOne(wrapper);

            if (wallet == null) {
                return Result.ERROR("用户钱包不存在");
            }

            if (wallet.getWalletStatus() == 0) {
                return Result.ERROR("钱包已被冻结，无法充值");
            }

            // 更新钱包余额
            wallet.setCoinBalance(wallet.getCoinBalance() + coinAmount);
            wallet.setTotalRecharge(wallet.getTotalRecharge() + coinAmount);
            wallet.setUpdatedAt(new Date());

            boolean updateResult = userWalletService.updateById(wallet);
            if (!updateResult) {
                return Result.ERROR("充值失败");
            }

            // 记录交易流水
            TbWalletTransaction transaction = new TbWalletTransaction();
            transaction.setUserId(userId);
            transaction.setTransactionType(1); // 1-充值
            transaction.setCoinAmount(coinAmount);
            transaction.setCoinBalance(wallet.getCoinBalance());
            // 虚拟币充值时，交易金额设置为虚拟币数量对应的人民币金额（假设1币=0.1元）
            transaction.setTransactionAmount(new BigDecimal(coinAmount).multiply(new BigDecimal("0.1")));
            transaction.setBalance(wallet.getCashBalance()); // 设置现金余额
            transaction.setTransactionDesc(walletDTO.getTransactionDesc() != null ?
                walletDTO.getTransactionDesc() : "虚拟币充值");
            transaction.setRelatedId(walletDTO.getRelatedId());
            transaction.setCreatedAt(new Date());

            walletTransactionService.save(transaction);

            log.info("用户{}充值成功，充值数量：{}，当前余额：{}", userId, coinAmount, wallet.getCoinBalance());
            return Result.OK("充值成功");
        } catch (Exception e) {
            log.error("虚拟币充值失败", e);
            return Result.ERROR("充值失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result consume(WalletDTO walletDTO) {
        // 如果包含优惠券信息，调用带优惠券的消费方法
        if (walletDTO.getVoucherOrderId() != null && !walletDTO.getVoucherOrderId().isEmpty()) {
            return consumeWithVoucher(walletDTO);
        }

        try {
            Long userId = walletDTO.getUserId();
            Integer coinAmount = walletDTO.getCoinAmount();

            if (coinAmount == null || coinAmount <= 0) {
                return Result.ERROR("消费数量必须大于0");
            }

            // 获取用户钱包
            LambdaQueryWrapper<TbUserWallet> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbUserWallet::getUserId, userId);
            TbUserWallet wallet = userWalletService.getOne(wrapper);

            if (wallet == null) {
                return Result.ERROR("用户钱包不存在");
            }

            if (wallet.getWalletStatus() == 0) {
                return Result.ERROR("钱包已被冻结，无法消费");
            }

            if (wallet.getCoinBalance() < coinAmount) {
                return Result.ERROR("虚拟币余额不足");
            }

            // 更新钱包余额
            wallet.setCoinBalance(wallet.getCoinBalance() - coinAmount);
            wallet.setTotalConsume(wallet.getTotalConsume() + coinAmount);
            wallet.setUpdatedAt(new Date());

            boolean updateResult = userWalletService.updateById(wallet);
            if (!updateResult) {
                return Result.ERROR("消费失败");
            }

            // 记录交易流水
            TbWalletTransaction transaction = new TbWalletTransaction();
            transaction.setUserId(userId);
            transaction.setTransactionType(2); // 2-消费
            transaction.setCoinAmount(coinAmount);
            transaction.setCoinBalance(wallet.getCoinBalance());
            // 虚拟币消费时，交易金额设置为虚拟币数量对应的人民币金额（假设1币=0.1元）
            transaction.setTransactionAmount(new BigDecimal(coinAmount).multiply(new BigDecimal("0.1")));
            transaction.setBalance(wallet.getCashBalance()); // 设置现金余额
            transaction.setTransactionDesc(walletDTO.getTransactionDesc() != null ?
                walletDTO.getTransactionDesc() : "虚拟币消费");
            transaction.setRelatedId(walletDTO.getRelatedId());
            transaction.setCreatedAt(new Date());

            walletTransactionService.save(transaction);

            // 构建返回数据，包含余额信息
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("message", "消费成功");
            resultData.put("consumeAmount", coinAmount);
            resultData.put("beforeBalance", wallet.getCoinBalance() + coinAmount); // 扣减前余额
            resultData.put("afterBalance", wallet.getCoinBalance()); // 扣减后余额
            resultData.put("transactionDesc", walletDTO.getTransactionDesc());

            log.info("用户{}消费成功，消费数量：{}，扣减前余额：{}，当前余额：{}",
                userId, coinAmount, wallet.getCoinBalance() + coinAmount, wallet.getCoinBalance());
            return Result.OK(resultData);
        } catch (Exception e) {
            log.error("虚拟币消费失败", e);
            return Result.ERROR("消费失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result consumeWithVoucher(WalletDTO walletDTO) {
        try {
            Long userId = walletDTO.getUserId();
            Integer originalCoinAmount = walletDTO.getCoinAmount();
            String voucherOrderIdStr = walletDTO.getVoucherOrderId();
            Integer voucherDiscountAmount = walletDTO.getVoucherDiscountAmount();

            // 将字符串类型的优惠券订单ID转换为Long类型
            Long voucherOrderId = null;
            if (voucherOrderIdStr != null && !voucherOrderIdStr.isEmpty()) {
                try {
                    voucherOrderId = Long.parseLong(voucherOrderIdStr);
                } catch (NumberFormatException e) {
                    log.error("优惠券订单ID格式错误：{}", voucherOrderIdStr);
                    return Result.ERROR("优惠券订单ID格式错误");
                }
            }

            log.info("开始处理带优惠券的消费，用户ID：{}，原始消费金额：{}，优惠券订单ID：{}，优惠券抵扣金额：{}",
                userId, originalCoinAmount, voucherOrderId, voucherDiscountAmount);

            if (originalCoinAmount == null || originalCoinAmount <= 0) {
                return Result.ERROR("消费数量必须大于0");
            }

            // 验证优惠券
            if (voucherOrderId != null) {
                TbVoucherOrder voucherOrder = voucherOrderService.getById(voucherOrderId);
                if (voucherOrder == null) {
                    return Result.ERROR("优惠券不存在");
                }

                if (!voucherOrder.getUserId().equals(userId)) {
                    return Result.ERROR("优惠券不属于当前用户");
                }

                if (voucherOrder.getStatus() != 2) { // 2-已支付（未使用）
                    return Result.ERROR("优惠券状态异常，无法使用");
                }

                // 获取优惠券信息
                TbVoucher voucher = voucherService.getById(voucherOrder.getVoucherId());
                if (voucher == null) {
                    return Result.ERROR("优惠券信息不存在");
                }

                // 验证优惠券抵扣金额
                if (voucherDiscountAmount == null || !voucherDiscountAmount.equals(voucher.getActualValue().intValue())) {
                    return Result.ERROR("优惠券抵扣金额不正确");
                }
            }

            // 计算实际需要扣减的虚拟币数量
            Integer actualCoinAmount = originalCoinAmount;
            if (voucherDiscountAmount != null && voucherDiscountAmount > 0) {
                actualCoinAmount = Math.max(0, originalCoinAmount - voucherDiscountAmount);
            }

            // 获取用户钱包
            LambdaQueryWrapper<TbUserWallet> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbUserWallet::getUserId, userId);
            TbUserWallet wallet = userWalletService.getOne(wrapper);

            if (wallet == null) {
                return Result.ERROR("用户钱包不存在");
            }

            if (wallet.getWalletStatus() == 0) {
                return Result.ERROR("钱包已被冻结，无法消费");
            }

            if (wallet.getCoinBalance() < actualCoinAmount) {
                return Result.ERROR("虚拟币余额不足");
            }

            // 更新钱包余额
            wallet.setCoinBalance(wallet.getCoinBalance() - actualCoinAmount);
            wallet.setTotalConsume(wallet.getTotalConsume() + actualCoinAmount);
            wallet.setUpdatedAt(new Date());

            boolean updateResult = userWalletService.updateById(wallet);
            if (!updateResult) {
                return Result.ERROR("消费失败");
            }

            // 如果使用了优惠券，更新优惠券状态为已使用
            if (voucherOrderId != null) {
                log.info("开始更新优惠券状态，优惠券订单ID：{}", voucherOrderId);
                TbVoucherOrder voucherOrder = voucherOrderService.getById(voucherOrderId);
                if (voucherOrder == null) {
                    log.error("优惠券订单不存在，ID：{}", voucherOrderId);
                    return Result.ERROR("优惠券订单不存在");
                }

                log.info("优惠券订单更新前状态：{}，用户ID：{}", voucherOrder.getStatus(), voucherOrder.getUserId());
                voucherOrder.setStatus(3); // 3-已核销
                voucherOrder.setUseTime(java.time.LocalDateTime.now());
                voucherOrder.setUpdateTime(java.time.LocalDateTime.now());

                boolean voucherUpdateResult = voucherOrderService.updateById(voucherOrder);
                if (voucherUpdateResult) {
                    log.info("优惠券状态更新成功，订单ID：{}，新状态：3", voucherOrderId);
                } else {
                    log.error("优惠券状态更新失败，订单ID：{}", voucherOrderId);
                    return Result.ERROR("优惠券状态更新失败");
                }
            }

            // 记录交易流水
            TbWalletTransaction transaction = new TbWalletTransaction();
            transaction.setUserId(userId);
            transaction.setTransactionType(2); // 2-消费
            transaction.setCoinAmount(actualCoinAmount);
            transaction.setCoinBalance(wallet.getCoinBalance());
            // 虚拟币消费时，交易金额设置为虚拟币数量对应的人民币金额（假设1币=0.1元）
            transaction.setTransactionAmount(new BigDecimal(actualCoinAmount).multiply(new BigDecimal("0.1")));
            transaction.setBalance(wallet.getCashBalance()); // 设置现金余额

            String transactionDesc = walletDTO.getTransactionDesc() != null ?
                walletDTO.getTransactionDesc() : "虚拟币消费";
            if (voucherDiscountAmount != null && voucherDiscountAmount > 0) {
                transactionDesc += "（使用优惠券抵扣" + voucherDiscountAmount + "币）";
            }
            transaction.setTransactionDesc(transactionDesc);
            transaction.setRelatedId(walletDTO.getRelatedId());
            transaction.setCreatedAt(new Date());

            walletTransactionService.save(transaction);

            // 构建返回数据，包含余额信息和优惠券使用信息
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("message", "消费成功");
            resultData.put("originalAmount", originalCoinAmount); // 原始消费金额
            resultData.put("voucherDiscount", voucherDiscountAmount != null ? voucherDiscountAmount : 0); // 优惠券抵扣
            resultData.put("actualAmount", actualCoinAmount); // 实际扣减金额
            resultData.put("beforeBalance", wallet.getCoinBalance() + actualCoinAmount); // 扣减前余额
            resultData.put("afterBalance", wallet.getCoinBalance()); // 扣减后余额
            resultData.put("transactionDesc", transactionDesc);
            resultData.put("voucherUsed", voucherOrderId != null); // 是否使用了优惠券

            log.info("用户{}消费成功，原始金额：{}，优惠券抵扣：{}，实际扣减：{}，扣减前余额：{}，当前余额：{}",
                userId, originalCoinAmount, voucherDiscountAmount != null ? voucherDiscountAmount : 0,
                actualCoinAmount, wallet.getCoinBalance() + actualCoinAmount, wallet.getCoinBalance());
            return Result.OK(resultData);
        } catch (Exception e) {
            log.error("虚拟币消费失败", e);
            return Result.ERROR("消费失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result income(WalletDTO walletDTO) {
        try {
            Long userId = walletDTO.getUserId();
            BigDecimal amount = walletDTO.getTransactionAmount();
            
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Result.ERROR("收入金额必须大于0");
            }

            // 获取用户钱包
            LambdaQueryWrapper<TbUserWallet> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbUserWallet::getUserId, userId);
            TbUserWallet wallet = userWalletService.getOne(wrapper);

            if (wallet == null) {
                return Result.ERROR("用户钱包不存在");
            }

            if (wallet.getWalletStatus() == 0) {
                return Result.ERROR("钱包已被冻结，无法收入");
            }

            // 更新钱包余额
            wallet.setCashBalance(wallet.getCashBalance().add(amount));
            wallet.setTotalIncome(wallet.getTotalIncome().add(amount));
            wallet.setUpdatedAt(new Date());

            boolean updateResult = userWalletService.updateById(wallet);
            if (!updateResult) {
                return Result.ERROR("收入失败");
            }

            // 记录交易流水
            TbWalletTransaction transaction = new TbWalletTransaction();
            transaction.setUserId(userId);
            transaction.setTransactionType(3); // 3-收入
            transaction.setTransactionAmount(amount);
            transaction.setBalance(wallet.getCashBalance());
            transaction.setCoinAmount(0); // 现金收入时虚拟币数量为0
            transaction.setCoinBalance(wallet.getCoinBalance()); // 设置虚拟币余额
            transaction.setTransactionDesc(walletDTO.getTransactionDesc() != null ?
                walletDTO.getTransactionDesc() : "现金收入");
            transaction.setRelatedId(walletDTO.getRelatedId());
            transaction.setCreatedAt(new Date());

            walletTransactionService.save(transaction);

            log.info("用户{}收入成功，收入金额：{}，当前现金余额：{}", userId, amount, wallet.getCashBalance());
            return Result.OK("收入成功");
        } catch (Exception e) {
            log.error("现金收入失败", e);
            return Result.ERROR("收入失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result withdraw(WalletDTO walletDTO) {
        try {
            Long userId = walletDTO.getUserId();
            BigDecimal amount = walletDTO.getTransactionAmount();
            
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Result.ERROR("提现金额必须大于0");
            }

            if (amount.compareTo(new BigDecimal("10")) < 0) {
                return Result.ERROR("提现金额不能少于10元");
            }

            // 获取用户钱包
            LambdaQueryWrapper<TbUserWallet> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbUserWallet::getUserId, userId);
            TbUserWallet wallet = userWalletService.getOne(wrapper);

            if (wallet == null) {
                return Result.ERROR("用户钱包不存在");
            }

            if (wallet.getWalletStatus() == 0) {
                return Result.ERROR("钱包已被冻结，无法提现");
            }

            if (wallet.getCashBalance().compareTo(amount) < 0) {
                return Result.ERROR("现金余额不足");
            }

            // 更新钱包余额
            wallet.setCashBalance(wallet.getCashBalance().subtract(amount));
            wallet.setTotalWithdraw(wallet.getTotalWithdraw().add(amount));
            wallet.setUpdatedAt(new Date());

            boolean updateResult = userWalletService.updateById(wallet);
            if (!updateResult) {
                return Result.ERROR("提现申请失败");
            }

            // 记录交易流水
            TbWalletTransaction transaction = new TbWalletTransaction();
            transaction.setUserId(userId);
            transaction.setTransactionType(4); // 4-提现
            transaction.setTransactionAmount(amount);
            transaction.setBalance(wallet.getCashBalance());
            transaction.setCoinAmount(0); // 现金提现时虚拟币数量为0
            transaction.setCoinBalance(wallet.getCoinBalance()); // 设置虚拟币余额
            transaction.setTransactionDesc(walletDTO.getTransactionDesc() != null ?
                walletDTO.getTransactionDesc() : "现金提现");
            transaction.setRelatedId(walletDTO.getRelatedId());
            transaction.setCreatedAt(new Date());

            walletTransactionService.save(transaction);

            log.info("用户{}提现申请成功，提现金额：{}，当前现金余额：{}", userId, amount, wallet.getCashBalance());
            return Result.OK("提现申请成功，请等待审核");
        } catch (Exception e) {
            log.error("现金提现申请失败", e);
            return Result.ERROR("提现申请失败：" + e.getMessage());
        }
    }

    @Override
    public Result getStatistics(Long userId) {
        try {
            // 获取钱包信息
            LambdaQueryWrapper<TbUserWallet> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbUserWallet::getUserId, userId);
            TbUserWallet wallet = userWalletService.getOne(wrapper);

            if (wallet == null) {
                return Result.ERROR("用户钱包不存在");
            }

            // 构建统计信息
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("coinBalance", wallet.getCoinBalance());
            statistics.put("cashBalance", wallet.getCashBalance());
            statistics.put("totalRecharge", wallet.getTotalRecharge());
            statistics.put("totalConsume", wallet.getTotalConsume());
            statistics.put("totalIncome", wallet.getTotalIncome());
            statistics.put("totalWithdraw", wallet.getTotalWithdraw());
            statistics.put("walletStatus", wallet.getWalletStatus());

            return Result.OK(statistics);
        } catch (Exception e) {
            log.error("获取钱包统计信息失败，用户ID：{}", userId, e);
            return Result.ERROR("获取统计信息失败");
        }
    }

    @Override
    public Result createWallet(Long userId) {
        try {
            // 检查钱包是否已存在
            LambdaQueryWrapper<TbUserWallet> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbUserWallet::getUserId, userId);
            TbUserWallet existingWallet = userWalletService.getOne(wrapper);

            if (existingWallet != null) {
                return Result.OK("钱包已存在");
            }

            // 创建新钱包
            TbUserWallet wallet = new TbUserWallet();
            wallet.setUserId(userId);
            wallet.setCoinBalance(0);
            wallet.setTotalRecharge(0);
            wallet.setTotalConsume(0);
            wallet.setCashBalance(BigDecimal.ZERO);
            wallet.setTotalIncome(BigDecimal.ZERO);
            wallet.setTotalWithdraw(BigDecimal.ZERO);
            wallet.setWalletStatus(1); // 1-正常
            wallet.setCreatedAt(new Date());
            wallet.setUpdatedAt(new Date());

            boolean saveResult = userWalletService.save(wallet);
            if (saveResult) {
                log.info("用户{}钱包创建成功", userId);
                return Result.OK("钱包创建成功");
            } else {
                return Result.ERROR("钱包创建失败");
            }
        } catch (Exception e) {
            log.error("创建钱包失败，用户ID：{}", userId, e);
            return Result.ERROR("钱包创建失败：" + e.getMessage());
        }
    }

    @Override
    public Result checkWalletStatus(Long userId) {
        try {
            LambdaQueryWrapper<TbUserWallet> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbUserWallet::getUserId, userId);
            TbUserWallet wallet = userWalletService.getOne(wrapper);

            if (wallet == null) {
                return Result.ERROR("用户钱包不存在");
            }

            Map<String, Object> status = new HashMap<>();
            status.put("walletStatus", wallet.getWalletStatus());
            status.put("statusDesc", wallet.getWalletStatus() == 1 ? "正常" : "冻结");

            return Result.OK(status);
        } catch (Exception e) {
            log.error("检查钱包状态失败，用户ID：{}", userId, e);
            return Result.ERROR("检查钱包状态失败");
        }
    }

    @Override
    public Result updateWalletStatus(Long userId, Integer status) {
        try {
            LambdaQueryWrapper<TbUserWallet> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbUserWallet::getUserId, userId);
            TbUserWallet wallet = userWalletService.getOne(wrapper);

            if (wallet == null) {
                return Result.ERROR("用户钱包不存在");
            }

            wallet.setWalletStatus(status);
            wallet.setUpdatedAt(new Date());

            boolean updateResult = userWalletService.updateById(wallet);
            if (updateResult) {
                log.info("用户{}钱包状态更新成功，新状态：{}", userId, status);
                return Result.OK("钱包状态更新成功");
            } else {
                return Result.ERROR("钱包状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新钱包状态失败，用户ID：{}，状态：{}", userId, status, e);
            return Result.ERROR("钱包状态更新失败：" + e.getMessage());
        }
    }
}
