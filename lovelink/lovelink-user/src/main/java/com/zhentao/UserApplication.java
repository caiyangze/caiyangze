package com.zhentao;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

/**
 * 用户服务启动类
 *
 * @author zhentao
 */
@SpringBootApplication
@MapperScan("com.zhentao.mapper")
@ComponentScan(basePackages = {"com.zhentao"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableScheduling
@EnableWebSocket
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
        System.out.println("\n" +
                "#                                                    __----~~~~~~~~~~~------___\n" +
                "#                                   .  .   ~~//====......          __--~ ~~\n" +
                "#                   -.            \\_|//     |||\\\\  ~~~~~~::::... /~\n" +
                "#                ___-==_       _-~o~  \\/    |||  \\\\            _/~~-\n" +
                "#        __---~~~.==~||\\=_    -_--~/_-~|-   |\\\\   \\\\        _/~\n" +
                "#    _-~~     .=~    |  \\\\-_    '-~7  /-   /  ||    \\      /\n" +
                "#  .~       .~       |   \\\\ -_    /  /-   /   ||      \\   /\n" +
                "# /  ____  /         |     \\\\ ~-_/  /|- _/   .||       \\ /\n" +
                "# |~~    ~~|--~~~~--_ \\     ~==-/   | \\~--===~~        .\\\n" +
                "#          '         ~-|      /|    |-~\\~~       __--~~\n" +
                "#                      |-~~-_/ |    |   ~\\_   _-~            /\\\n" +
                "#                           /  \\     \\__   \\/~                \\__\n" +
                "#                       _--~ _/ | .-~~____--~-/                  ~~==.\n" +
                "#                      ((->/~   '.|||' -_|    ~~-/ ,              . _||\n" +
                "#                                 -_     ~\\      ~~---l__i__i__i--~~_/\n" +
                "#                                 _-~-__   ~)  \\--______________--~~\n" +
                "#                               //.-~~~-~_--~- |-------~~~~~~~~\n" +
                "#                                      //.-~~~--\\\n" +
                "#                  神兽保佑\n" +
                "#                代码无大个儿!");
    }
}