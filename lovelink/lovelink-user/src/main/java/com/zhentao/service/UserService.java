package com.zhentao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhentao.dto.UserRegisterDTO;
import com.zhentao.pojo.TbUser;
import com.zhentao.utils.Result;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务接口
 * 
 * @author zhentao
 */
public interface UserService extends IService<TbUser> {
    
    /**
     * 发送注册验证码
     * 
     * @param phone 手机号
     * @return 发送结果
     */
    Result sendVerificationCode(String phone);
    
    /**
     * 验证验证码是否正确
     * 
     * @param phone 手机号
     * @param code 验证码
     * @return 验证结果，true表示验证通过，false表示验证失败
     */
    boolean verifyCode(String phone, String code);
    
    /**
     * 验证验证码是否正确，并返回结果对象
     * 
     * @param phone 手机号
     * @param code 验证码
     * @return 验证结果
     */
    Result verifyCodeWithResult(String phone, String code);
    
    /**
     * 用户注册
     * 
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    Result register(UserRegisterDTO registerDTO);
    
    /**
     * 根据手机号查询用户
     * 
     * @param phone 手机号
     * @return 用户对象，不存在返回null
     */
    TbUser findByPhone(String phone);

    String uploadAvatar(MultipartFile file);

    TbUser getById(Integer userId);

    /**
     * 心动速配 - 根据性别随机匹配用户
     *
     * @param gender 要匹配的性别 1-男 2-女
     * @param currentUserId 当前用户ID（排除自己）
     * @return 匹配结果
     */
    Result randomMatchByGender(Integer gender, Long currentUserId);

    /**
     * 心动速配 - 根据性别随机匹配用户（支持跳过钱包扣减）
     *
     * @param gender 要匹配的性别 1-男 2-女
     * @param currentUserId 当前用户ID（排除自己）
     * @param skipWalletDeduction 是否跳过钱包扣减
     * @return 匹配结果
     */
    Result randomMatchByGender(Integer gender, Long currentUserId, Boolean skipWalletDeduction);
}