package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_gift
 */
@TableName(value ="tb_gift")
@Data
public class TbGift {
    /**
     * 礼物ID
     */
    @TableId(type = IdType.AUTO)
    private Long giftId;

    /**
     * 礼物名称
     */
    private String giftName;

    /**
     * 礼物图片URL
     */
    private String giftImage;

    /**
     * 礼物动画URL
     */
    private String giftAnimation;

    /**
     * 礼物价格（虚拟币）
     */
    private Integer giftPrice;

    /**
     * 礼物类型：1-普通礼物，2-特效礼物，3-限时礼物
     */
    private Integer giftType;

    /**
     * 礼物状态：0-下架，1-上架
     */
    private Integer giftStatus;

    /**
     * 上架开始时间
     */
    private Date startTime;

    /**
     * 上架结束时间
     */
    private Date endTime;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        TbGift other = (TbGift) that;
        return (this.getGiftId() == null ? other.getGiftId() == null : this.getGiftId().equals(other.getGiftId()))
            && (this.getGiftName() == null ? other.getGiftName() == null : this.getGiftName().equals(other.getGiftName()))
            && (this.getGiftImage() == null ? other.getGiftImage() == null : this.getGiftImage().equals(other.getGiftImage()))
            && (this.getGiftAnimation() == null ? other.getGiftAnimation() == null : this.getGiftAnimation().equals(other.getGiftAnimation()))
            && (this.getGiftPrice() == null ? other.getGiftPrice() == null : this.getGiftPrice().equals(other.getGiftPrice()))
            && (this.getGiftType() == null ? other.getGiftType() == null : this.getGiftType().equals(other.getGiftType()))
            && (this.getGiftStatus() == null ? other.getGiftStatus() == null : this.getGiftStatus().equals(other.getGiftStatus()))
            && (this.getStartTime() == null ? other.getStartTime() == null : this.getStartTime().equals(other.getStartTime()))
            && (this.getEndTime() == null ? other.getEndTime() == null : this.getEndTime().equals(other.getEndTime()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getGiftId() == null) ? 0 : getGiftId().hashCode());
        result = prime * result + ((getGiftName() == null) ? 0 : getGiftName().hashCode());
        result = prime * result + ((getGiftImage() == null) ? 0 : getGiftImage().hashCode());
        result = prime * result + ((getGiftAnimation() == null) ? 0 : getGiftAnimation().hashCode());
        result = prime * result + ((getGiftPrice() == null) ? 0 : getGiftPrice().hashCode());
        result = prime * result + ((getGiftType() == null) ? 0 : getGiftType().hashCode());
        result = prime * result + ((getGiftStatus() == null) ? 0 : getGiftStatus().hashCode());
        result = prime * result + ((getStartTime() == null) ? 0 : getStartTime().hashCode());
        result = prime * result + ((getEndTime() == null) ? 0 : getEndTime().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", giftId=").append(giftId);
        sb.append(", giftName=").append(giftName);
        sb.append(", giftImage=").append(giftImage);
        sb.append(", giftAnimation=").append(giftAnimation);
        sb.append(", giftPrice=").append(giftPrice);
        sb.append(", giftType=").append(giftType);
        sb.append(", giftStatus=").append(giftStatus);
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}