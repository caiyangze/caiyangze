package com.zhentao.utils;

import java.util.Map;

public class ThreadLocalUtil {
    // 提供ThreadLocal对象
    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal();

    // 获取数据的方法，根据键获取值
    public static <T> T get(){
        return (T) THREAD_LOCAL.get();
    }

    // 存储数据的方法，储存键值对
    public static void set(Object value){
        THREAD_LOCAL.set(value);
    }

    // 删除数据的方法，清除ThreadLocal,防止内存溢出
    public static void remove(){
        THREAD_LOCAL.remove();
    }

    /**
     * 获取当前用户ID
     *
     * @return 当前用户ID
     */
    public static Long getCurrentUserId() {
        Object value = THREAD_LOCAL.get();
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            Object userId = map.get("userId");
            if (userId instanceof Long) {
                return (Long) userId;
            } else if (userId instanceof String) {
                return Long.valueOf((String) userId);
            } else if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            }
        } else if (value instanceof Long) {
            return (Long) value;
        }
        return null;
    }

    /**
     * 设置当前用户ID
     *
     * @param userId 用户ID
     */
    public static void setCurrentUserId(Long userId) {
        THREAD_LOCAL.set(userId);
    }
}
