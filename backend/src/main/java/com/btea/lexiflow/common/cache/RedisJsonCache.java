package com.btea.lexiflow.common.cache;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.btea.lexiflow.common.constant.RedisCacheConstant.CACHE_TTL_JITTER_RATIO;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: Redis缓存工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisJsonCache {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 读取单个JSON缓存
     *
     * @param key 缓存键
     * @param type 目标类型
     * @param <T> 目标类型
     * @return 缓存值，不存在或读取失败时返回null
     */
    public <T> T get(String key, Class<T> type) {
        String value = getRaw(key);
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.readValue(value, type);
        } catch (Exception e) {
            log.warn("Redis缓存反序列化失败: key={}, errorType={}", key, e.getClass().getSimpleName());
            delete(key);
            return null;
        }
    }

    /**
     * 读取JSON列表缓存
     *
     * @param key 缓存键
     * @param elementType 列表元素类型
     * @param <T> 列表元素类型
     * @return 缓存列表，不存在或读取失败时返回null
     */
    public <T> List<T> getList(String key, Class<T> elementType) {
        String value = getRaw(key);
        if (value == null) {
            return null;
        }
        try {
            JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, elementType);
            return objectMapper.readValue(value, type);
        } catch (Exception e) {
            log.warn("Redis列表缓存反序列化失败: key={}, errorType={}", key, e.getClass().getSimpleName());
            delete(key);
            return null;
        }
    }

    /**
     * 批量读取JSON缓存
     *
     * @param keys 缓存键列表
     * @param type 目标类型
     * @param <T> 目标类型
     * @return 读取成功的缓存键值映射
     */
    public <T> Map<String, T> multiGet(List<String> keys, Class<T> type) {
        Map<String, T> result = new LinkedHashMap<>();
        if (keys.isEmpty()) {
            return result;
        }
        try {
            List<String> values = stringRedisTemplate.opsForValue().multiGet(keys);
            if (values == null) {
                return result;
            }
            for (int index = 0; index < Math.min(keys.size(), values.size()); index++) {
                String value = values.get(index);
                if (value == null) {
                    continue;
                }
                try {
                    result.put(keys.get(index), objectMapper.readValue(value, type));
                } catch (Exception e) {
                    log.warn("Redis批量缓存反序列化失败: key={}, errorType={}",
                            keys.get(index), e.getClass().getSimpleName());
                    delete(keys.get(index));
                }
            }
        } catch (Exception e) {
            log.warn("Redis批量读取失败: keyCount={}, errorType={}", keys.size(), e.getClass().getSimpleName());
        }
        return result;
    }

    /**
     * 写入单个JSON缓存
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param ttl 缓存时间
     */
    public void put(String key, Object value, Duration ttl) {
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), jitter(ttl));
        } catch (Exception e) {
            log.warn("Redis缓存写入失败: key={}, errorType={}", key, e.getClass().getSimpleName());
        }
    }

    /**
     * 批量写入JSON缓存
     *
     * @param values 缓存键值映射
     * @param ttl 缓存时间
     */
    public void putAll(Map<String, ?> values, Duration ttl) {
        if (values.isEmpty()) {
            return;
        }
        try {
            RedisSerializer<String> serializer = stringRedisTemplate.getStringSerializer();
            long ttlSeconds = Math.max(1L, jitter(ttl).toSeconds());
            Map<byte[], byte[]> serialized = new LinkedHashMap<>();
            for (Map.Entry<String, ?> entry : values.entrySet()) {
                serialized.put(serializer.serialize(entry.getKey()),
                        serializer.serialize(objectMapper.writeValueAsString(entry.getValue())));
            }
            stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                serialized.forEach((key, value) -> connection.stringCommands().setEx(key, ttlSeconds, value));
                return null;
            });
        } catch (Exception e) {
            log.warn("Redis批量缓存写入失败: keyCount={}, errorType={}", values.size(), e.getClass().getSimpleName());
        }
    }

    /**
     * 获取缓存版本号
     *
     * @param key 版本缓存键
     * @return 当前版本号，不存在时返回0
     */
    public long getVersion(String key) {
        String value = getRaw(key);
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            delete(key);
            return 0L;
        }
    }

    /**
     * 递增缓存版本号
     *
     * @param key 版本缓存键
     */
    public void incrementVersion(String key) {
        try {
            stringRedisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.warn("Redis缓存版本更新失败: key={}, errorType={}", key, e.getClass().getSimpleName());
        }
    }

    /**
     * 删除单个缓存
     *
     * @param key 缓存键
     */
    public void delete(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis缓存删除失败: key={}, errorType={}", key, e.getClass().getSimpleName());
        }
    }

    /**
     * 批量删除缓存
     *
     * @param keys 缓存键集合
     */
    public void delete(Collection<String> keys) {
        if (keys.isEmpty()) {
            return;
        }
        try {
            stringRedisTemplate.delete(keys);
        } catch (Exception e) {
            log.warn("Redis批量缓存删除失败: keyCount={}, errorType={}", keys.size(), e.getClass().getSimpleName());
        }
    }

    private String getRaw(String key) {
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis缓存读取失败: key={}, errorType={}", key, e.getClass().getSimpleName());
            return null;
        }
    }

    private Duration jitter(Duration ttl) {
        long millis = Math.max(1L, ttl.toMillis());
        long range = Math.max(1L, Math.round(millis * CACHE_TTL_JITTER_RATIO));
        long delta = ThreadLocalRandom.current().nextLong(-range, range + 1L);
        return Duration.ofMillis(Math.max(1L, millis + delta));
    }
}
