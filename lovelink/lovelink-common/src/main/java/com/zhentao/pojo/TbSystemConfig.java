package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_system_config
 */
@TableName(value ="tb_system_config")
@Data
public class TbSystemConfig {
    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
    private Long configId;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置描述
     */
    private String configDesc;

    /**
     * 配置类型：1-系统参数，2-业务参数，3-算法参数
     */
    private Integer configType;

    /**
     * 是否可编辑：0-否，1-是
     */
    private Integer isEditable;

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
        TbSystemConfig other = (TbSystemConfig) that;
        return (this.getConfigId() == null ? other.getConfigId() == null : this.getConfigId().equals(other.getConfigId()))
            && (this.getConfigKey() == null ? other.getConfigKey() == null : this.getConfigKey().equals(other.getConfigKey()))
            && (this.getConfigValue() == null ? other.getConfigValue() == null : this.getConfigValue().equals(other.getConfigValue()))
            && (this.getConfigDesc() == null ? other.getConfigDesc() == null : this.getConfigDesc().equals(other.getConfigDesc()))
            && (this.getConfigType() == null ? other.getConfigType() == null : this.getConfigType().equals(other.getConfigType()))
            && (this.getIsEditable() == null ? other.getIsEditable() == null : this.getIsEditable().equals(other.getIsEditable()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getConfigId() == null) ? 0 : getConfigId().hashCode());
        result = prime * result + ((getConfigKey() == null) ? 0 : getConfigKey().hashCode());
        result = prime * result + ((getConfigValue() == null) ? 0 : getConfigValue().hashCode());
        result = prime * result + ((getConfigDesc() == null) ? 0 : getConfigDesc().hashCode());
        result = prime * result + ((getConfigType() == null) ? 0 : getConfigType().hashCode());
        result = prime * result + ((getIsEditable() == null) ? 0 : getIsEditable().hashCode());
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
        sb.append(", configId=").append(configId);
        sb.append(", configKey=").append(configKey);
        sb.append(", configValue=").append(configValue);
        sb.append(", configDesc=").append(configDesc);
        sb.append(", configType=").append(configType);
        sb.append(", isEditable=").append(isEditable);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}