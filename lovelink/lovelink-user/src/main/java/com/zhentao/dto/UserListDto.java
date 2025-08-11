package com.zhentao.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 王恒飞
 * @date 2025/7/18
 * @ClassName UserListDto
 */
@Data
public class UserListDto extends Page implements Serializable {
    private Integer userId;
}
