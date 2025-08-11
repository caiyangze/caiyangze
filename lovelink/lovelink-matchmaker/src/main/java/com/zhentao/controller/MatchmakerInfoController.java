package com.zhentao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.zhentao.dto.MatchmakerApplicationDto;
import com.zhentao.dto.WalletDTO;
import com.zhentao.pojo.TbMatchmaker;
import com.zhentao.pojo.TbMatchmakerApplication;
import com.zhentao.pojo.TbUser;
import com.zhentao.pojo.TbUserVerification;
import com.zhentao.service.TbMatchmakerApplicationService;
import com.zhentao.service.TbMatchmakerService;
import com.zhentao.service.TbUserService;
import com.zhentao.service.TbUserVerificationService;
import com.zhentao.feign.WalletServiceClient;
import com.zhentao.utils.JwtService;
import com.zhentao.utils.Result;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/matchmaker/info")
public class MatchmakerInfoController {
    @Autowired
    private TbMatchmakerService tbMatchmakerService;
    @Autowired
    private TbMatchmakerApplicationService tbMatchmakerApplicationService;
    @Autowired
    private TbUserService tbUserService;
    @Autowired
    private TbUserVerificationService tbUserVerificationService;
    @Autowired
    private WalletServiceClient walletService;
    /**
     * 获取匹配maker列表
     * @return
     */
    @PostMapping("/list")
    public Result getMatchmakerList() {

        QueryWrapper<TbMatchmaker> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("matchmaker_status",1);
        queryWrapper.orderByDesc("matchmaker_level");
        queryWrapper.orderByDesc("service_years");
        queryWrapper.orderByDesc("success_count");
        queryWrapper.orderByDesc("created_at");
        queryWrapper.last("limit 10");
        queryWrapper.select("matchmaker_id","real_name","service_area","service_years","success_count","introduction","certification");
        List<TbMatchmaker> matchmakerList = tbMatchmakerService.list(queryWrapper);
        return Result.OK(matchmakerList);
    }
    /**
     * 获取匹配红娘详情
     * @return
     */
    @PostMapping("/detail")
    public Result getMatchmakerDetail(@RequestParam("matchmakerId") Long matchmakerId) {
        TbMatchmaker matchmaker = tbMatchmakerService.getById(matchmakerId);
        if (matchmaker == null) {
            return Result.ERROR("匹配maker不存在");
        }else {
            return Result.OK(matchmaker);
        }
    }
    /**
     * 申请成为红娘（需要消耗699个虚拟币）
     */
    @PostMapping("/apply")
    @Transactional(rollbackFor = Exception.class)
    public Result applyMatchmaker(@RequestBody MatchmakerApplicationDto matchmakerApplicationDto, @RequestHeader("token") String token) {
        int i = JwtService.verifyToken(token);
        if (i != 1){
            return Result.NO_LOGIN();
        }else {
            Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
            Object o = claimsMap.get("userId");
            if (o instanceof Integer){
                Long userId = Long.valueOf((Integer) o);
                TbUser user = tbUserService.getById(userId);
                //先让用户绑定手机号
                if (user.getPhone()==null){
                    return Result.ERROR("请先绑定手机号");
                }
                //判断性别如果用户没有选择性别也是不行申请红娘
                if (user.getGender()==0){
                    return Result.ERROR("请选择您的性别");
                }
                //判断用户是否已经是红娘
                if (user.getUserRole()==2){
                    return Result.ERROR("您已经是红娘，无需重复申请");
                }
                if (user.getIsVerified()==0){
                    return Result.OK("请先完成实名认证");
                }else {
                    String realName = matchmakerApplicationDto.getRealName();
                    QueryWrapper<TbMatchmakerApplication> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("user_id",userId);
                    queryWrapper.eq("real_name",realName);
                    TbMatchmakerApplication matchmakerApplication1 = tbMatchmakerApplicationService.getOne(queryWrapper);
                    if (matchmakerApplication1!=null){
                        return Result.ERROR("您已经申请了");
                    }else {
                        // 1. 先扣减虚拟币（申请红娘需要699个虚拟币，支持优惠券）
                        WalletDTO walletDTO = new WalletDTO();
                        walletDTO.setUserId(userId);
                        walletDTO.setCoinAmount(699);
                        walletDTO.setTransactionDesc("申请成为红娘");
                        walletDTO.setRelatedId("MATCHMAKER_APPLY_" + System.currentTimeMillis());

                        // 如果传递了优惠券信息，添加到钱包DTO中
                        if (matchmakerApplicationDto.getVoucherOrderId() != null) {
                            walletDTO.setVoucherOrderId(String.valueOf(matchmakerApplicationDto.getVoucherOrderId()));
                            walletDTO.setVoucherDiscountAmount(matchmakerApplicationDto.getVoucherDiscountAmount());
                            System.out.println("申请红娘使用优惠券，订单ID：" + matchmakerApplicationDto.getVoucherOrderId() +
                                "，抵扣金额：" + matchmakerApplicationDto.getVoucherDiscountAmount());
                        }

                        Result consumeResult = walletService.consume(walletDTO);
                        if (consumeResult.getCode() != 200) {
                            return consumeResult; // 返回钱包扣减失败的结果（如余额不足等）
                        }

                        // 2. 创建红娘申请记录
                        TbMatchmakerApplication matchmakerApplication =new TbMatchmakerApplication();
//            BeanUtils.copyProperties(matchmakerApplicationDto, matchmakerApplication);
                        matchmakerApplication.setUserId(userId);
                        matchmakerApplication.setRealName(matchmakerApplicationDto.getRealName());
                        matchmakerApplication.setPhone(matchmakerApplicationDto.getPhone());
                        matchmakerApplication.setIdCardNo(matchmakerApplicationDto.getIdCardNo());
                        matchmakerApplication.setIdCardFront(matchmakerApplicationDto.getIdCardFront());
                        matchmakerApplication.setIdCardBack(matchmakerApplicationDto.getIdCardBack());
                        matchmakerApplication.setServiceArea(matchmakerApplicationDto.getServiceArea());
                        matchmakerApplication.setIntroduction(matchmakerApplicationDto.getIntroduction());
                        matchmakerApplication.setExperience(matchmakerApplicationDto.getExperience());
                        matchmakerApplication.setApplicationStatus(0);
                        matchmakerApplication.setCreatedAt(new Date());
                        boolean save = tbMatchmakerApplicationService.save(matchmakerApplication);
                        if (!save) {
                            return Result.ERROR("添加申请失败");
                        }else {
                            // 3. 返回申请成功信息
                            Map<String, Object> resultData = new HashMap<>();
                            resultData.put("application", matchmakerApplication);
                            resultData.put("consumeInfo", consumeResult.getData());
                            resultData.put("message", "申请提交成功，已扣除699个虚拟币，请等待审核");

                            return Result.OK(resultData);
                        }
                    }
                }
            }else {
                return Result.NO_LOGIN();
            }

        }
    }

    /**
     * 获取用户实名认证信息（用于红娘申请表单自动填充）
     */
    @GetMapping("/getUserVerification")
    public Result getUserVerification(@RequestHeader("token") String token) {
        int i = JwtService.verifyToken(token);
        if (i != 1) {
            return Result.NO_LOGIN();
        }

        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Object o = claimsMap.get("userId");
        if (!(o instanceof Integer)) {
            return Result.NO_LOGIN();
        }

        Long userId = Long.valueOf((Integer) o);
        TbUser user = tbUserService.getById(userId);
        if (user == null) {
            return Result.ERROR("用户不存在");
        }

        // 检查用户是否已实名认证
        if (user.getIsVerified() == 0) {
            return Result.OK(null); // 未实名认证，返回空数据
        }

        // 查询用户的实名认证信息
        QueryWrapper<TbUserVerification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("verification_status", 1); // 只查询已通过的认证记录
        queryWrapper.orderByDesc("created_at"); // 按创建时间倒序，获取最新的认证记录
        queryWrapper.last("LIMIT 1");

        TbUserVerification verification = tbUserVerificationService.getOne(queryWrapper);
        if (verification == null) {
            return Result.OK(null); // 没有找到认证记录
        }

        // 构建返回数据
        Map<String, Object> verificationData = new HashMap<>();
        verificationData.put("realName", verification.getRealName());
        verificationData.put("idCardNo", verification.getIdCardNo());
        verificationData.put("idCardFront", verification.getIdCardFront());
        verificationData.put("idCardBack", verification.getIdCardBack());

        return Result.OK(verificationData);
    }

    /**
     * 检查用户是否已申请红娘
     */
    @GetMapping("/checkApplicationStatus")
    public Result checkApplicationStatus(@RequestHeader("token") String token) {
        int i = JwtService.verifyToken(token);
        if (i != 1) {
            return Result.NO_LOGIN();
        }

        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Object o = claimsMap.get("userId");
        if (!(o instanceof Integer)) {
            return Result.NO_LOGIN();
        }

        Long userId = Long.valueOf((Integer) o);
        TbUser user = tbUserService.getById(userId);
        if (user == null) {
            return Result.ERROR("用户不存在");
        }

        // 检查用户是否已经是红娘
        if (user.getUserRole() == 2) {
            Map<String, Object> result = new HashMap<>();
            result.put("hasApplied", true);
            result.put("isMatchmaker", true);
            result.put("status", "已是红娘");
            result.put("userRole", user.getUserRole());
            return Result.OK(result);
        }

        // 查询用户是否有申请记录
        QueryWrapper<TbMatchmakerApplication> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("created_at");
        queryWrapper.last("LIMIT 1");

        TbMatchmakerApplication application = tbMatchmakerApplicationService.getOne(queryWrapper);

        Map<String, Object> result = new HashMap<>();
        if (application != null) {
            result.put("hasApplied", true);
            result.put("isMatchmaker", false);
            result.put("applicationId", application.getApplicationId());
            result.put("applicationStatus", application.getApplicationStatus());
            result.put("createdAt", application.getCreatedAt());

            // 根据申请状态返回不同的状态描述
            switch (application.getApplicationStatus()) {
                case 0:
                    result.put("status", "申请审核中");
                    break;
                case 1:
                    result.put("status", "申请已通过");
                    break;
                case 2:
                    result.put("status", "申请已拒绝");
                    result.put("rejectReason", application.getRejectReason());
                    break;
                default:
                    result.put("status", "未知状态");
                    break;
            }
        } else {
            result.put("hasApplied", false);
            result.put("isMatchmaker", false);
            result.put("status", "未申请");
        }

        return Result.OK(result);
    }
}
