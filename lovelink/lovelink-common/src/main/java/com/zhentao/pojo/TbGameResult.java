package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_game_result
 */
@TableName(value ="tb_game_result")
@Data
public class TbGameResult {
    /**
     * 结果ID
     */
    @TableId(type = IdType.AUTO)
    private Long resultId;

    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * 结果类型：1-性格测试结果，2-匹配度结果，3-互动游戏结果
     */
    private Integer resultType;

    /**
     * 结果数据（JSON格式）
     */
    private Object resultData;

    /**
     * 匹配得分
     */
    private Integer matchScore;

    /**
     * 匹配等级：1-5级
     */
    private Integer matchLevel;

    /**
     * 结果摘要
     */
    private String resultSummary;

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
        TbGameResult other = (TbGameResult) that;
        return (this.getResultId() == null ? other.getResultId() == null : this.getResultId().equals(other.getResultId()))
            && (this.getRoomId() == null ? other.getRoomId() == null : this.getRoomId().equals(other.getRoomId()))
            && (this.getResultType() == null ? other.getResultType() == null : this.getResultType().equals(other.getResultType()))
            && (this.getResultData() == null ? other.getResultData() == null : this.getResultData().equals(other.getResultData()))
            && (this.getMatchScore() == null ? other.getMatchScore() == null : this.getMatchScore().equals(other.getMatchScore()))
            && (this.getMatchLevel() == null ? other.getMatchLevel() == null : this.getMatchLevel().equals(other.getMatchLevel()))
            && (this.getResultSummary() == null ? other.getResultSummary() == null : this.getResultSummary().equals(other.getResultSummary()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getResultId() == null) ? 0 : getResultId().hashCode());
        result = prime * result + ((getRoomId() == null) ? 0 : getRoomId().hashCode());
        result = prime * result + ((getResultType() == null) ? 0 : getResultType().hashCode());
        result = prime * result + ((getResultData() == null) ? 0 : getResultData().hashCode());
        result = prime * result + ((getMatchScore() == null) ? 0 : getMatchScore().hashCode());
        result = prime * result + ((getMatchLevel() == null) ? 0 : getMatchLevel().hashCode());
        result = prime * result + ((getResultSummary() == null) ? 0 : getResultSummary().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", resultId=").append(resultId);
        sb.append(", roomId=").append(roomId);
        sb.append(", resultType=").append(resultType);
        sb.append(", resultData=").append(resultData);
        sb.append(", matchScore=").append(matchScore);
        sb.append(", matchLevel=").append(matchLevel);
        sb.append(", resultSummary=").append(resultSummary);
        sb.append(", createdAt=").append(createdAt);
        sb.append("]");
        return sb.toString();
    }
}