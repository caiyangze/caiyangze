package com.zhentao.controller;

import com.zhentao.pojo.TbBanner;
import com.zhentao.service.TbBannerService;
import com.zhentao.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: 阿巴阿巴
 * @CreateTime: 2025-07-10 15:04:33
 * @Description: 轮播图控制层
 * @blessing: 佛祖保佑    永无BUG
 */
@RestController
@RequestMapping("lunb")
public class LunbController {
    @Autowired
    private TbBannerService tbBannerService;
    @RequestMapping("lunbList")
    public Result lunbList(){
        List<TbBanner> list = tbBannerService.list();
        if (list != null) {
            return Result.OK(list);
        }
        return Result.ERROR();
    }
}
