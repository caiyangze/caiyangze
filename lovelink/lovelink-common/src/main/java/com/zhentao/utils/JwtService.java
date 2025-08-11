package com.zhentao.utils;

import io.jsonwebtoken.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;


/**
 * @ClassName: JwtUtil
 * @Author: 阿巴阿巴
 * @Date: 2024年3月1日 09:29
 */
public class JwtService {

    // 设置token过期时间30分钟
    private static final long EXPIRE_TIME = 30 * 60 * 1000; // 30分钟
    // 加密KEY
    private static final String TOKEN_ENCRY_KEY = "MDk4ZjZi789y438h9jikog5tfr4dew7gg5ft4689iy6t5f4rd3y679uiy6t5f4ry9uyg5tf4rh9jikog5tf4rd38h9ujiyg5fr4d38h9juig5tf4rd3h9juiyg5ft4r9jigt5f4rY2Q0NjIxZDM3M2NhZGU0ZTgzMjYyN2I0ZjY";

    // 生成TOKEN
    public static String createToken(Map<String, Object> claimMaps) {
        long currentTime = System.currentTimeMillis();
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date(currentTime)) // 签发时间
                .setSubject("zhentao.bms.token") // 说明
                .setIssuer("zhentao.bms") // 签发者信息
                .setAudience("zhentao.bms.pc.b") // 接收用户
                .compressWith(CompressionCodecs.GZIP) // 数据压缩方式
                .signWith(SignatureAlgorithm.HS512, TOKEN_ENCRY_KEY) // 加密方式
                .setExpiration(new Date(currentTime + EXPIRE_TIME)) // 过期时间戳
                .addClaims(claimMaps) // cla信息
                .compact();
    }

    /**
     * 获取payload body信息
     *
     * @param token
     * @return
     */
    private static Claims getClaimsBody(String token) {
        try {
            Jws<Claims> jwt = Jwts.parser().setSigningKey(TOKEN_ENCRY_KEY).parseClaimsJws(token);
            return jwt.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取payload中claims的集合
     *
     * @param token
     * @return
     */
    public static Map<String, Object> getClaimsMap(String token) {
        try {
            Jws<Claims> jwt = Jwts.parser().setSigningKey(TOKEN_ENCRY_KEY).parseClaimsJws(token);
            return jwt.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取hearder body信息
     *
     * @param token
     * @return
     */
    public static JwsHeader getHeaderBody(String token) {
        Jws<Claims> jwt = Jwts.parser().setSigningKey(TOKEN_ENCRY_KEY).parseClaimsJws(token);
        return jwt.getHeader();
    }

    /**
     * 是否有效
     *
     * @param token
     * @return 1：有效，0：无效
     */
    public static int verifyToken(String token) {
        if(StringUtils.isBlank(token)){//token为空
            return -3;
        }
        try {
            Jwts.parser().setSigningKey(TOKEN_ENCRY_KEY).parseClaimsJws(token);
        } catch (ExpiredJwtException e) {//token过期
            return -2;
        } catch (JwtException e){//token被篡改
            return -1;
        }
        // 当前时间在有效期范围内
        return 1;
    }

    /**
     * 从token中获取用户ID
     *
     * @param token JWT token
     * @return 用户ID，如果获取失败返回null
     */
    public static Long getUserIdFromToken(String token) {
        try {
            if (verifyToken(token) == 1) {
                Map<String, Object> claimsMap = getClaimsMap(token);
                if (claimsMap != null && claimsMap.containsKey("userId")) {
                    return Long.valueOf(String.valueOf(claimsMap.get("userId")));
                }
            }
        } catch (Exception e) {
            // 忽略异常，返回null
        }
        return null;
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", "10086");
        map.put("username", "admin");
//        map.put("authorities", Arrays.asList("student:add","user:list"));

        String token = createToken(map);
        System.out.println(token);

        int i = verifyToken(token);
        System.out.println(i);


        Map<String, Object> claimsMap = getClaimsMap(token);
        System.out.println(claimsMap.get("userId"));
    }
}
