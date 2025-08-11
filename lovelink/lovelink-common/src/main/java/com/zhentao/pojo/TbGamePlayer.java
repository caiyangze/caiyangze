package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_game_player
 */
@TableName(value ="tb_game_player")
@Data
public class TbGamePlayer {
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
     * 加入时间
     */
    private Date joinTime;

    /**
     * 离开时间
     */
    private Date leaveTime;

    /**
     * 玩家状态：0-离开，1-在线
     */
    private Integer playerStatus;

    /**
     * 游戏得分
     */
    private Integer score;

    /**
     * 游戏排名
     */
    private Integer rank;

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
        TbGamePlayer other = (TbGamePlayer) that;
        return (this.getRecordId() == null ? other.getRecordId() == null : this.getRecordId().equals(other.getRecordId()))
            && (this.getRoomId() == null ? other.getRoomId() == null : this.getRoomId().equals(other.getRoomId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getJoinTime() == null ? other.getJoinTime() == null : this.getJoinTime().equals(other.getJoinTime()))
            && (this.getLeaveTime() == null ? other.getLeaveTime() == null : this.getLeaveTime().equals(other.getLeaveTime()))
            && (this.getPlayerStatus() == null ? other.getPlayerStatus() == null : this.getPlayerStatus().equals(other.getPlayerStatus()))
            && (this.getScore() == null ? other.getScore() == null : this.getScore().equals(other.getScore()))
            && (this.getRank() == null ? other.getRank() == null : this.getRank().equals(other.getRank()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getRecordId() == null) ? 0 : getRecordId().hashCode());
        result = prime * result + ((getRoomId() == null) ? 0 : getRoomId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getJoinTime() == null) ? 0 : getJoinTime().hashCode());
        result = prime * result + ((getLeaveTime() == null) ? 0 : getLeaveTime().hashCode());
        result = prime * result + ((getPlayerStatus() == null) ? 0 : getPlayerStatus().hashCode());
        result = prime * result + ((getScore() == null) ? 0 : getScore().hashCode());
        result = prime * result + ((getRank() == null) ? 0 : getRank().hashCode());
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
        sb.append(", joinTime=").append(joinTime);
        sb.append(", leaveTime=").append(leaveTime);
        sb.append(", playerStatus=").append(playerStatus);
        sb.append(", score=").append(score);
        sb.append(", rank=").append(rank);
        sb.append("]");
        return sb.toString();
    }
}