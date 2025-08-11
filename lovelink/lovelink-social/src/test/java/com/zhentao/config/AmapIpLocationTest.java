package com.zhentao.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 高德IP定位服务测试
 */
@SpringBootTest
public class AmapIpLocationTest {
    
    @Test
    public void testGetProvinceByIp() {
        AmapIpLocation amapIpLocation = new AmapIpLocation();
        
        // 测试获取当前IP的省份
        String province = amapIpLocation.getProvinceByIp("");
        System.out.println("当前IP对应省份: " + province);
        
        // 测试指定IP的省份（北京的IP）
        String beijingProvince = amapIpLocation.getProvinceByIp("123.125.71.36");
        System.out.println("北京IP对应省份: " + beijingProvince);
        
        // 测试指定IP的省份（上海的IP）
        String shanghaiProvince = amapIpLocation.getProvinceByIp("101.227.131.220");
        System.out.println("上海IP对应省份: " + shanghaiProvince);
    }
}
