package com.zhentao.service;

import com.zhentao.pojo.TbVoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhentao.utils.Result;

/**
* @author a2634
* @description 针对表【tb_voucher_order】的数据库操作Service
* @createDate 2025-07-20 19:55:51
*/
public interface TbVoucherOrderService extends IService<TbVoucherOrder> {

    Result seckillVoucher(Long voucherId, Integer userId);
    Result createVoucherOrder(Long voucherId,Integer userId);

}
