/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 单词复习Redis队列
 */
package com.btea.lexiflow.learning.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.btea.lexiflow.learning.constant.LearningRedisConstant.REVIEW_QUEUE_KEY_PREFIX;
import static com.btea.lexiflow.learning.constant.LearningRedisConstant.REVIEW_QUEUE_TTL;

/**
 * 单词复习Redis队列。
 */
@Component
@RequiredArgsConstructor
public class LearningReviewQueueCache {

    private static final DefaultRedisScript<Long> MERGE_QUEUE_SCRIPT = new DefaultRedisScript<>("""
            local existing = {}
            local queue = redis.call('LRANGE', KEYS[1], 0, -1)
            for _, entry in ipairs(queue) do
                existing[entry] = true
            end
            local added = 0
            for index = 2, #ARGV do
                if not existing[ARGV[index]] then
                    redis.call('RPUSH', KEYS[1], ARGV[index])
                    existing[ARGV[index]] = true
                    added = added + 1
                end
            end
            if redis.call('LLEN', KEYS[1]) > 0 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            return added
            """, Long.class);

    private static final DefaultRedisScript<Long> ADVANCE_QUEUE_SCRIPT = new DefaultRedisScript<>("""
            local head = redis.call('LINDEX', KEYS[1], 0)
            if not head then
                return 0
            end
            if head ~= ARGV[1] then
                return -1
            end
            redis.call('LPOP', KEYS[1])
            if ARGV[2] == '1' then
                redis.call('RPUSH', KEYS[1], ARGV[1])
            end
            if redis.call('LLEN', KEYS[1]) > 0 then
                redis.call('EXPIRE', KEYS[1], ARGV[3])
            else
                redis.call('DEL', KEYS[1])
            end
            return 1
            """, Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 查询当前用户复习队列，并续期其有效时间。
     *
     * @param userId 用户ID
     * @return 按复习顺序排列的词汇标识
     */
    public List<String> getQueue(String userId) {
        String key = queueKey(userId);
        List<String> queue = stringRedisTemplate.opsForList().range(key, 0, -1);
        if (queue == null || queue.isEmpty()) {
            return List.of();
        }
        stringRedisTemplate.expire(key, REVIEW_QUEUE_TTL);
        return List.copyOf(queue);
    }

    /**
     * 将尚未进入当前复习队列的待复习词追加到队尾
     *
     * @param userId 用户ID
     * @param entries 待复习词汇标识
     * @return 合并后的复习队列
     */
    public List<String> mergeMissing(String userId, List<String> entries) {
        String key = queueKey(userId);
        String[] arguments = new String[entries.size() + 1];
        arguments[0] = String.valueOf(REVIEW_QUEUE_TTL.toSeconds());
        for (int index = 0; index < entries.size(); index++) {
            arguments[index + 1] = entries.get(index);
        }
        stringRedisTemplate.execute(MERGE_QUEUE_SCRIPT, List.of(key), arguments);
        return getQueue(userId);
    }

    /**
     * 校验并推进队首单词；不认识或模糊时重新放入队尾。
     *
     * @param userId 用户ID
     * @param entry 当前词汇标识
     * @param requeue 是否重新加入队尾
     * @return 是否成功推进队列
     */
    public boolean advanceHead(String userId, String entry, boolean requeue) {
        Long result = stringRedisTemplate.execute(ADVANCE_QUEUE_SCRIPT,
                List.of(queueKey(userId)),
                entry,
                requeue ? "1" : "0",
                String.valueOf(REVIEW_QUEUE_TTL.toSeconds()));
        return Long.valueOf(1L).equals(result);
    }

    /**
     * 生成当前用户复习队列缓存键。
     *
     * @param userId 用户ID
     * @return Redis缓存键
     */
    private String queueKey(String userId) {
        return REVIEW_QUEUE_KEY_PREFIX + userId;
    }
}
