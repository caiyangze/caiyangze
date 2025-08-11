package com.zhentao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhentao.config.HttpUtils;
import com.zhentao.dto.UserRegisterDTO;
import com.zhentao.mapper.UserMapper;
import com.zhentao.mapper.ProfileMapper;
import com.zhentao.pojo.TbUser;
import com.zhentao.pojo.TbUserProfile;
import com.zhentao.pojo.TbUserWallet;
import com.zhentao.service.TbUserWalletService;
import com.zhentao.service.UserService;
import com.zhentao.service.WalletService;
import com.zhentao.dto.WalletDTO;
import com.zhentao.utils.MinioUtil;
import com.zhentao.utils.PasswordEncoder;
import com.zhentao.utils.Result;
import com.zhentao.utils.VerificationCodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 *
 * @author zhentao
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper,TbUser> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProfileMapper profileMapper;

    @Autowired
    private MinioUtil minioUtil;


    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private TbUserWalletService userWalletService;

    @Autowired
    private WalletService walletService;

    /**
     * 验证码在Redis中的前缀
     */
    private static final String VERIFICATION_CODE_PREFIX = "user:verification:code:";

    /**
     * 验证码有效期（分钟）
     */
    private static final long CODE_EXPIRE_MINUTES = 5;

    /**
     * 发送注册验证码
     *
     * @param phone 手机号
     * @return 发送结果
     */
    @Override
    @Transactional
    public Result sendVerificationCode(String phone) {
        // 2. 检查是否频繁发送
        String codeKey = VERIFICATION_CODE_PREFIX + phone;
        String existingCode = redisTemplate.opsForValue().get(codeKey);
        if (existingCode != null) {
            // 获取剩余过期时间
            Long expireTime = redisTemplate.getExpire(codeKey, TimeUnit.SECONDS);
            if (expireTime != null && expireTime > 240) { // 如果剩余时间超过4分钟，说明是1分钟内重复发送
                return Result.ERROR("请勿频繁发送验证码，请稍后再试");
            }
        }

        // 3. 生成6位验证码
        String code = VerificationCodeUtil.generateCode();

        // 4. 调用短信服务发送验证码
        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String method = "POST";
        String appcode = "2ec46bc546834a5492cedf61d68d577f";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);
        querys.put("param", "**code**:" + code + ",**minute**:5");

//smsSignId（短信前缀）和templateId（短信模板），可登录国阳云控制台自助申请。参考文档：http://help.guoyangyun.com/Problem/Qm.html

        querys.put("smsSignId", "2e65b1bb3d054466b82f0c9d125465e2");
        querys.put("templateId", "908e94ccf08b4476ba6c876d13f084ad");
        Map<String, String> bodys = new HashMap<String, String>();

        Integer statusCode = 0;
        try {
            /**
             * 重要提示如下:
             * HttpUtils请从\r\n\t    \t* https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java\r\n\t    \t* 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            statusCode = response.getStatusLine().getStatusCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (statusCode != 200) {
            return Result.ERROR("验证码发送失败，请稍后再试");
        }

        // 5. 将验证码保存到Redis，设置过期时间
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.info("验证码发送成功，手机号：{}，验证码：{}", phone, code);
        return Result.OK("验证码发送成功");
    }

    /**
     * 验证验证码是否正确
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 验证结果，true表示验证通过，false表示验证失败
     */
    @Override
    public boolean verifyCode(String phone, String code) {
        if (phone == null || code == null) {
            return false;
        }

        String codeKey = VERIFICATION_CODE_PREFIX + phone;
        String savedCode = redisTemplate.opsForValue().get(codeKey);

        // 验证码不存在或已过期
        if (savedCode == null) {
            return false;
        }

        // 验证码不匹配
        if (!savedCode.equals(code)) {
            return false;
        }

        return true;
    }

    /**
     * 验证验证码是否正确，并返回结果对象
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 验证结果
     */
    @Override
    public Result verifyCodeWithResult(String phone, String code) {
        if (phone == null || phone.trim().isEmpty()) {
            return Result.ERROR("手机号不能为空");
        }

        if (code == null || code.trim().isEmpty()) {
            return Result.ERROR("验证码不能为空");
        }

        String codeKey = VERIFICATION_CODE_PREFIX + phone;
        String savedCode = redisTemplate.opsForValue().get(codeKey);

        // 验证码不存在或已过期
        if (savedCode == null) {
            return Result.ERROR("验证码已过期，请重新获取");
        }

        // 验证码不匹配
        if (!savedCode.equals(code)) {
            return Result.ERROR("验证码错误");
        }

        // 验证成功后，可以选择是否删除验证码（根据业务需求）
        // 如果是注册流程的一部分，可能需要保留验证码，以便后续注册步骤使用
        // 如果是单纯的验证操作，可以删除验证码，防止重复使用
        redisTemplate.delete(codeKey);
        return Result.OK("验证码验证成功");
    }

    /**
     * 用户注册
     *
     * @return 注册结果
     */

//    头像上传逻辑
    @Override
    public String uploadAvatar(MultipartFile file) {
        try {
            // 上传头像到MinIO
            String avatarUrl = minioUtil.uploadAvatar(file);
            log.info("用户头像上传成功：{}", avatarUrl);
            return avatarUrl;
        } catch (Exception e) {
            log.error("头像上传失败：", e);
            return "头像上传失败：" + e.getMessage();
        }
    }
    //获取用户信息
    @Override
    public TbUser getById(Integer userId) {
        TbUser tbUser = userMapper.selectById(userId);
        return tbUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result register(UserRegisterDTO registerDTO) {
        String avatarFile = registerDTO.getAvatarUrl();
        if (registerDTO.getId() == null) {
            // 1. 校验手机号是否已注册
            TbUser existUser = findByPhone(registerDTO.getPhone());
            if (existUser != null) {
                return Result.ERROR("该手机号已被注册");
            }

            TbUser user = new TbUser();
            user.setPhone(registerDTO.getPhone());
            // 对密码进行加密
            user.setPassword(PasswordEncoder.encode(registerDTO.getPassword()));

            // 设置昵称，如果用户没提供，则使用默认昵称
            String nickname = registerDTO.getNickname();
            if (nickname == null || nickname.trim().isEmpty()) {
                nickname = "用户" + registerDTO.getPhone().substring(7);
            }
            user.setNickname(nickname);
            // 设置默认头像
            user.setAvatarUrl("https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
            // 设置默认值
            user.setGender(0); // 默认未知
            user.setUserRole(1); // 默认普通用户
            user.setRegisterSource(1); // 手机号注册
            user.setRegisterTime(new Date());
            user.setAccountStatus(1); // 正常状态
            user.setIsVerified(0); // 未实名认证
            user.setIsVip(0); // 非VIP
            user.setCountLike(0); //初始化点赞数
            user.setCountFollow(0); //初始化关注数
            user.setFan(0); //初始化粉丝数
            user.setCreatedAt(new Date());
            user.setUpdatedAt(new Date());
            // 4. 保存用户信息
            userMapper.insert(user);
            log.info("用户注册成功，手机号：{}", registerDTO.getPhone());

            // 5. 初始化用户钱包
            TbUserWallet wallet = new TbUserWallet();
            wallet.setUserId(user.getUserId());
            wallet.setCoinBalance(0); // 虚拟币余额初始化为0
            wallet.setTotalRecharge(0); // 累计充值虚拟币初始化为0
            wallet.setTotalConsume(0); // 累计消费虚拟币初始化为0
            wallet.setCashBalance(BigDecimal.ZERO); // 现金余额初始化为0
            wallet.setTotalIncome(BigDecimal.ZERO); // 累计收入初始化为0
            wallet.setTotalWithdraw(BigDecimal.ZERO); // 累计提现初始化为0
            wallet.setWalletStatus(1); // 钱包状态：1-正常
            wallet.setCreatedAt(new Date());
            wallet.setUpdatedAt(new Date());

            // 保存钱包信息
            userWalletService.save(wallet);
            log.info("用户钱包初始化成功，用户ID：{}", user.getUserId());

            // 6. 返回结果，不返回密码
            user.setPassword(null);
            return Result.OK(user);
        } else {
            TbUser tbUser = userMapper.selectById(registerDTO.getId());
            if (tbUser == null) {
                return Result.ERROR("用户不存在");
            }
            tbUser.setNickname(registerDTO.getNickname());
            tbUser.setGender(registerDTO.getGender());
            if (avatarFile != null && !avatarFile.isEmpty()) {
                tbUser.setAvatarUrl(registerDTO.getAvatarUrl());
            } else {
                // 设置默认头像
                tbUser.setAvatarUrl("https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
            }
            userMapper.updateById(tbUser);
            return Result.OK("用户信息修改成功");
        }

//        临时不需要这个验证，控制层已经调用验证了
//        // 2. 验证码校验
//        if (!verifyCode(registerDTO.getPhone(), registerDTO.getVerifyCode())) {
//            return Result.ERROR("验证码错误或已过期，请重新获取");
//        }

        // 验证通过后，删除Redis中的验证码
//        String codeKey = VERIFICATION_CODE_PREFIX + registerDTO.getPhone();
//        redisTemplate.delete(codeKey);

        // 3. 创建用户对象并设置属性

    }

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户对象，不存在返回null
     */
    @Override
    public TbUser findByPhone(String phone) {
        LambdaQueryWrapper<TbUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TbUser::getPhone, phone);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 心动速配 - 根据性别随机匹配用户
     *
     * @param gender 要匹配的性别 0-女 1-男
     * @param currentUserId 当前用户ID（排除自己）
     * @return 匹配结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result randomMatchByGender(Integer gender, Long currentUserId) {
        return randomMatchByGender(gender, currentUserId, false);
    }

    /**
     * 心动速配 - 根据性别随机匹配用户（支持跳过钱包扣减）
     *
     * @param gender 要匹配的性别 0-女 1-男
     * @param currentUserId 当前用户ID（排除自己）
     * @param skipWalletDeduction 是否跳过钱包扣减
     * @return 匹配结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result randomMatchByGender(Integer gender, Long currentUserId, Boolean skipWalletDeduction) {
        try {
            // 1. 如果不跳过钱包扣减，先检查并扣减虚拟币（心动速配消耗5个虚拟币）
            if (skipWalletDeduction == null || !skipWalletDeduction) {
                WalletDTO walletDTO = new WalletDTO();
                walletDTO.setUserId(currentUserId);
                walletDTO.setCoinAmount(5);
                walletDTO.setTransactionDesc("心动速配");
                walletDTO.setRelatedId("HEART_MATCH_" + System.currentTimeMillis());

                Result consumeResult = walletService.consume(walletDTO);
                if (consumeResult.getCode() != 200) {
                    return consumeResult; // 返回钱包扣减失败的结果（如余额不足等）
                }

                log.info("用户{}心动速配扣减5个虚拟币成功", currentUserId);
            } else {
                log.info("用户{}心动速配跳过钱包扣减（前端已处理）", currentUserId);
            }

            // 2. 执行心动速配匹配逻辑
            // 构建查询条件
            LambdaQueryWrapper<TbUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TbUser::getGender, gender)
                       .eq(TbUser::getAccountStatus, 1) // 只查询正常状态的用户
                       .ne(TbUser::getUserId, currentUserId); // 排除当前用户

            // 查询符合条件的用户总数
            Long count = userMapper.selectCount(queryWrapper);
            if (count == 0) {
                return Result.ERROR("暂无符合条件的用户");
            }

            // 随机选择一个用户
            // 生成随机偏移量
            int offset = (int) (Math.random() * count);

            // 使用分页查询获取随机用户
            IPage<TbUser> page = new Page<>(offset + 1, 1);
            IPage<TbUser> result = userMapper.selectPage(page, queryWrapper);

            if (result.getRecords().isEmpty()) {
                return Result.ERROR("匹配失败，请重试");
            }

            TbUser matchedUser = result.getRecords().get(0);

            // 清除敏感信息
            matchedUser.setPassword(null);

            // 查询用户资料获取年龄
            LambdaQueryWrapper<TbUserProfile> profileWrapper = new LambdaQueryWrapper<>();
            profileWrapper.eq(TbUserProfile::getUserId, matchedUser.getUserId());
            TbUserProfile userProfile = profileMapper.selectOne(profileWrapper);

            // 获取年龄，优先使用资料表中的年龄，如果没有则计算
            Integer age = null;
            if (userProfile != null && userProfile.getAge() != null) {
                age = userProfile.getAge();
            } else {
                age = calculateAge(matchedUser.getBirthDate());
            }

            // 构建返回结果
            Map<String, Object> matchResult = new HashMap<>();
            matchResult.put("userId", matchedUser.getUserId());
            matchResult.put("nickname", matchedUser.getNickname());
            matchResult.put("avatarUrl", matchedUser.getAvatarUrl());
            matchResult.put("gender", matchedUser.getGender());
            matchResult.put("age", age);

            log.info("心动速配成功，匹配用户：{}，年龄：{}", matchedUser.getNickname(), age);
            return Result.OK(matchResult);

        } catch (Exception e) {
            log.error("心动速配失败：", e);
            return Result.ERROR("匹配失败，请重试");
        }
    }

    /**
     * 计算年龄
     */
    private Integer calculateAge(Date birthday) {
        if (birthday == null) {
            return null;
        }

        Calendar birth = Calendar.getInstance();
        birth.setTime(birthday);

        Calendar now = Calendar.getInstance();

        int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

        // 如果今年的生日还没过，年龄减1
        if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }


}