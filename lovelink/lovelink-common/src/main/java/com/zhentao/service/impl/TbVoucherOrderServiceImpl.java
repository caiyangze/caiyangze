package com.zhentao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhentao.pojo.TbSeckilVoucher;
import com.zhentao.pojo.TbVoucherOrder;
import com.zhentao.service.TbSeckilVoucherService;
import com.zhentao.service.TbVoucherOrderService;
import com.zhentao.mapper.TbVoucherOrderMapper;
import com.zhentao.utils.Result;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
* @author a2634
* @description 针对表【tb_voucher_order】的数据库操作Service实现
* @createDate 2025-07-20 19:55:51
*/
@Service
public class TbVoucherOrderServiceImpl extends ServiceImpl<TbVoucherOrderMapper, TbVoucherOrder>
    implements TbVoucherOrderService{
@Autowired
private TbSeckilVoucherService seckilVoucherService;
@Autowired
StringRedisTemplate redisTemplate;
@Autowired
RedisIdWorker redisIdWorker;
    @Autowired
    private TbVoucherOrderService tbVoucherOrderService;

    @Override
    public Result seckillVoucher(Long voucherId, Integer userId) {
        //查询优惠券
        TbSeckilVoucher voucher = seckilVoucherService.getById(voucherId);
        //判断秒杀是否开始
        if(voucher.getBeginTime().isAfter(LocalDateTime.now())){
            //尚未开始
            return Result.ERROR("尚未开始");
        }
        //判断秒杀是否结束
        if(voucher.getEndTime().isBefore(LocalDateTime.now())){
            //尚未开始
            return Result.ERROR("尚未开始");
        }
        //判断库存是否充足
        if(voucher.getStock() < 1){
            //库存不足
            return Result.ERROR("库存不足");
        }
        //创建锁对象
        SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, redisTemplate);
        //获取锁对象
        boolean isLock = lock.tryLock(1200);
        if(!isLock){
            return Result.ERROR("不允许重复下单");
        }
        try {
            //获取代理对象（事物）
            TbVoucherOrderService proxy = (TbVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId,userId);
        } finally {
            //释放锁
            lock.unlock();
        }
    }

    @Override
    public Result createVoucherOrder(Long voucherId,Integer userId) {
        QueryWrapper<TbVoucherOrder> voucherOrderQueryWrapper=new QueryWrapper<>();
        voucherOrderQueryWrapper.eq("user_id",userId);
        voucherOrderQueryWrapper.eq("voucher_id",voucherId);
        long count = tbVoucherOrderService.count(voucherOrderQueryWrapper);
        if(count>0){
            return Result.ERROR("用户已经购买过一次");
        }
        //扣减库存
        boolean success = seckilVoucherService.update()
                .setSql("stock= stock -1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)
                .update();
        if(!success){
            //扣减库存
            return Result.ERROR("库存不足");
        }
        //6.创建订单
        TbVoucherOrder voucherOrder = new TbVoucherOrder();
        // 6.1.订单id
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        // 6.2.用户id
        voucherOrder.setUserId(Long.valueOf(userId));
        // 6.3.代金券id
        voucherOrder.setVoucherId(voucherId);
        voucherOrder.setPayType(1);
        voucherOrder.setStatus(2);
        voucherOrder.setCreateTime(LocalDateTime.now());
        voucherOrder.setUpdateTime(LocalDateTime.now());
        voucherOrder.setPayTime(LocalDateTime.now());
        save(voucherOrder);
        return Result.OK(orderId);
    }
}




