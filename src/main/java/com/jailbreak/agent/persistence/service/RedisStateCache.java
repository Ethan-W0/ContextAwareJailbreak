package com.jailbreak.agent.persistence.service;

import com.jailbreak.agent.model.AttackState;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisStateCache {

    private static final String STATE_KEY_PREFIX = "attack:state:";
    private static final String EMITTER_KEY_PREFIX = "attack:emitter:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final int stateTtlMinutes;

    public RedisStateCache(RedisTemplate<String, Object> attackStateRedisTemplate,
                           com.jailbreak.agent.config.RedisConfig redisConfig) {
        this.redisTemplate = attackStateRedisTemplate;
        this.stateTtlMinutes = redisConfig.getStateTtlMinutes();
    }

    public void saveState(String taskId, AttackState state) {
        redisTemplate.opsForValue().set(
                STATE_KEY_PREFIX + taskId, state, stateTtlMinutes, TimeUnit.MINUTES);
    }

    public AttackState getState(String taskId) {
        Object obj = redisTemplate.opsForValue().get(STATE_KEY_PREFIX + taskId);
        if (obj instanceof AttackState) return (AttackState) obj;
        return null;
    }

    public void deleteState(String taskId) {
        redisTemplate.delete(STATE_KEY_PREFIX + taskId);
    }

    public void registerEmitter(String taskId, String emitterId) {
        redisTemplate.opsForSet().add(EMITTER_KEY_PREFIX + taskId, emitterId);
        redisTemplate.expire(EMITTER_KEY_PREFIX + taskId, stateTtlMinutes, TimeUnit.MINUTES);
    }

    public void unregisterEmitter(String taskId) {
        redisTemplate.delete(EMITTER_KEY_PREFIX + taskId);
    }

    public boolean isRedisAvailable() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
