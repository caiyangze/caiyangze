package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_voice_room_user
 */
@TableName(value ="tb_voice_room_user")
@Data
public class TbVoiceRoomUser {
    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long recordId;

    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户角色：0-观众，1-主持人，2-嘉宾
     */
    private Integer userRole;

    /**
     * 麦克风状态：0-关闭，1-开启
     */
    private Integer micStatus;

    /**
     * 加入时间
     */
    private Date joinTime;

    /**
     * 离开时间
     */
    private Date leaveTime;

    /**
     * 是否在线：0-离线，1-在线
     */
    private Integer isOnline;

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
        TbVoiceRoomUser other = (TbVoiceRoomUser) that;
        return (this.getRecordId() == null ? other.getRecordId() == null : this.getRecordId().equals(other.getRecordId()))
            && (this.getRoomId() == null ? other.getRoomId() == null : this.getRoomId().equals(other.getRoomId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getUserRole() == null ? other.getUserRole() == null : this.getUserRole().equals(other.getUserRole()))
            && (this.getMicStatus() == null ? other.getMicStatus() == null : this.getMicStatus().equals(other.getMicStatus()))
            && (this.getJoinTime() == null ? other.getJoinTime() == null : this.getJoinTime().equals(other.getJoinTime()))
            && (this.getLeaveTime() == null ? other.getLeaveTime() == null : this.getLeaveTime().equals(other.getLeaveTime()))
            && (this.getIsOnline() == null ? other.getIsOnline() == null : this.getIsOnline().equals(other.getIsOnline()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getRecordId() == null) ? 0 : getRecordId().hashCode());
        result = prime * result + ((getRoomId() == null) ? 0 : getRoomId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getUserRole() == null) ? 0 : getUserRole().hashCode());
        result = prime * result + ((getMicStatus() == null) ? 0 : getMicStatus().hashCode());
        result = prime * result + ((getJoinTime() == null) ? 0 : getJoinTime().hashCode());
        result = prime * result + ((getLeaveTime() == null) ? 0 : getLeaveTime().hashCode());
        result = prime * result + ((getIsOnline() == null) ? 0 : getIsOnline().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", recordId=").append(recordId);
        sb.append(", roomId=").append(roomId);
        sb.append(", userId=").append(userId);
        sb.append(", userRole=").append(userRole);
        sb.append(", micStatus=").append(micStatus);
        sb.append(", joinTime=").append(joinTime);
        sb.append(", leaveTime=").append(leaveTime);
        sb.append(", isOnline=").append(isOnline);
        sb.append("]");
        return sb.toString();
    }
}