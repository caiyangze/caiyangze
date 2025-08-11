package com.zhentao.feign;

import com.zhentao.dto.WalletDTO;
import com.zhentao.service.WalletService;
import com.zhentao.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 钱包服务 Feign 客户端
 *
 * @author zhentao
 */
@FeignClient(name = "lovelink-user", path = "/wallet", url = "${feign.client.lovelink-user.url:http://localhost:9001}")
public interface WalletServiceClient extends WalletService {

    @Override
    @GetMapping("/info/{userId}")
    Result getWalletInfo(@PathVariable("userId") Long userId);

    @Override
    @GetMapping("/transactions")
    Result getTransactions(@RequestParam("userId") Long userId,
                           @RequestParam("pageNum") Integer pageNum,
                           @RequestParam("pageSize") Integer pageSize,
                           @RequestParam(value = "type", required = false) Integer type);

    @Override
    @PostMapping("/recharge/internal")
    Result recharge(@RequestBody WalletDTO walletDTO);



    @Override
    @PostMapping("/consume")
    Result consume(@RequestBody WalletDTO walletDTO);

    @Override
    @PostMapping("/consume-with-voucher")
    Result consumeWithVoucher(@RequestBody WalletDTO walletDTO);

    @Override
    @PostMapping("/income")
    Result income(@RequestBody WalletDTO walletDTO);

    @Override
    @PostMapping("/withdraw")
    Result withdraw(@RequestBody WalletDTO walletDTO);

    @Override
    @GetMapping("/statistics/{userId}")
    Result getStatistics(@PathVariable("userId") Long userId);

    @Override
    @PostMapping("/create/{userId}")
    Result createWallet(@PathVariable("userId") Long userId);

    @Override
    @GetMapping("/status/{userId}")
    Result checkWalletStatus(@PathVariable("userId") Long userId);

    @Override
    @PutMapping("/status/{userId}")
    Result updateWalletStatus(@PathVariable("userId") Long userId, @RequestParam("status") Integer status);
}
