package com.zhentao.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class AmapIpLocation {
    // 替换为你的高德API Key
    private static final String API_KEY = "da0db48121bc669e9d9e81bf5d1940c0";
    // IP定位接口地址
    private static final String IP_LOCATION_URL = "https://restapi.amap.com/v3/ip";

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据IP地址获取省份信息
     * @param ip IP地址，为空则自动获取请求IP
     * @return 省份名称，获取失败返回null
     */
    public String getProvinceByIp(String ip) {
        try {
            // 构建请求参数
            StringBuilder params = new StringBuilder();
            params.append("key=").append(URLEncoder.encode(API_KEY, StandardCharsets.UTF_8.name()));
            if (ip != null && !ip.isEmpty()) {
                params.append("&ip=").append(URLEncoder.encode(ip, StandardCharsets.UTF_8.name()));
            }

            // 创建URL连接
            URL url = new URL(IP_LOCATION_URL + "?" + params.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 连接超时时间
            connection.setReadTimeout(5000);    // 读取超时时间

            // 读取响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                inputStream.close();

                // 解析JSON响应
                String responseStr = response.toString();
                log.info("高德IP定位响应: {}", responseStr);

                JsonNode jsonNode = objectMapper.readTree(responseStr);
                if ("1".equals(jsonNode.get("status").asText())) {
                    String province = jsonNode.get("province").asText();
                    log.info("IP: {} 对应省份: {}", ip, province);
                    return province;
                } else {
                    log.warn("高德IP定位失败: {}", jsonNode.get("info").asText());
                }
            } else {
                log.error("高德IP定位请求失败，响应码: {}", responseCode);
            }
            connection.disconnect();

        } catch (Exception e) {
            log.error("IP定位异常: ", e);
        }

        return null;
    }

    /**
     * 测试方法
     */
    public static void main(String[] args) {
        AmapIpLocation location = new AmapIpLocation();
        String province = location.getProvinceByIp("");
        System.out.println(province);
    }
}
