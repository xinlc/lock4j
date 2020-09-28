package com.github.xinlc.lock4j.core.redis;

import com.github.xinlc.lock4j.core.lock.BaseDistributedLock;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 *
 * @author Leo Xin
 * @since 1.0.0
 */
public class RedisDistributedLock extends BaseDistributedLock {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final RedisOperations<String, String> operations;

	private final String FAILURE = "0";

	/**
	 * 默认请求锁的超时时间(ms 毫秒)
	 */
	private final long TIME_OUT = 100;

	/**
	 * 默认锁的有效时间(ms 毫秒)
	 */
	private final long EXPIRE = 60 * 1000;

	/**
	 * 锁值随机长度
	 */
	private final int LOCK_VALUE_RANDOM_LEN = 4;

	/**
	 * 锁标志对应的key
	 */
	private final String lockKey;

	/**
	 * 锁对应的值
	 */
	private String lockValue;

	/**
	 * 锁的有效时间(ms)
	 */
	private long expireTime = EXPIRE;

	/**
	 * 请求锁的超时时间(ms)
	 */
	private long timeOut = TIME_OUT;

	/**
	 * 锁标记
	 */
	private volatile boolean locked = false;

	/**
	 * 解锁 Lua 脚本
	 */
	private static final String UNLOCK_LUA;

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

	private final Random random = new Random();

	/**
	 * 使用默认的锁过期时间和请求锁的超时时间
	 *
	 * @param operations
	 * @param lockKey    锁的key（Redis的Key）
	 */
	public RedisDistributedLock(RedisOperations<String, String> operations, String lockKey) {
		this.operations = operations;
		this.lockKey = lockKey + "_lock";
	}

	/**
	 * 使用默认的请求锁的超时时间，指定锁的过期时间
	 *
	 * @param operations
	 * @param lockKey    锁的key（Redis的Key）
	 * @param expireTime 锁的过期时间(单位：毫秒)
	 */
	public RedisDistributedLock(RedisOperations<String, String> operations, String lockKey, long expireTime) {
		this(operations, lockKey);
		this.expireTime = expireTime;
	}

	/**
	 * 锁的过期时间和请求锁的超时时间都是用指定的值
	 *
	 * @param operations
	 * @param lockKey    锁的key（Redis的Key）
	 * @param expireTime 锁的过期时间(单位：毫秒)
	 * @param timeOut    请求锁的超时时间(单位：毫秒)
	 */
	public RedisDistributedLock(RedisOperations<String, String> operations, String lockKey, long expireTime, long timeOut) {
		this(operations, lockKey, expireTime);
		this.timeOut = timeOut;
	}

	/**
	 * 尝试获取锁 立即返回
	 *
	 * @return 是否成功获得锁
	 */
	public boolean tryLock() {
		lockValue = generateLockValue();
		Boolean result = operations.opsForValue().setIfAbsent(lockKey, lockValue, expireTime, TimeUnit.MILLISECONDS);
		locked = null != result && result;
		return locked;
	}

	/**
	 * 尝试获取锁
	 *
	 * @return 是否成功获得锁
	 */
	public boolean acquire() {
		lockValue = generateLockValue();

		// 请求锁超时时间，纳秒
		long timeout = timeOut * 1000000;

		// 系统当前时间，纳秒
		long beginTime = System.nanoTime();

		while ((System.nanoTime() - beginTime) < timeout) {
			Boolean result = operations.opsForValue().setIfAbsent(lockKey, lockValue, expireTime, TimeUnit.MILLISECONDS);
			if (null != result && result) {
				locked = true;
				// 上锁成功结束请求
				return locked;
			}

			// 每次请求等待一段时间
			seleep(10, 50000);
		}
		return locked;
	}

	/**
	 * 以阻塞方式的获取锁
	 *
	 * @return 是否成功获得锁
	 */
	public boolean lockBlock() {
		lockValue = generateLockValue();
		while (true) {
			Boolean result = operations.opsForValue().setIfAbsent(lockKey, lockValue, expireTime, TimeUnit.MILLISECONDS);
			if (locked = null != result && result) {
				locked = true;
				return locked;
			}

			// 每次请求等待一段时间
			seleep(10, 50000);
		}
	}

	/**
	 * 释放锁
	 */
	@Override
	public boolean releaseLock() {
		List<String> keys = Collections.singletonList(lockKey);
		Object result = operations.execute(new DefaultRedisScript<Object>(UNLOCK_LUA, Object.class), keys, lockValue);
		return null != result && !FAILURE.equals(result);
	}

	/**
	 * 线程等待
	 *
	 * @param millis 毫秒
	 * @param nanos  纳秒
	 */
	private void seleep(long millis, int nanos) {
		try {
			Thread.sleep(millis, random.nextInt(nanos));
		} catch (InterruptedException e) {
			logger.info("获取分布式锁休眠被中断：", e);
		}
	}

	/**
	 * 生成锁对应的值
	 */
	private String generateLockValue() {
		// 锁值要保证唯一, 使用4位随机字符串+时间戳基本可满足需求 注: UUID.randomUUID()在高并发情况下性能不佳.
		return RandomStringUtils.randomAlphanumeric(LOCK_VALUE_RANDOM_LEN) + System.currentTimeMillis();
	}

	@Override
	public String toString() {
		return "RedisDistributedLock [key=" + lockKey + ", value=" + lockValue + "]";
	}

	/**
	 * 获取 value
	 */
	public String getLockValue() {
		return lockValue;
//		return operations.opsForValue().get(lockKey);
	}

	/**
	 * 获取锁状态
	 */
	public boolean isLock() {
		return locked;
	}

	public long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(int expireTime) {
		this.expireTime = expireTime;
	}

	public long getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(long timeOut) {
		this.timeOut = timeOut;
	}
}
