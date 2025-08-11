package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_game_room
 */
@TableName(value ="tb_game_room")
@Data
public class TbGameRoom {
    /**
     * 房间ID
     */
    @TableId(type = IdType.AUTO)
    private Long roomId;

    /**
     * 游戏ID
     */
    private Long gameId;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 房间名称
     */
    private String roomName;

    /**
     * 房间密码
     */
    private String roomPassword;

    /**
     * 最大玩家数
     */
    private Integer maxPlayers;

    /**
     * 当前玩家数
     */
    private Integer currentPlayers;

    /**
     * 房间状态：0-等待中，1-游戏中，2-已结束
     */
    private Integer roomStatus;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
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
        TbGameRoom other = (TbGameRoom) that;
        return (this.getRoomId() == null ? other.getRoomId() == null : this.getRoomId().equals(other.getRoomId()))
            && (this.getGameId() == null ? other.getGameId() == null : this.getGameId().equals(other.getGameId()))
            && (this.getCreatorId() == null ? other.getCreatorId() == null : this.getCreatorId().equals(other.getCreatorId()))
            && (this.getRoomName() == null ? other.getRoomName() == null : this.getRoomName().equals(other.getRoomName()))
            && (this.getRoomPassword() == null ? other.getRoomPassword() == null : this.getRoomPassword().equals(other.getRoomPassword()))
            && (this.getMaxPlayers() == null ? other.getMaxPlayers() == null : this.getMaxPlayers().equals(other.getMaxPlayers()))
            && (this.getCurrentPlayers() == null ? other.getCurrentPlayers() == null : this.getCurrentPlayers().equals(other.getCurrentPlayers()))
            && (this.getRoomStatus() == null ? other.getRoomStatus() == null : this.getRoomStatus().equals(other.getRoomStatus()))
            && (this.getStartTime() == null ? other.getStartTime() == null : this.getStartTime().equals(other.getStartTime()))
            && (this.getEndTime() == null ? other.getEndTime() == null : this.getEndTime().equals(other.getEndTime()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getRoomId() == null) ? 0 : getRoomId().hashCode());
        result = prime * result + ((getGameId() == null) ? 0 : getGameId().hashCode());
        result = prime * result + ((getCreatorId() == null) ? 0 : getCreatorId().hashCode());
        result = prime * result + ((getRoomName() == null) ? 0 : getRoomName().hashCode());
        result = prime * result + ((getRoomPassword() == null) ? 0 : getRoomPassword().hashCode());
        result = prime * result + ((getMaxPlayers() == null) ? 0 : getMaxPlayers().hashCode());
        result = prime * result + ((getCurrentPlayers() == null) ? 0 : getCurrentPlayers().hashCode());
        result = prime * result + ((getRoomStatus() == null) ? 0 : getRoomStatus().hashCode());
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
        sb.append(", roomId=").append(roomId);
        sb.append(", gameId=").append(gameId);
        sb.append(", creatorId=").append(creatorId);
        sb.append(", roomName=").append(roomName);
        sb.append(", roomPassword=").append(roomPassword);
        sb.append(", maxPlayers=").append(maxPlayers);
        sb.append(", currentPlayers=").append(currentPlayers);
        sb.append(", roomStatus=").append(roomStatus);
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}