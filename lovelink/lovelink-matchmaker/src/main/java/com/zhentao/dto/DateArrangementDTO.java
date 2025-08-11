package com.zhentao.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 约会安排DTO
 * @author zhentao
 */
@Data
public class DateArrangementDTO {
    
    /**
     * 牵线申请ID
     */
    @NotNull(message = "牵线申请ID不能为空")
    private Long requestId;
    
    /**
     * 约会时间
     */
    @NotNull(message = "约会时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dateTime;
    
    /**
     * 约会地点
     */
    @NotNull(message = "约会地点不能为空")
    @Size(max = 100, message = "约会地点不能超过100字")
    private String dateLocation;
    
    /**
     * 约会类型：1-咖啡厅，2-餐厅，3-电影院，4-其他
     */
    @NotNull(message = "约会类型不能为空")
    private Integer dateType;
    
    /**
     * 约会计划详情
     */
    @Size(max = 500, message = "约会计划详情不能超过500字")
    private String datePlan;
}
