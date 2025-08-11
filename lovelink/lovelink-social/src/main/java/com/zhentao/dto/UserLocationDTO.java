package com.zhentao.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

/**
 * 用户位置信息DTO
 * @author zhentao
 * @date 2025/8/3
 */
public class UserLocationDTO {

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
     * 位置描述（可选）
     */
    private String locationName;

    /**
     * 地址信息（可选）
     */
    private String address;

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

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
