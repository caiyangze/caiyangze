package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_permission
 */
@TableName(value ="tb_permission")
@Data
public class TbPermission {
    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
    private Long permissionId;

    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 权限标识
     */
    private String permissionKey;

    /**
     * 权限描述
     */
    private String permissionDesc;

    /**
     * 父权限ID
     */
    private Long parentId;

    /**
     * 权限类型：0-目录，1-菜单，2-按钮，3-接口
     */
    private Integer permissionType;

    /**
     * 路径（菜单使用）
     */
    private String path;

    /**
     * 组件（菜单使用）
     */
    private String component;

    /**
     * 图标（菜单使用）
     */
    private String icon;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 是否可见：0-否，1-是
     */
    private Integer isVisible;

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
        TbPermission other = (TbPermission) that;
        return (this.getPermissionId() == null ? other.getPermissionId() == null : this.getPermissionId().equals(other.getPermissionId()))
            && (this.getPermissionName() == null ? other.getPermissionName() == null : this.getPermissionName().equals(other.getPermissionName()))
            && (this.getPermissionKey() == null ? other.getPermissionKey() == null : this.getPermissionKey().equals(other.getPermissionKey()))
            && (this.getPermissionDesc() == null ? other.getPermissionDesc() == null : this.getPermissionDesc().equals(other.getPermissionDesc()))
            && (this.getParentId() == null ? other.getParentId() == null : this.getParentId().equals(other.getParentId()))
            && (this.getPermissionType() == null ? other.getPermissionType() == null : this.getPermissionType().equals(other.getPermissionType()))
            && (this.getPath() == null ? other.getPath() == null : this.getPath().equals(other.getPath()))
            && (this.getComponent() == null ? other.getComponent() == null : this.getComponent().equals(other.getComponent()))
            && (this.getIcon() == null ? other.getIcon() == null : this.getIcon().equals(other.getIcon()))
            && (this.getSortOrder() == null ? other.getSortOrder() == null : this.getSortOrder().equals(other.getSortOrder()))
            && (this.getIsVisible() == null ? other.getIsVisible() == null : this.getIsVisible().equals(other.getIsVisible()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getPermissionId() == null) ? 0 : getPermissionId().hashCode());
        result = prime * result + ((getPermissionName() == null) ? 0 : getPermissionName().hashCode());
        result = prime * result + ((getPermissionKey() == null) ? 0 : getPermissionKey().hashCode());
        result = prime * result + ((getPermissionDesc() == null) ? 0 : getPermissionDesc().hashCode());
        result = prime * result + ((getParentId() == null) ? 0 : getParentId().hashCode());
        result = prime * result + ((getPermissionType() == null) ? 0 : getPermissionType().hashCode());
        result = prime * result + ((getPath() == null) ? 0 : getPath().hashCode());
        result = prime * result + ((getComponent() == null) ? 0 : getComponent().hashCode());
        result = prime * result + ((getIcon() == null) ? 0 : getIcon().hashCode());
        result = prime * result + ((getSortOrder() == null) ? 0 : getSortOrder().hashCode());
        result = prime * result + ((getIsVisible() == null) ? 0 : getIsVisible().hashCode());
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
        sb.append(", permissionId=").append(permissionId);
        sb.append(", permissionName=").append(permissionName);
        sb.append(", permissionKey=").append(permissionKey);
        sb.append(", permissionDesc=").append(permissionDesc);
        sb.append(", parentId=").append(parentId);
        sb.append(", permissionType=").append(permissionType);
        sb.append(", path=").append(path);
        sb.append(", component=").append(component);
        sb.append(", icon=").append(icon);
        sb.append(", sortOrder=").append(sortOrder);
        sb.append(", isVisible=").append(isVisible);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}