package com.zhentao.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

/**
 * 附近用户查询DTO
 * @author zhentao
 * @date 2025/8/3
 */
public class NearbyUsersQueryDTO {
    
    /**
     * 经度
     */
    @NotNull(message = "经度不能为空")
    @DecimalMin(value = "-180.0", message = "经度必须在-180到180之间")
    @DecimalMax(value = "180.0", message = "经度必须在-180到180之间")
    private Double longitude;
    
    /**
     * 纬度
     */
    @NotNull(message = "纬度不能为空")
    @DecimalMin(value = "-90.0", message = "纬度必须在-90到90之间")
    @DecimalMax(value = "90.0", message = "纬度必须在-90到90之间")
    private Double latitude;
    
    /**
     * 搜索半径（单位：公里）
     */
    @Min(value = 1, message = "搜索半径最小为1公里")
    @Max(value = 100, message = "搜索半径最大为100公里")
    private Double radius = 10.0;
    
    /**
     * 返回用户数量限制
     */
    @Min(value = 1, message = "返回数量最小为1")
    @Max(value = 100, message = "返回数量最大为100")
    private Integer limit = 20;
    
    /**
     * 性别筛选：0-不限，1-男，2-女
     */
    private Integer gender;
    
    /**
     * 年龄范围-最小年龄
     */
    @Min(value = 18, message = "最小年龄不能小于18岁")
    @Max(value = 100, message = "最小年龄不能大于100岁")
    private Integer minAge;
    
    /**
     * 年龄范围-最大年龄
     */
    @Min(value = 18, message = "最大年龄不能小于18岁")
    @Max(value = 100, message = "最大年龄不能大于100岁")
    private Integer maxAge;

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }
}
