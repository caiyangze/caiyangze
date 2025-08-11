package com.zhentao.controller;

import com.zhentao.service.DateStatusScheduleService;
import com.zhentao.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 退款测试控制器 - 仅用于测试和调试
 */
@RestController
@RequestMapping("/test/refund")
public class RefundTestController {

    @Autowired
    private DateStatusScheduleService dateStatusScheduleService;

    /**
     * 手动触发退款检查
     */
    @PostMapping("/trigger")
    public Result triggerRefundCheck() {
        try {
            // 手动调用退款检查方法
            dateStatusScheduleService.checkRefundableOrders();
            return Result.success("退款检查已触发，请查看日志");
        } catch (Exception e) {
            return Result.error("触发退款检查失败：" + e.getMessage());
        }
    }

    /**
     * 获取退款检查状态
     */
    @GetMapping("/status")
    public Result getRefundStatus() {
        try {
            // 这里可以返回一些退款检查的统计信息
            return Result.success("退款检查功能正常运行");
        } catch (Exception e) {
            return Result.error("获取退款状态失败：" + e.getMessage());
        }
    }
}
