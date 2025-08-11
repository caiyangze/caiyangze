package com.zhentao.controller;

import com.zhentao.dto.NearbyUsersQueryDTO;
import com.zhentao.dto.UserLocationDTO;
import com.zhentao.service.LocationService;
import com.zhentao.utils.JwtService;
import com.zhentao.vo.NearbyUserVO;
import com.zhentao.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 地理位置控制器
 * @author zhentao
 * @date 2025/8/3
 */
@Slf4j
@RestController
@RequestMapping("/api/social/location")
@Validated
public class LocationController {

    @Autowired
    private LocationService locationService;
    
    /**
     * 更新用户位置信息
     */
    @PostMapping("/update")
    public Result updateLocation(@Valid @RequestBody UserLocationDTO locationDTO,
                                        HttpServletRequest request) {
        try {
            // 从请求头中获取token并解析用户ID
            String token = request.getHeader("token");
            if (token == null || token.isEmpty()) {
                return Result.error("未登录或token已过期");
            }

            Long userId = JwtService.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("token无效");
            }

            Boolean success = locationService.updateUserLocation(userId, locationDTO);
            if (success) {
                return Result.success("位置更新成功", true);
            } else {
                return Result.error("位置更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户位置失败", e);
            return Result.error("系统异常：" + e.getMessage());
        }
    }
    
    /**
     * 查询附近的用户
     */
    @PostMapping("/nearby")
    public Result getNearbyUsers(@Valid @RequestBody NearbyUsersQueryDTO queryDTO,
                                                    HttpServletRequest request) {
        try {
            // 从请求头中获取token并解析用户ID
            String token = request.getHeader("token");
            if (token == null || token.isEmpty()) {
                return Result.error("未登录或token已过期");
            }

            Long currentUserId = JwtService.getUserIdFromToken(token);
            if (currentUserId == null) {
                return Result.error("token无效");
            }

            // 更新用户在线状态
            locationService.updateUserOnlineStatus(currentUserId, true);

            List<NearbyUserVO> nearbyUsers = locationService.getNearbyUsers(currentUserId, queryDTO);
            return Result.success("查询成功", nearbyUsers);
        } catch (Exception e) {
            log.error("查询附近用户失败", e);
            return Result.error("系统异常：" + e.getMessage());
        }
    }

    /**
     * 发送招呼
     */
    @PostMapping("/greeting")
    public Result sendGreeting(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String token = request.getHeader("token");
            if (token == null || token.isEmpty()) {
                return Result.error("未登录或token已过期");
            }

            Long currentUserId = JwtService.getUserIdFromToken(token);
            if (currentUserId == null) {
                return Result.error("token无效");
            }

            Long toUserId = Long.parseLong(params.get("toUserId").toString());
            String message = params.getOrDefault("message", "你好，很高兴认识你！").toString();

            Boolean success = locationService.sendGreeting(currentUserId, toUserId, message);
            if (success) {
                return Result.success("招呼发送成功");
            } else {
                return Result.error("招呼发送失败，可能今天已经打过招呼了");
            }
        } catch (Exception e) {
            log.error("发送招呼失败", e);
            return Result.error("系统异常：" + e.getMessage());
        }
    }

    /**
     * 收藏用户
     */
    @PostMapping("/favorite")
    public Result favoriteUser(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String token = request.getHeader("token");
            if (token == null || token.isEmpty()) {
                return Result.error("未登录或token已过期");
            }

            Long currentUserId = JwtService.getUserIdFromToken(token);
            if (currentUserId == null) {
                return Result.error("token无效");
            }

            Long targetUserId = Long.parseLong(params.get("targetUserId").toString());
            Boolean success = locationService.favoriteUser(currentUserId, targetUserId);

            if (success) {
                return Result.success("收藏成功");
            } else {
                return Result.error("收藏失败");
            }
        } catch (Exception e) {
            log.error("收藏用户失败", e);
            return Result.error("系统异常：" + e.getMessage());
        }
    }

    /**
     * 取消收藏用户
     */
    @PostMapping("/unfavorite")
    public Result unfavoriteUser(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String token = request.getHeader("token");
            if (token == null || token.isEmpty()) {
                return Result.error("未登录或token已过期");
            }

            Long currentUserId = JwtService.getUserIdFromToken(token);
            if (currentUserId == null) {
                return Result.error("token无效");
            }

            Long targetUserId = Long.parseLong(params.get("targetUserId").toString());
            Boolean success = locationService.unfavoriteUser(currentUserId, targetUserId);

            if (success) {
                return Result.success("取消收藏成功");
            } else {
                return Result.error("取消收藏失败");
            }
        } catch (Exception e) {
            log.error("取消收藏用户失败", e);
            return Result.error("系统异常：" + e.getMessage());
        }
    }

    /**
     * 获取收藏的用户列表
     */
    @GetMapping("/favorites")
    public Result getFavoriteUsers(HttpServletRequest request) {
        try {
            String token = request.getHeader("token");
            if (token == null || token.isEmpty()) {
                return Result.error("未登录或token已过期");
            }

            Long currentUserId = JwtService.getUserIdFromToken(token);
            if (currentUserId == null) {
                return Result.error("token无效");
            }

            List<NearbyUserVO> favoriteUsers = locationService.getFavoriteUsers(currentUserId);
            return Result.success("查询成功", favoriteUsers);
        } catch (Exception e) {
            log.error("获取收藏用户列表失败", e);
            return Result.error("系统异常：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户当前位置信息
     */
    @GetMapping("/current")
    public Result getCurrentLocation(HttpServletRequest request) {
        try {
            // 从请求头中获取token并解析用户ID
            String token = request.getHeader("token");
            if (token == null || token.isEmpty()) {
                return Result.error("未登录或token已过期");
            }

            Long userId = JwtService.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("token无效");
            }

            UserLocationDTO location = locationService.getUserLocation(userId);
            if (location != null) {
                return Result.success("获取位置成功", location);
            } else {
                return Result.error("未找到位置信息");
            }
        } catch (Exception e) {
            log.error("获取用户位置失败", e);
            return Result.error("系统异常：" + e.getMessage());
        }
    }
    
    /**
     * 删除用户位置信息
     */
    @DeleteMapping("/remove")
    public Result removeLocation(HttpServletRequest request) {
        try {
            // 从请求头中获取token并解析用户ID
            String token = request.getHeader("token");
            if (token == null || token.isEmpty()) {
                return Result.error("未登录或token已过期");
            }

            Long userId = JwtService.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("token无效");
            }

            Boolean success = locationService.removeUserLocation(userId);
            if (success) {
                return Result.success("位置删除成功", true);
            } else {
                return Result.error("位置删除失败");
            }
        } catch (Exception e) {
            log.error("删除用户位置失败", e);
            return Result.error("系统异常：" + e.getMessage());
        }
    }
    
    /**
     * 计算两点之间的距离
     */
    @GetMapping("/distance")
    public Result calculateDistance(@RequestParam Double lon1,
                                          @RequestParam Double lat1,
                                          @RequestParam Double lon2,
                                          @RequestParam Double lat2) {
        try {
            Double distance = locationService.calculateDistance(lon1, lat1, lon2, lat2);
            return Result.success("距离计算成功", distance);
        } catch (Exception e) {
            log.error("计算距离失败", e);
            return Result.error("系统异常：" + e.getMessage());
        }
    }

    /**
     * 初始化测试数据（仅用于开发环境）
     */
    @PostMapping("/init/testData")
    public Result initTestData() {
        try {
            // 添加一些测试用户位置数据（北京周边）
            String[] testUsers = {"22", "23", "24", "25", "26", "27", "28", "29", "30"};
            double[][] testLocations = {
                {116.407526, 39.904030},  // 王府井
                {116.383331, 39.911456},  // 西单
                {116.417592, 39.921984},  // 三里屯
                {116.368904, 39.913423},  // 金融街
                {116.390000, 39.920000},  // 附近位置
                {116.454346, 39.928353},  // 朝阳公园
                {116.356532, 39.874632},  // 丰台
                {116.434546, 39.960234},  // 望京
                {116.325467, 39.945123}   // 海淀
            };

            for (int i = 0; i < testUsers.length && i < testLocations.length; i++) {
                UserLocationDTO locationDTO = new UserLocationDTO();
                locationDTO.setLongitude(testLocations[i][0]);
                locationDTO.setLatitude(testLocations[i][1]);
                locationDTO.setLocationName("测试位置" + (i + 1));
                locationDTO.setAddress("北京市测试地址" + (i + 1));

                locationService.updateUserLocation(Long.parseLong(testUsers[i]), locationDTO);
            }

            return Result.success("测试数据初始化成功", null);
        } catch (Exception e) {
            log.error("初始化测试数据失败", e);
            return Result.error("系统异常：" + e.getMessage());
        }
    }

    /**
     * 初始化河北测试数据
     */
    @PostMapping("/init/hebeiTestData")
    public Result initHebeiTestData() {
        try {
            // 添加一些河北石家庄周边的测试用户位置数据
            String[] testUsers = {"31", "32", "33", "34", "35", "36", "37", "38", "39"};
            double[][] testLocations = {
                {114.502461, 38.045474},  // 石家庄市中心
                {114.515234, 38.052341},  // 长安区
                {114.489567, 38.038912},  // 桥西区
                {114.523456, 38.041234},  // 裕华区
                {114.478901, 38.049876},  // 新华区
                {114.534567, 38.056789},  // 高新区
                {114.467890, 38.032145},  // 井陉矿区
                {114.545678, 38.063456},  // 正定县
                {114.456789, 38.027890}   // 鹿泉区
            };

            String[] locationNames = {
                "石家庄市中心", "长安区", "桥西区", "裕华区", "新华区",
                "高新区", "井陉矿区", "正定县", "鹿泉区"
            };

            for (int i = 0; i < testUsers.length && i < testLocations.length; i++) {
                UserLocationDTO locationDTO = new UserLocationDTO();
                locationDTO.setLongitude(testLocations[i][0]);
                locationDTO.setLatitude(testLocations[i][1]);
                locationDTO.setLocationName(locationNames[i]);
                locationDTO.setAddress("河北省石家庄市" + locationNames[i]);

                locationService.updateUserLocation(Long.parseLong(testUsers[i]), locationDTO);
            }

            return Result.success("河北测试数据初始化成功", null);
        } catch (Exception e) {
            log.error("初始化河北测试数据失败", e);
            return Result.error("系统异常：" + e.getMessage());
        }
    }
}
