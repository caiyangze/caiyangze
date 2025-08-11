package com.zhentao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhentao.dto.VoucherDto;
import com.zhentao.pojo.TbSeckilVoucher;
import com.zhentao.pojo.TbVoucher;
import com.zhentao.pojo.TbVoucherOrder;
import com.zhentao.service.TbSeckilVoucherService;
import com.zhentao.service.TbVoucherOrderService;
import com.zhentao.service.TbVoucherService;
import com.zhentao.utils.JwtService;
import com.zhentao.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author 王恒飞
 * @date 2025/7/21
 * @ClassName VoucherController
 */
@RestController
@RequestMapping("voucher")
public class VoucherController {
    @Autowired
    private TbVoucherService voucherService;
    @Autowired
    private TbSeckilVoucherService seckilVoucherService;
    @Autowired
    private TbVoucherOrderService voucherOrderService;
    @RequestMapping("list")
    public Result list(@RequestBody VoucherDto voucherDto) {
        QueryWrapper<TbVoucher> voucherQueryWrapper=new QueryWrapper<>();
        voucherQueryWrapper.eq("status",1);
        Page<TbVoucher> page = voucherService.page(new Page<>(voucherDto.getPageNum(), voucherDto.getPageSize()), voucherQueryWrapper);
        List<TbVoucher> list = page.getRecords();
        for (TbVoucher voucher : list){
            TbSeckilVoucher byId = seckilVoucherService.getById(voucher.getId());
            voucher.setSeckilVoucher(byId);
        }
        return Result.success(page);
    }
    @RequestMapping("seckill")
    public Result seckillVoucher(@RequestParam Long voucherId, @RequestHeader("token") String token) {
        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Integer userId = Integer.parseInt(String.valueOf( claimsMap.get("userId")));
        return voucherOrderService.seckillVoucher(voucherId,userId);
    }
    @RequestMapping("getMyVoucherList")
    public Result getMyVoucherList(@RequestBody VoucherDto voucherDto, @RequestHeader("token") String token) {
        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Integer userId = Integer.parseInt(String.valueOf( claimsMap.get("userId")));
        QueryWrapper<TbVoucherOrder> voucherOrderQueryWrapper=new QueryWrapper<>();
        voucherOrderQueryWrapper.eq("user_id",userId);

        // 如果传入了状态参数，则按状态筛选；否则查询所有状态的优惠券
        if (voucherDto.getStatus() != null) {
            voucherOrderQueryWrapper.eq("status", voucherDto.getStatus());
        } else {
            // 查询已支付的优惠券（包括未使用和已使用）
            voucherOrderQueryWrapper.in("status", 2, 3); // 2-未使用，3-已核销
        }

        // 按创建时间倒序排列
        voucherOrderQueryWrapper.orderByDesc("create_time");

        Page<TbVoucherOrder> page = voucherOrderService.page(new Page<>(voucherDto.getPageNum(), voucherDto.getPageSize()), voucherOrderQueryWrapper);
        List<TbVoucherOrder> list = page.getRecords();
        for (TbVoucherOrder voucherOrder : list){
            TbVoucher byId = voucherService.getById(voucherOrder.getVoucherId());
            voucherOrder.setVoucher(byId);
            // 添加调试日志
            System.out.println("优惠券订单ID: " + voucherOrder.getId() + ", 状态: " + voucherOrder.getStatus() +
                ", 创建时间: " + voucherOrder.getCreateTime() + ", 使用时间: " + voucherOrder.getUseTime());
        }
        return Result.success(page);
    }

    /**
     * 测试接口：手动更新优惠券状态
     */
    @RequestMapping("testUpdateVoucherStatus")
    public Result testUpdateVoucherStatus(@RequestParam Long voucherOrderId, @RequestParam Integer status) {
        try {
            TbVoucherOrder voucherOrder = voucherOrderService.getById(voucherOrderId);
            if (voucherOrder == null) {
                return Result.error("优惠券订单不存在");
            }

            System.out.println("更新前状态：" + voucherOrder.getStatus());
            voucherOrder.setStatus(status);
            voucherOrder.setUpdateTime(java.time.LocalDateTime.now());
            if (status == 3) {
                voucherOrder.setUseTime(java.time.LocalDateTime.now());
            }

            boolean result = voucherOrderService.updateById(voucherOrder);
            System.out.println("更新结果：" + result + "，更新后状态：" + voucherOrder.getStatus());

            return Result.success("更新成功，结果：" + result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败：" + e.getMessage());
        }
    }
}
