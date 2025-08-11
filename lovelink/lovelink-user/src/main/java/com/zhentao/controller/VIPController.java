package com.zhentao.controller;

import com.zhentao.pojo.TbUser;
import com.zhentao.pojo.TbUserWallet;
import com.zhentao.pojo.TbVipOrder;
import com.zhentao.pojo.TbWalletTransaction;
import com.zhentao.service.TbUserService;
import com.zhentao.service.TbUserWalletService;
import com.zhentao.service.TbVipOrderService;
import com.zhentao.service.TbWalletTransactionService;
import com.zhentao.utils.JwtService;
import com.zhentao.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * VIP控制器
 * 处理VIP相关的业务逻辑，包括VIP套餐查询、订单创建、支付等
 *
 * @author 王恒飞
 * @date 2025/7/22
 * @ClassName VIPController
 */
@RestController
@RequestMapping("/VIP")
@Slf4j
public class VIPController {

    @Autowired
    private TbVipOrderService vipOrderService;
    @Autowired
    private TbUserService userService;
    @Autowired
    private TbUserWalletService userWalletService;
    @Autowired
    private TbWalletTransactionService walletTransactionService;

    /**
     * 获取VIP套餐列表
     * 返回可选的VIP类型及其价格信息
     *
     * @return VIP套餐列表
     */
    @GetMapping("/packages")
    public Result getVipPackages() {
        try {
            List<Map<String, Object>> packages = new ArrayList<>();

            // 月度VIP
            Map<String, Object> monthlyVip = new HashMap<>();
            monthlyVip.put("vipType", 1);
            monthlyVip.put("name", "月度VIP");
            monthlyVip.put("duration", "1个月");
            monthlyVip.put("originalPrice", new BigDecimal("29.90"));
            monthlyVip.put("currentPrice", new BigDecimal("19.90"));
            monthlyVip.put("discount", "限时优惠");
            monthlyVip.put("features", Arrays.asList(
                "无限制查看用户资料",
                "每日10次超级喜欢",
                "专属VIP标识",
                "优先匹配推荐"
            ));
            packages.add(monthlyVip);

            // 季度VIP
            Map<String, Object> quarterlyVip = new HashMap<>();
            quarterlyVip.put("vipType", 2);
            quarterlyVip.put("name", "季度VIP");
            quarterlyVip.put("duration", "3个月");
            quarterlyVip.put("originalPrice", new BigDecimal("89.70"));
            quarterlyVip.put("currentPrice", new BigDecimal("49.90"));
            quarterlyVip.put("discount", "超值推荐");
            quarterlyVip.put("features", Arrays.asList(
                "月度VIP所有特权",
                "每日20次超级喜欢",
                "查看谁喜欢了我",
                "无限制撤回操作"
            ));
            packages.add(quarterlyVip);

            // 年度VIP
            Map<String, Object> yearlyVip = new HashMap<>();
            yearlyVip.put("vipType", 3);
            yearlyVip.put("name", "年度VIP");
            yearlyVip.put("duration", "12个月");
            yearlyVip.put("originalPrice", new BigDecimal("358.80"));
            yearlyVip.put("currentPrice", new BigDecimal("99.90"));
            yearlyVip.put("discount", "最划算");
            yearlyVip.put("features", Arrays.asList(
                "季度VIP所有特权",
                "无限次超级喜欢",
                "专属客服服务",
                "优先活动参与权"
            ));
            packages.add(yearlyVip);

            return Result.success("获取VIP套餐成功", packages);
        } catch (Exception e) {
            log.error("获取VIP套餐失败", e);
            return Result.error("获取VIP套餐失败");
        }
    }

    /**
     * 创建VIP订单
     * 用户选择VIP类型后创建待支付订单
     *
     * @param requestData 请求数据，包含vipType
     * @param token 用户token
     * @return 订单信息
     */
    @PostMapping("/createOrder")
    public Result createVipOrder(@RequestBody Map<String, Object> requestData,
                                @RequestHeader("token") String token) {
        try {
            // 获取当前用户ID
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.NO_LOGIN("用户未登录");
            }

            // 获取VIP类型
            Integer vipType = (Integer) requestData.get("vipType");
            if (vipType == null || vipType < 1 || vipType > 3) {
                return Result.error("VIP类型参数错误");
            }

            // 根据VIP类型设置价格和时长
            BigDecimal amount;
            BigDecimal payAmount;
            String vipName;
            int durationMonths;

            switch (vipType) {
                case 1: // 月度VIP
                    amount = new BigDecimal("29.90");
                    payAmount = new BigDecimal("19.90");
                    vipName = "月度VIP";
                    durationMonths = 1;
                    break;
                case 2: // 季度VIP
                    amount = new BigDecimal("89.70");
                    payAmount = new BigDecimal("49.90");
                    vipName = "季度VIP";
                    durationMonths = 3;
                    break;
                case 3: // 年度VIP
                    amount = new BigDecimal("358.80");
                    payAmount = new BigDecimal("99.90");
                    vipName = "年度VIP";
                    durationMonths = 12;
                    break;
                default:
                    return Result.error("不支持的VIP类型");
            }

            // 创建订单
            TbVipOrder vipOrder = new TbVipOrder();
            vipOrder.setUserId(userId);
            vipOrder.setOrderNo(generateOrderNo());
            vipOrder.setVipType(vipType);
            vipOrder.setAmount(amount);
            vipOrder.setPayAmount(payAmount);
            vipOrder.setDiscountAmount(amount.subtract(payAmount));
            vipOrder.setOrderStatus(0); // 待支付
            vipOrder.setCreatedAt(new Date());
            vipOrder.setUpdatedAt(new Date());

            // 计算VIP时间
            Calendar calendar = Calendar.getInstance();
            Date startTime = calendar.getTime();
            calendar.add(Calendar.MONTH, durationMonths);
            Date endTime = calendar.getTime();

            vipOrder.setStartTime(startTime);
            vipOrder.setEndTime(endTime);

            // 保存订单
            boolean saved = vipOrderService.save(vipOrder);
            if (!saved) {
                return Result.error("创建订单失败");
            }

            // 返回订单信息
            Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put("orderId", vipOrder.getOrderId());
            orderInfo.put("orderNo", vipOrder.getOrderNo());
            orderInfo.put("vipType", vipType);
            orderInfo.put("vipName", vipName);
            orderInfo.put("amount", amount);
            orderInfo.put("payAmount", payAmount);
            orderInfo.put("discountAmount", vipOrder.getDiscountAmount());
            orderInfo.put("startTime", startTime);
            orderInfo.put("endTime", endTime);

            return Result.success("创建订单成功", orderInfo);
        } catch (Exception e) {
            log.error("创建VIP订单失败", e);
            return Result.error("创建订单失败");
        }
    }

    /**
     * 获取支付方式列表
     * 返回可用的支付方式
     *
     * @return 支付方式列表
     */
    @GetMapping("/payMethods")
    public Result getPayMethods() {
        try {
            List<Map<String, Object>> payMethods = new ArrayList<>();

            // 微信支付
            Map<String, Object> wechatPay = new HashMap<>();
            wechatPay.put("payType", 1);
            wechatPay.put("name", "微信支付");
            wechatPay.put("icon", "wechat");
            wechatPay.put("description", "使用微信快捷支付");
            wechatPay.put("enabled", true);
            payMethods.add(wechatPay);

            // 支付宝支付
            Map<String, Object> alipay = new HashMap<>();
            alipay.put("payType", 2);
            alipay.put("name", "支付宝");
            alipay.put("icon", "alipay");
            alipay.put("description", "使用支付宝安全支付");
            alipay.put("enabled", true);
            payMethods.add(alipay);

            // 苹果支付
            Map<String, Object> applePay = new HashMap<>();
            applePay.put("payType", 3);
            applePay.put("name", "Apple Pay");
            applePay.put("icon", "apple");
            applePay.put("description", "使用Apple Pay便捷支付");
            applePay.put("enabled", true);
            payMethods.add(applePay);

            // 其他支付
            Map<String, Object> otherPay = new HashMap<>();
            otherPay.put("payType", 4);
            otherPay.put("name", "其他支付");
            otherPay.put("icon", "other");
            otherPay.put("description", "银行卡等其他支付方式");
            otherPay.put("enabled", true);
            payMethods.add(otherPay);

            return Result.success("获取支付方式成功", payMethods);
        } catch (Exception e) {
            log.error("获取支付方式失败", e);
            return Result.error("获取支付方式失败");
        }
    }

    /**
     * 处理VIP支付（假支付）
     * 模拟支付流程，直接标记订单为已支付，并更新用户VIP状态
     *
     * @param requestData 请求数据，包含orderId和payType
     * @param token 用户token
     * @return 支付结果
     */
    @PostMapping("/pay")
    @Transactional(rollbackFor = Exception.class)
    public Result processVipPayment(@RequestBody Map<String, Object> requestData,
                                   @RequestHeader("token") String token) {
        try {
            // 获取当前用户ID
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.NO_LOGIN("用户未登录");
            }

            // 获取订单ID和支付方式
            Long orderId = Long.valueOf(String.valueOf(requestData.get("orderId")));
            Integer payType = (Integer) requestData.get("payType");

            if (orderId == null) {
                return Result.error("订单ID不能为空");
            }

            if (payType == null || payType < 1 || payType > 4) {
                return Result.error("支付方式参数错误");
            }

            // 查询订单
            TbVipOrder vipOrder = vipOrderService.getById(orderId);
            if (vipOrder == null) {
                return Result.error("订单不存在");
            }

            // 验证订单所有者
            if (!vipOrder.getUserId().equals(userId)) {
                return Result.error("无权操作此订单");
            }

            // 检查订单状态
            if (vipOrder.getOrderStatus() != 0) {
                return Result.error("订单状态异常，无法支付");
            }

            // 模拟支付处理
            String payMethodName = getPayMethodName(payType);
            String transactionId = generateTransactionId(payType);

            // 更新订单状态
            vipOrder.setPayType(payType);
            vipOrder.setPayTime(new Date());
            vipOrder.setTransactionId(transactionId);
            vipOrder.setOrderStatus(1); // 已支付
            vipOrder.setUpdatedAt(new Date());

            boolean updated = vipOrderService.updateById(vipOrder);
            if (!updated) {
                return Result.error("支付处理失败");
            }

            // 更新用户VIP状态
            TbUser user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 设置用户角色，需要考虑用户是否已经是红娘
            if (user.getUserRole() == 3) {
                // 如果用户已经是红娘，升级为VIP+红娘
                user.setUserRole(4); // 4-VIP+红娘
            } else {
                // 否则设置为VIP用户
                user.setUserRole(2); // 2-VIP用户
            }
            user.setIsVip(1); // 设置为VIP
            user.setVipExpireTime(vipOrder.getEndTime()); // 设置VIP过期时间
            user.setUpdatedAt(new Date());

            boolean userUpdated = userService.updateById(user);
            if (!userUpdated) {
                return Result.error("更新用户VIP状态失败");
            }

            // 创建钱包交易记录
            TbWalletTransaction transaction = new TbWalletTransaction();
            transaction.setUserId(userId);
            transaction.setTransactionType(1); // 1-充值（VIP购买视为充值类型）
            transaction.setTransactionAmount(vipOrder.getPayAmount());
            transaction.setCoinAmount(0); // VIP购买不涉及虚拟币
            transaction.setTransactionDesc("购买VIP会员 - " + getVipTypeName(vipOrder.getVipType()));
            transaction.setRelatedId(vipOrder.getOrderNo()); // 关联订单号
            transaction.setCreatedAt(new Date());

            // 获取用户当前钱包余额（用于记录交易后余额）
            TbUserWallet userWallet = userWalletService.lambdaQuery()
                    .eq(TbUserWallet::getUserId, userId)
                    .one();

            if (userWallet != null) {
                transaction.setBalance(userWallet.getCashBalance());
                transaction.setCoinBalance(userWallet.getCoinBalance());
            } else {
                transaction.setBalance(BigDecimal.ZERO);
                transaction.setCoinBalance(0);
            }

            boolean transactionSaved = walletTransactionService.save(transaction);
            if (!transactionSaved) {
                return Result.error("创建交易记录失败");
            }

            // 返回支付结果
            Map<String, Object> payResult = new HashMap<>();
            payResult.put("orderId", orderId);
            payResult.put("orderNo", vipOrder.getOrderNo());
            payResult.put("payType", payType);
            payResult.put("payMethodName", payMethodName);
            payResult.put("payAmount", vipOrder.getPayAmount());
            payResult.put("transactionId", transactionId);
            payResult.put("payTime", vipOrder.getPayTime());
            payResult.put("vipStartTime", vipOrder.getStartTime());
            payResult.put("vipEndTime", vipOrder.getEndTime());

            log.info("用户{}成功购买VIP，订单号：{}，支付方式：{}", userId, vipOrder.getOrderNo(), payMethodName);

            return Result.success("支付成功", payResult);
        } catch (Exception e) {
            log.error("VIP支付处理失败", e);
            return Result.error("支付处理失败");
        }
    }

    /**
     * 获取用户VIP状态
     *
     * @param token 用户token
     * @return VIP状态信息
     */
    @GetMapping("/status")
    public Result getUserVipStatus(@RequestHeader("token") String token) {
        try {
            // 获取当前用户ID
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.NO_LOGIN("用户未登录");
            }

            // 查询用户信息
            TbUser user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 构建VIP状态信息
            Map<String, Object> vipStatus = new HashMap<>();
            vipStatus.put("userId", userId);
            vipStatus.put("isVip", user.getIsVip());
            vipStatus.put("userRole", user.getUserRole());
            vipStatus.put("vipExpireTime", user.getVipExpireTime());

            // 判断VIP是否过期
            boolean isExpired = false;
            if (user.getVipExpireTime() != null) {
                isExpired = user.getVipExpireTime().before(new Date());
                if (isExpired && user.getIsVip() == 1) {
                    // 如果VIP已过期但状态还是VIP，更新状态
                    user.setIsVip(0);

                    // VIP过期后，用户角色保持不变（1-普通用户，2-红娘）

                    user.setUpdatedAt(new Date());
                    userService.updateById(user);

                    vipStatus.put("isVip", 0);
                    vipStatus.put("userRole", user.getUserRole());
                }
            }

            vipStatus.put("isExpired", isExpired);
            vipStatus.put("userRoleName", getUserRoleName(user.getUserRole()));

            return Result.success("获取VIP状态成功", vipStatus);
        } catch (Exception e) {
            log.error("获取用户VIP状态失败", e);
            return Result.error("获取VIP状态失败");
        }
    }

    /**
     * 查询VIP订单详情
     *
     * @param orderId 订单ID
     * @param token 用户token
     * @return 订单详情
     */
    @GetMapping("/order/{orderId}")
    public Result getVipOrderDetail(@PathVariable Long orderId,
                                   @RequestHeader("token") String token) {
        try {
            // 获取当前用户ID
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.NO_LOGIN("用户未登录");
            }

            // 查询订单
            TbVipOrder vipOrder = vipOrderService.getById(orderId);
            if (vipOrder == null) {
                return Result.error("订单不存在");
            }

            // 验证订单所有者
            if (!vipOrder.getUserId().equals(userId)) {
                return Result.error("无权查看此订单");
            }

            // 构建返回数据
            Map<String, Object> orderDetail = new HashMap<>();
            orderDetail.put("orderId", vipOrder.getOrderId());
            orderDetail.put("orderNo", vipOrder.getOrderNo());
            orderDetail.put("vipType", vipOrder.getVipType());
            orderDetail.put("vipTypeName", getVipTypeName(vipOrder.getVipType()));
            orderDetail.put("amount", vipOrder.getAmount());
            orderDetail.put("payAmount", vipOrder.getPayAmount());
            orderDetail.put("discountAmount", vipOrder.getDiscountAmount());
            orderDetail.put("payType", vipOrder.getPayType());
            orderDetail.put("payTypeName", vipOrder.getPayType() != null ? getPayMethodName(vipOrder.getPayType()) : null);
            orderDetail.put("payTime", vipOrder.getPayTime());
            orderDetail.put("transactionId", vipOrder.getTransactionId());
            orderDetail.put("startTime", vipOrder.getStartTime());
            orderDetail.put("endTime", vipOrder.getEndTime());
            orderDetail.put("orderStatus", vipOrder.getOrderStatus());
            orderDetail.put("orderStatusName", getOrderStatusName(vipOrder.getOrderStatus()));
            orderDetail.put("createdAt", vipOrder.getCreatedAt());

            return Result.success("获取订单详情成功", orderDetail);
        } catch (Exception e) {
            log.error("获取VIP订单详情失败", e);
            return Result.error("获取订单详情失败");
        }
    }

    /**
     * 生成订单号
     * 格式：VIP + 时间戳 + 随机数
     *
     * @return 订单号
     */
    private String generateOrderNo() {
        return "VIP" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
    }

    /**
     * 生成交易号
     * 模拟第三方支付平台的交易号
     *
     * @param payType 支付方式
     * @return 交易号
     */
    private String generateTransactionId(Integer payType) {
        String prefix;
        switch (payType) {
            case 1:
                prefix = "WX";
                break;
            case 2:
                prefix = "ALI";
                break;
            case 3:
                prefix = "APP";
                break;
            default:
                prefix = "OTH";
                break;
        }
        return prefix + System.currentTimeMillis() + String.format("%06d", new Random().nextInt(1000000));
    }

    /**
     * 获取支付方式名称
     *
     * @param payType 支付方式代码
     * @return 支付方式名称
     */
    private String getPayMethodName(Integer payType) {
        switch (payType) {
            case 1:
                return "微信支付";
            case 2:
                return "支付宝";
            case 3:
                return "Apple Pay";
            case 4:
                return "其他支付";
            default:
                return "未知支付方式";
        }
    }

    /**
     * 获取VIP类型名称
     *
     * @param vipType VIP类型代码
     * @return VIP类型名称
     */
    private String getVipTypeName(Integer vipType) {
        switch (vipType) {
            case 1:
                return "月度VIP";
            case 2:
                return "季度VIP";
            case 3:
                return "年度VIP";
            default:
                return "未知VIP类型";
        }
    }

    /**
     * 获取订单状态名称
     *
     * @param orderStatus 订单状态代码
     * @return 订单状态名称
     */
    private String getOrderStatusName(Integer orderStatus) {
        switch (orderStatus) {
            case 0:
                return "待支付";
            case 1:
                return "已支付";
            case 2:
                return "已取消";
            case 3:
                return "已退款";
            default:
                return "未知状态";
        }
    }

    /**
     * 获取用户角色名称
     *
     * @param userRole 用户角色代码
     * @return 用户角色名称
     */
    private String getUserRoleName(Integer userRole) {
        switch (userRole) {
            case 1:
                return "普通用户";
            case 2:
                return "VIP用户";
            case 3:
                return "红娘";
            case 4:
                return "VIP+红娘";
            default:
                return "未知角色";
        }
    }

    /**
     * 从token中获取用户ID
     *
     * @param token JWT token
     * @return 用户ID，如果获取失败返回null
     */
    private Long getUserIdFromToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                return null;
            }

            Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
            if (claimsMap != null && claimsMap.containsKey("userId")) {
                Object userIdObj = claimsMap.get("userId");
                if (userIdObj instanceof Integer) {
                    return ((Integer) userIdObj).longValue();
                } else if (userIdObj instanceof Long) {
                    return (Long) userIdObj;
                } else if (userIdObj instanceof String) {
                    return Long.valueOf((String) userIdObj);
                }
            }
        } catch (Exception e) {
            log.warn("从token获取用户ID失败: {}", e.getMessage());
        }
        return null;
    }
}
