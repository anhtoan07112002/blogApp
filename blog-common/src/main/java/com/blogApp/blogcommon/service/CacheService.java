package com.blogApp.blogcommon.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service chung để xử lý cache với prefix theo service
 */
@Slf4j
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
        String fullKey = cachePrefix + cacheType + ":" + key;
        log.debug("Tạo cache key: {}", fullKey);
        return fullKey;
    }

    /**
     * Lưu giá trị vào cache
     */
    public void set(String cacheType, String key, Object value) {
        String fullKey = createKey(cacheType, key);
        log.debug("Lưu vào cache: key={}, valueType={}", 
                 fullKey, value != null ? value.getClass().getName() : "null");
        redisTemplate.opsForValue().set(fullKey, value);
    }

    /**
     * Lưu giá trị vào cache với thời gian hết hạn
     */
    public void set(String cacheType, String key, Object value, long timeout, TimeUnit unit) {
        String fullKey = createKey(cacheType, key);
        log.debug("Lưu vào cache có TTL: key={}, valueType={}, timeout={}, unit={}", 
                 fullKey, value != null ? value.getClass().getName() : "null", timeout, unit);
        try {
            redisTemplate.opsForValue().set(fullKey, value, timeout, unit);
        } catch (Exception e) {
            log.error("Lỗi khi lưu vào cache: key={}, error={}", fullKey, e.getMessage(), e);
        }
    }

    /**
     * Lấy giá trị từ cache
     */
    public Object get(String cacheType, String key) {
        String fullKey = createKey(cacheType, key);
        log.debug("Lấy từ cache: key={}", fullKey);
        
        try {
            Object value = redisTemplate.opsForValue().get(fullKey);
            log.debug("Kết quả từ cache: key={}, found={}, valueType={}", 
                     fullKey, (value != null), (value != null ? value.getClass().getName() : "null"));
            return value;
        } catch (Exception e) {
            log.error("Lỗi khi lấy từ cache: key={}, error={}", fullKey, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Xóa giá trị khỏi cache
     */
    public Boolean delete(String cacheType, String key) {
        String fullKey = createKey(cacheType, key);
        log.debug("Xóa khỏi cache: key={}", fullKey);
        try {
            return redisTemplate.delete(fullKey);
        } catch (Exception e) {
            log.error("Lỗi khi xóa khỏi cache: key={}, error={}", fullKey, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Kiểm tra key có tồn tại không
     */
    public Boolean hasKey(String cacheType, String key) {
        String fullKey = createKey(cacheType, key);
        boolean exists = redisTemplate.hasKey(fullKey);
        log.debug("Kiểm tra key tồn tại: key={}, exists={}", fullKey, exists);
        return exists;
    }
}