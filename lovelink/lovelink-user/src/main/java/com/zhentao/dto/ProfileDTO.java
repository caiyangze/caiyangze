package com.zhentao.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * 用户资料DTO
 *
 * @author zhentao
 */
@Data
public class ProfileDTO {
    /**
     * 资料ID
     */
    private Long profileId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 真实姓名
     */
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

//    年龄
    @Min(value = 18, message = "年龄不能低于18岁")
    @Max(value = 100, message = "年龄不能超过100岁")
    private Integer age;
    
    /**
     * 身高(cm)
     */
    @Min(value = 100, message = "身高不能低于100cm")
    @Max(value = 250, message = "身高不能超过250cm")
    private Integer height;
    
    /**
     * 体重(kg)
     */
    @Min(value = 30, message = "体重不能低于30kg")
    @Max(value = 200, message = "体重不能超过200kg")
    private Integer weight;
    
    /**
     * 学历: 1-高中及以下, 2-大专, 3-本科, 4-硕士, 5-博士
     */
    @Min(value = 1, message = "学历值不正确")
    @Max(value = 5, message = "学历值不正确")
    private Integer education;
    
    /**
     * 学历证明图片URL
     */
    private String educationCert;
    
    /**
     * 公司
     */
    @Size(max = 100, message = "公司名称长度不能超过100个字符")
    private String company;
    
    /**
     * 职位
     */
    @Size(max = 50, message = "职位名称长度不能超过50个字符")
    private String position;
    
    /**
     * 收入水平: 1-5k以下, 2-5k-10k, 3-10k-20k, 4-20k以上
     */
    @Min(value = 1, message = "收入水平值不正确")
    @Max(value = 4, message = "收入水平值不正确")
    private Integer incomeLevel;
    
    /**
     * 工作城市
     */
    @Size(max = 50, message = "工作城市长度不能超过50个字符")
    private String workCity;
    
    /**
     * 家乡
     */
    @Size(max = 50, message = "家乡长度不能超过50个字符")
    private String hometown;
    
    /**
     * 婚姻状况: 1-未婚, 2-离异, 3-丧偶
     */
    @Min(value = 1, message = "婚姻状况值不正确")
    @Max(value = 3, message = "婚姻状况值不正确")
    private Integer maritalStatus;
    
    /**
     * 是否有子女: 0-无, 1-有
     */
    @Min(value = 0, message = "是否有子女值不正确")
    @Max(value = 1, message = "是否有子女值不正确")
    private Integer hasChildren;
    
    /**
     * 住房情况: 1-租房, 2-有房贷款, 3-有房无贷款, 4-和父母同住
     */
    @Min(value = 1, message = "住房情况值不正确")
    @Max(value = 4, message = "住房情况值不正确")
    private Integer houseStatus;
    
    /**
     * 车辆情况: 1-无车, 2-有车有贷款, 3-有车无贷款
     */
    @Min(value = 1, message = "车辆情况值不正确")
    @Max(value = 3, message = "车辆情况值不正确")
    private Integer carStatus;
    
    /**
     * 自我介绍
     */
    private String selfIntroduction;
    
    /**
     * 兴趣爱好
     */
    @Size(max = 500, message = "兴趣爱好长度不能超过500个字符")
    private String hobby;
}