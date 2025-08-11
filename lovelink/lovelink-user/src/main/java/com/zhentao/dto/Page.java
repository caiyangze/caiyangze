package com.zhentao.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 王恒飞
 * @date 2025/7/18
 * @ClassName Page
 */
@Data
public class Page implements Serializable {
    @TableField(exist = false)
    public Integer pageNum;
    @TableField(exist = false)
    public Integer pageSize;
}
