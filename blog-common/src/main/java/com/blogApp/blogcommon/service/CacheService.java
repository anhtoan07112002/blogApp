package com.blogApp.blogcommon.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service chung để xử lý cache với prefix theo service
 */
@Service
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String cachePrefix;

    public CacheService(RedisTemplate<String, Object> redisTemplate, 
                        @Value("${app.cache.prefix:blog}") String cachePrefix) {
        this.redisTemplate = redisTemplate;
        this.cachePrefix = cachePrefix;
    }

    /**
     * Tạo key với prefix của service
     */
    private String createKey(String cacheType, String key) {
        return cachePrefix + cacheType + ":" + key;
    }

    /**
     * Lưu giá trị vào cache
     */
    public void set(String cacheType, String key, Object value) {
        redisTemplate.opsForValue().set(createKey(cacheType, key), value);
    }

    /**
     * Lưu giá trị vào cache với thời gian hết hạn
     */
    public void set(String cacheType, String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(createKey(cacheType, key), value, timeout, unit);
    }

    /**
     * Lấy giá trị từ cache
     */
    public Object get(String cacheType, String key) {
        return redisTemplate.opsForValue().get(createKey(cacheType, key));
    }

    /**
     * Xóa giá trị khỏi cache
     */
    public Boolean delete(String cacheType, String key) {
        return redisTemplate.delete(createKey(cacheType, key));
    }

    /**
     * Kiểm tra key có tồn tại không
     */
    public Boolean hasKey(String cacheType, String key) {
        return redisTemplate.hasKey(createKey(cacheType, key));
    }
}