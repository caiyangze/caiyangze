package com.zhentao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhentao.dto.UserVerificationDTO;
import com.zhentao.pojo.TbUser;
import com.zhentao.pojo.TbUserVerification;
import com.zhentao.service.TbUserService;
import com.zhentao.service.TbUserVerificationService;
import com.zhentao.util.AliIdCardVerifyUtil;
import com.zhentao.utils.JwtService;
import com.zhentao.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户实名认证控制器
 * 
 * @author zhentao
 */
@RestController
@RequestMapping("/user/verification")
@CrossOrigin("*")
@Slf4j
public class UserVerificationController {
    
    @Autowired
    private TbUserService userService;
    
    @Autowired
    private TbUserVerificationService verificationService;
    
    @Autowired
    private AliIdCardVerifyUtil aliIdCardVerifyUtil;

    /**
     * 身份证预验证
     * 在用户填写完姓名和身份证号后，先调用阿里云API验证
     */
    @PostMapping("/preVerify")
    public Result preVerifyIdCard(@RequestBody Map<String, String> request, @RequestHeader("token") String token) {
        try {
            // 验证token
            if (JwtService.verifyToken(token) != 1) {
                return Result.NO_LOGIN();
            }
            
            String realName = request.get("realName");
            String idCardNo = request.get("idCardNo");
            
            if (realName == null || realName.trim().isEmpty()) {
                return Result.ERROR("真实姓名不能为空");
            }
            
            if (idCardNo == null || idCardNo.trim().isEmpty()) {
                return Result.ERROR("身份证号不能为空");
            }
            
            // 身份证号格式验证
            if (!idCardNo.matches("^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$")) {
                return Result.ERROR("身份证号格式不正确");
            }
            
            log.info("开始身份证预验证，姓名：{}，身份证号：{}", realName, maskIdCard(idCardNo));
            
            // 调用阿里云身份证验证API
            AliIdCardVerifyUtil.VerifyResult verifyResult = aliIdCardVerifyUtil.verifyIdCard(realName, idCardNo);
            
            if (verifyResult.isSuccess()) {
                log.info("身份证预验证成功，姓名：{}，身份证号：{}", realName, maskIdCard(idCardNo));
                return Result.OK("身份证验证成功，请继续上传证件照片");
            } else {
                log.warn("身份证预验证失败，姓名：{}，身份证号：{}，原因：{}", 
                        realName, maskIdCard(idCardNo), verifyResult.getMessage());
                return Result.ERROR(verifyResult.getMessage());
            }
            
        } catch (Exception e) {
            log.error("身份证预验证异常", e);
            return Result.ERROR("身份证验证服务异常，请稍后重试");
        }
    }
    
    /**
     * 提交实名认证申请
     * 在身份证预验证成功后，用户上传完所有资料后调用
     */
    @PostMapping("/submit")
    @Transactional(rollbackFor = Exception.class)
    public Result submitVerification(@RequestBody @Validated UserVerificationDTO dto, @RequestHeader("token") String token) {
        try {
            // 验证token
            if (JwtService.verifyToken(token) != 1) {
                return Result.NO_LOGIN();
            }
            
            Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
            Object userIdObj = claimsMap.get("userId");
            if (!(userIdObj instanceof Integer)) {
                return Result.NO_LOGIN();
            }
            
            Long userId = Long.valueOf((Integer) userIdObj);
            TbUser user = userService.getById(userId);
            if (user == null) {
                return Result.ERROR("用户不存在");
            }
            
            log.info("用户{}开始提交实名认证申请", userId);
            
            // 检查是否已经认证过
            if (user.getIsVerified() == 1) {
                return Result.ERROR("您已经通过实名认证，无需重复提交");
            }
            
            // 检查是否有待审核的申请
            QueryWrapper<TbUserVerification> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.eq("verification_status", 0); // 待审核状态
            TbUserVerification existingVerification = verificationService.getOne(queryWrapper);
            
            if (existingVerification != null) {
                return Result.ERROR("您已有待审核的实名认证申请，请耐心等待审核结果");
            }
            
            // 再次验证身份证信息（确保数据一致性）
            AliIdCardVerifyUtil.VerifyResult verifyResult = aliIdCardVerifyUtil.verifyIdCard(dto.getRealName(), dto.getIdCardNo());
            if (!verifyResult.isSuccess()) {
                log.warn("提交认证时身份证验证失败，用户ID：{}，原因：{}", userId, verifyResult.getMessage());
                return Result.ERROR("身份证信息验证失败：" + verifyResult.getMessage());
            }
            
            // 创建认证记录
            TbUserVerification verification = new TbUserVerification();
            verification.setUserId(userId);
            verification.setRealName(dto.getRealName());
            verification.setIdCardNo(dto.getIdCardNo());
            verification.setIdCardFront(dto.getIdCardFront());
            verification.setIdCardBack(dto.getIdCardBack());
            verification.setFacePhoto(dto.getFacePhoto());
            verification.setVerificationStatus(0); // 待审核
            verification.setCreatedAt(new Date());
            verification.setUpdatedAt(new Date());
            
            boolean saved = verificationService.save(verification);
            if (!saved) {
                log.error("保存实名认证记录失败，用户ID：{}", userId);
                return Result.ERROR("提交认证申请失败，请重试");
            }
            
            log.info("用户{}实名认证申请提交成功", userId);
            
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("verificationId", verification.getVerificationId());
            resultData.put("message", "实名认证申请已提交，请等待审核");
            
            return Result.OK(resultData);
            
        } catch (Exception e) {
            log.error("提交实名认证申请异常", e);
            return Result.ERROR("提交认证申请失败，请重试");
        }
    }
    
    /**
     * 查询认证状态
     */
    @GetMapping("/status")
    public Result getVerificationStatus(@RequestHeader("token") String token) {
        try {
            // 验证token
            if (JwtService.verifyToken(token) != 1) {
                return Result.NO_LOGIN();
            }
            
            Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
            Object userIdObj = claimsMap.get("userId");
            if (!(userIdObj instanceof Integer)) {
                return Result.NO_LOGIN();
            }
            
            Long userId = Long.valueOf((Integer) userIdObj);
            
            // 查询最新的认证记录
            QueryWrapper<TbUserVerification> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.orderByDesc("created_at");
            queryWrapper.last("LIMIT 1");
            
            TbUserVerification verification = verificationService.getOne(queryWrapper);
            
            if (verification == null) {
                return Result.OK(null); // 没有认证记录
            }
            
            Map<String, Object> statusData = new HashMap<>();
            statusData.put("status", verification.getVerificationStatus());
            statusData.put("rejectReason", verification.getRejectReason());
            statusData.put("createdAt", verification.getCreatedAt());
            statusData.put("updatedAt", verification.getUpdatedAt());
            
            return Result.OK(statusData);
            
        } catch (Exception e) {
            log.error("查询认证状态异常", e);
            return Result.ERROR("查询认证状态失败");
        }
    }
    
    /**
     * 脱敏身份证号
     */
    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 4) + "****" + idCard.substring(idCard.length() - 4);
    }
}
