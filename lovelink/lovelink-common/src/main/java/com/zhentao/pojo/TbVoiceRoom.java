package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_voice_room
 */
@TableName(value ="tb_voice_room")
@Data
public class TbVoiceRoom {
    /**
     * 房间ID
     */
    @TableId(type = IdType.AUTO)
    private Long roomId;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 房间名称
     */
    private String roomName;

    /**
     * 房间封面URL
     */
    private String roomCover;

    /**
     * 房间描述
     */
    private String roomDesc;

    /**
     * 房间类型：1-闲聊，2-音乐，3-情感话题，4-其他
     */
    private Integer roomType;

    /**
     * 最大用户数
     */
    private Integer maxUsers;

    /**
     * 当前用户数
     */
    private Integer currentUsers;

    /**
     * 房间状态：0-关闭，1-开放
     */
    private Integer roomStatus;

    /**
     * 是否私密：0-公开，1-私密
     */
    private Integer isPrivate;

    /**
     * 房间密码
     */
    private String roomPassword;

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
        TbVoiceRoom other = (TbVoiceRoom) that;
        return (this.getRoomId() == null ? other.getRoomId() == null : this.getRoomId().equals(other.getRoomId()))
            && (this.getCreatorId() == null ? other.getCreatorId() == null : this.getCreatorId().equals(other.getCreatorId()))
            && (this.getRoomName() == null ? other.getRoomName() == null : this.getRoomName().equals(other.getRoomName()))
            && (this.getRoomCover() == null ? other.getRoomCover() == null : this.getRoomCover().equals(other.getRoomCover()))
            && (this.getRoomDesc() == null ? other.getRoomDesc() == null : this.getRoomDesc().equals(other.getRoomDesc()))
            && (this.getRoomType() == null ? other.getRoomType() == null : this.getRoomType().equals(other.getRoomType()))
            && (this.getMaxUsers() == null ? other.getMaxUsers() == null : this.getMaxUsers().equals(other.getMaxUsers()))
            && (this.getCurrentUsers() == null ? other.getCurrentUsers() == null : this.getCurrentUsers().equals(other.getCurrentUsers()))
            && (this.getRoomStatus() == null ? other.getRoomStatus() == null : this.getRoomStatus().equals(other.getRoomStatus()))
            && (this.getIsPrivate() == null ? other.getIsPrivate() == null : this.getIsPrivate().equals(other.getIsPrivate()))
            && (this.getRoomPassword() == null ? other.getRoomPassword() == null : this.getRoomPassword().equals(other.getRoomPassword()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getRoomId() == null) ? 0 : getRoomId().hashCode());
        result = prime * result + ((getCreatorId() == null) ? 0 : getCreatorId().hashCode());
        result = prime * result + ((getRoomName() == null) ? 0 : getRoomName().hashCode());
        result = prime * result + ((getRoomCover() == null) ? 0 : getRoomCover().hashCode());
        result = prime * result + ((getRoomDesc() == null) ? 0 : getRoomDesc().hashCode());
        result = prime * result + ((getRoomType() == null) ? 0 : getRoomType().hashCode());
        result = prime * result + ((getMaxUsers() == null) ? 0 : getMaxUsers().hashCode());
        result = prime * result + ((getCurrentUsers() == null) ? 0 : getCurrentUsers().hashCode());
        result = prime * result + ((getRoomStatus() == null) ? 0 : getRoomStatus().hashCode());
        result = prime * result + ((getIsPrivate() == null) ? 0 : getIsPrivate().hashCode());
        result = prime * result + ((getRoomPassword() == null) ? 0 : getRoomPassword().hashCode());
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
        sb.append(", roomId=").append(roomId);
        sb.append(", creatorId=").append(creatorId);
        sb.append(", roomName=").append(roomName);
        sb.append(", roomCover=").append(roomCover);
        sb.append(", roomDesc=").append(roomDesc);
        sb.append(", roomType=").append(roomType);
        sb.append(", maxUsers=").append(maxUsers);
        sb.append(", currentUsers=").append(currentUsers);
        sb.append(", roomStatus=").append(roomStatus);
        sb.append(", isPrivate=").append(isPrivate);
        sb.append(", roomPassword=").append(roomPassword);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}