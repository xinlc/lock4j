package com.github.xinlc.lock4j.core;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;

/**
 * 分布式锁原生 RedisTemplate 处理器
 *
 * @author Leo Xin
 * @since 1.0.0
 */
public class RedisTemplateLockExecutor implements LockExecutor {
	private static final RedisScript<String> SCRIPT_LOCK = new DefaultRedisScript<>("return redis.call('set',KEYS[1],ARGV[1],'NX','PX',ARGV[2])", String.class);
	private static final RedisScript<String> SCRIPT_UNLOCK = new DefaultRedisScript<>("if redis.call('get',KEYS[1]) == ARGV[1] then return tostring(redis.call('del', KEYS[1])==1) else return 'false' end", String.class);
	private static final String LOCK_SUCCESS = "OK";

	private RedisTemplate<String, Object> redisTemplate;

	public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public boolean acquire(String lockKey, String lockValue, long acquireExpire) {
		Object lockResult = redisTemplate.execute(SCRIPT_LOCK,
				redisTemplate.getStringSerializer(),
				redisTemplate.getStringSerializer(),
				Collections.singletonList(lockKey),
				lockValue, String.valueOf(acquireExpire));
		return LOCK_SUCCESS.equals(lockResult);
	}


	@Override
	public boolean releaseLock(LockInfo lockInfo) {
		Object releaseResult = redisTemplate.execute(SCRIPT_UNLOCK,
				redisTemplate.getStringSerializer(),
				redisTemplate.getStringSerializer(),
				Collections.singletonList(lockInfo.getLockKey()),
				lockInfo.getLockValue());
		return Boolean.valueOf(releaseResult.toString());
	}
}
