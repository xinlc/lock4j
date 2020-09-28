package com.github.xinlc.lock4j.core.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁服务
 *
 * @author Leo Xin
 * @since 1.0.0
 */
public class RedisDistributedLockService {

	private final RedisTemplate<Object, Object> redisTemplate;

	public RedisDistributedLockService(RedisTemplate<Object, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

//	private static final Long RELEASE_SUCCESS = 1L;
//	private static final String LOCK_SUCCESS = "OK";
//	private static final String SET_IF_NOT_EXIST = "NX";
//	private static final String SET_WITH_EXPIRE_TIME = "EX";
//	private static final String RELEASE_LOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

	/** 解锁 Lua 脚本 */
	public static final String UNLOCK_LUA;

	static {
		// 拼接 Lua 代码，保证释放锁的原子性
		StringBuilder sb = new StringBuilder();
		sb.append("if redis.call(\"get\",KEYS[1]) == ARGV[1] ");
		sb.append("then ");
		sb.append("    return redis.call(\"del\",KEYS[1]) ");
		sb.append("else ");
		sb.append("    return 0 ");
		sb.append("end ");
		UNLOCK_LUA = sb.toString();
	}

	/**
	 * 尝试获取锁
	 *
	 * @param key
	 * @param expire
	 * @return
	 */
	public boolean tryLock(final String key, final long expire) {
		String requestId = UUID.randomUUID().toString();
		Boolean result = redisTemplate.opsForValue().setIfAbsent(key, requestId, expire, TimeUnit.MILLISECONDS);
		return null != result && result;
	}

	/**
	 * 尝试获取锁，自定义value
	 *
	 * @param key
	 * @param value
	 * @param expire
	 * @return
	 */
	public boolean tryLockCustom(final String key, final String value, final long expire) {
		Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, expire, TimeUnit.MILLISECONDS);
		return null != result && result;
	}

	/**
	 * 获取 value
	 *
	 * @param key
	 * @return
	 */
	public String get(final String key) {
		return String.valueOf(redisTemplate.opsForValue().get(key));
	}

	/**
	 * 释放锁
	 *
	 * @param key
	 * @param requestId
	 * @return
	 */
	public void releaseLock(final String key, final String requestId) {
		List<Object> keys = Collections.singletonList(key);
		redisTemplate.execute(new DefaultRedisScript<Object>(UNLOCK_LUA, Object.class), keys, requestId);
	}

	public static void main(String[] args) {

//		Example：
//		String key = "lock.userid." + userId;
//		Boolean tryGetLock = redisDistributedLockService.tryLock(key, 5000);
//		String requestId = null;
//		// 没有获取到锁
//		if (!tryGetLock) {
//			throw new BusinessException(xx);
//		} else {
//			requestId = redisDistributedLockService.get(key);
//		}
//		try {
//			// anything……
//		} catch (Exception e) {
//			throw new BusinessException(xx);
//		} finally {
//			redisDistributedLockService.releaseLock(key, requestId);
//		}

	}
}

