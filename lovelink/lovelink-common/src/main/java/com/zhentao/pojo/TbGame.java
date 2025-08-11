package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_game
 */
@TableName(value ="tb_game")
@Data
public class TbGame {
    /**
     * 游戏ID
     */
    @TableId(type = IdType.AUTO)
    private Long gameId;

    /**
     * 游戏名称
     */
    private String gameName;

    /**
     * 游戏描述
     */
    private String gameDesc;

    /**
     * 游戏图标URL
     */
    private String gameIcon;

    /**
     * 游戏类型：1-性格测试，2-默契挑战，3-互动游戏，4-其他
     */
    private Integer gameType;

    /**
     * 最少参与人数
     */
    private Integer playerMin;

    /**
     * 最多参与人数
     */
    private Integer playerMax;

    /**
     * 预计游戏时长(分钟)
     */
    private Integer duration;

    /**
     * 是否VIP专属：0-否，1-是
     */
    private Integer isVip;

    /**
     * 游戏状态：0-下线，1-上线
     */
    private Integer gameStatus;

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
        TbGame other = (TbGame) that;
        return (this.getGameId() == null ? other.getGameId() == null : this.getGameId().equals(other.getGameId()))
            && (this.getGameName() == null ? other.getGameName() == null : this.getGameName().equals(other.getGameName()))
            && (this.getGameDesc() == null ? other.getGameDesc() == null : this.getGameDesc().equals(other.getGameDesc()))
            && (this.getGameIcon() == null ? other.getGameIcon() == null : this.getGameIcon().equals(other.getGameIcon()))
            && (this.getGameType() == null ? other.getGameType() == null : this.getGameType().equals(other.getGameType()))
            && (this.getPlayerMin() == null ? other.getPlayerMin() == null : this.getPlayerMin().equals(other.getPlayerMin()))
            && (this.getPlayerMax() == null ? other.getPlayerMax() == null : this.getPlayerMax().equals(other.getPlayerMax()))
            && (this.getDuration() == null ? other.getDuration() == null : this.getDuration().equals(other.getDuration()))
            && (this.getIsVip() == null ? other.getIsVip() == null : this.getIsVip().equals(other.getIsVip()))
            && (this.getGameStatus() == null ? other.getGameStatus() == null : this.getGameStatus().equals(other.getGameStatus()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getGameId() == null) ? 0 : getGameId().hashCode());
        result = prime * result + ((getGameName() == null) ? 0 : getGameName().hashCode());
        result = prime * result + ((getGameDesc() == null) ? 0 : getGameDesc().hashCode());
        result = prime * result + ((getGameIcon() == null) ? 0 : getGameIcon().hashCode());
        result = prime * result + ((getGameType() == null) ? 0 : getGameType().hashCode());
        result = prime * result + ((getPlayerMin() == null) ? 0 : getPlayerMin().hashCode());
        result = prime * result + ((getPlayerMax() == null) ? 0 : getPlayerMax().hashCode());
        result = prime * result + ((getDuration() == null) ? 0 : getDuration().hashCode());
        result = prime * result + ((getIsVip() == null) ? 0 : getIsVip().hashCode());
        result = prime * result + ((getGameStatus() == null) ? 0 : getGameStatus().hashCode());
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
        sb.append(", gameId=").append(gameId);
        sb.append(", gameName=").append(gameName);
        sb.append(", gameDesc=").append(gameDesc);
        sb.append(", gameIcon=").append(gameIcon);
        sb.append(", gameType=").append(gameType);
        sb.append(", playerMin=").append(playerMin);
        sb.append(", playerMax=").append(playerMax);
        sb.append(", duration=").append(duration);
        sb.append(", isVip=").append(isVip);
        sb.append(", gameStatus=").append(gameStatus);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}