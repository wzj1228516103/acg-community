package com.acg.community.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, Object value, long seconds) {
        try {
            redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis set failed: key={}, error={}", key, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        try {
            return (T) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis get failed: key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis delete failed: key={}, error={}", key, e.getMessage());
        }
    }

    public void deleteByPrefix(String prefix) {
        try {
            Collection<String> keys = redisTemplate.keys(prefix + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("Redis deleteByPrefix failed: prefix={}, error={}", prefix, e.getMessage());
        }
    }

    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            return false;
        }
    }

    public <T> T getOrLoad(String key, long seconds, Supplier<T> loader) {
        T cached = get(key);
        if (cached != null) {
            return cached;
        }
        T value = loader.get();
        if (value != null) {
            set(key, value, seconds);
        }
        return value;
    }
}
