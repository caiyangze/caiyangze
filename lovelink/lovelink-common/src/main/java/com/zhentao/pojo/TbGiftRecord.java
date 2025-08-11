package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_gift_record
 */
@TableName(value ="tb_gift_record")
@Data
public class TbGiftRecord {
    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long recordId;

    /**
     * 赠送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 礼物ID
     */
    private Long giftId;

    /**
     * 礼物数量
     */
    private Integer giftCount;

    /**
     * 总价（虚拟币）
     */
    private Integer totalPrice;

    /**
     * 赠送场景：1-个人主页，2-动态评论，3-语音房，4-游戏中
     */
    private Integer sceneType;

    /**
     * 场景ID
     */
    private Long sceneId;

    /**
     * 创建时间
     */
    private Date createdAt;

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
        TbGiftRecord other = (TbGiftRecord) that;
        return (this.getRecordId() == null ? other.getRecordId() == null : this.getRecordId().equals(other.getRecordId()))
            && (this.getSenderId() == null ? other.getSenderId() == null : this.getSenderId().equals(other.getSenderId()))
            && (this.getReceiverId() == null ? other.getReceiverId() == null : this.getReceiverId().equals(other.getReceiverId()))
            && (this.getGiftId() == null ? other.getGiftId() == null : this.getGiftId().equals(other.getGiftId()))
            && (this.getGiftCount() == null ? other.getGiftCount() == null : this.getGiftCount().equals(other.getGiftCount()))
            && (this.getTotalPrice() == null ? other.getTotalPrice() == null : this.getTotalPrice().equals(other.getTotalPrice()))
            && (this.getSceneType() == null ? other.getSceneType() == null : this.getSceneType().equals(other.getSceneType()))
            && (this.getSceneId() == null ? other.getSceneId() == null : this.getSceneId().equals(other.getSceneId()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getRecordId() == null) ? 0 : getRecordId().hashCode());
        result = prime * result + ((getSenderId() == null) ? 0 : getSenderId().hashCode());
        result = prime * result + ((getReceiverId() == null) ? 0 : getReceiverId().hashCode());
        result = prime * result + ((getGiftId() == null) ? 0 : getGiftId().hashCode());
        result = prime * result + ((getGiftCount() == null) ? 0 : getGiftCount().hashCode());
        result = prime * result + ((getTotalPrice() == null) ? 0 : getTotalPrice().hashCode());
        result = prime * result + ((getSceneType() == null) ? 0 : getSceneType().hashCode());
        result = prime * result + ((getSceneId() == null) ? 0 : getSceneId().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", recordId=").append(recordId);
        sb.append(", senderId=").append(senderId);
        sb.append(", receiverId=").append(receiverId);
        sb.append(", giftId=").append(giftId);
        sb.append(", giftCount=").append(giftCount);
        sb.append(", totalPrice=").append(totalPrice);
        sb.append(", sceneType=").append(sceneType);
        sb.append(", sceneId=").append(sceneId);
        sb.append(", createdAt=").append(createdAt);
        sb.append("]");
        return sb.toString();
    }
}