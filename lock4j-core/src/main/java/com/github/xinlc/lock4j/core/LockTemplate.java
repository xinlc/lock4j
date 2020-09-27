package com.github.xinlc.lock4j.core;

import com.github.xinlc.lock4j.core.annotation.DistributedLockable;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁模板
 *
 * @author Leo Xin
 * @since 1.0.0
 */
public class LockTemplate {

	public final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 锁值随机长度
	 */
	private final int LOCK_VALUE_RANDOM_LEN = 4;

	private LockExecutor lockExecutor;

	public LockTemplate(LockExecutor lockExecutor) {
		this.lockExecutor = lockExecutor;
	}

	/**
	 * 使用 AOP 获取锁
	 *
	 * @param <T>
	 * @param key         lock key
	 * @param handler     处理器
	 * @param expireTime  过期时间
	 * @param autoUnlock  完成时是否自动解锁
	 * @param retries     重试次数
	 * @param waitingTime 重试间隔时间
	 * @param onFailure   获取锁失败时抛出的异常
	 * @return
	 */
	public <T> T tryLock(String key, LockHandler<T> handler, long expireTime, boolean autoUnlock, int retries, long waitingTime,
						 Class<? extends RuntimeException> onFailure) throws Throwable {

		BaseDistributedLock lock = this.acquire(key, expireTime, retries, waitingTime);
		try {
			if (lock != null) {
				logger.debug("get lock success, key: {}", key);
				return handler.handle();
			}
			logger.debug("get lock fail, key: {}", key);
			if (null != onFailure && onFailure != DistributedLockable.NoException.class) {
				// 反射实例
				throw onFailure.newInstance();
			}
		} finally {
			if (autoUnlock && lock != null) {
				lock.releaseLock();
			}
		}
		return null;
	}

	/**
	 * acquire distributed lock
	 *
	 * @param key         lock key
	 * @param expireTime  过期时间
	 * @param retries     重试次数
	 * @param waitingTime 重试间隔时间
	 * @return {@link DistributedLock}
	 * @throws InterruptedException
	 */
	public DistributedLock acquire(String key, long expireTime, int retries, long waitingTime) throws InterruptedException {
		int acquireCount = 0;
		do {
			acquireCount++;
			String lockValue = generateLockValue();

			if (lockExecutor.acquire(key, lockValue, expireTime)) {
				LockInfo lockInfo = new LockInfo(key, lockValue, expireTime, waitingTime, acquireCount);
				return new DistributedLock(lockExecutor, lockInfo);
			}

			// 重试获取锁
			if (retries > NumberUtils.INTEGER_ZERO) {
				// 优化重试策略为订阅 Redis 事件: 订阅 Redis 事件可以进一步优化锁的性能, 可通过 wait + notifyAll 来替代 sleep
				TimeUnit.MILLISECONDS.sleep(waitingTime);
			}
			if (Thread.currentThread().isInterrupted()) {
				break;
			}
		} while (retries-- > NumberUtils.INTEGER_ZERO);

		return null;
	}

	/**
	 * 释放锁
	 *
	 * @param distributedLock
	 * @return
	 */
	public boolean releaseLock(DistributedLock distributedLock) {
		return distributedLock.releaseLock();
	}

	/**
	 * 生成锁对应的值
	 */
	private String generateLockValue() {
		// 锁值要保证唯一, 使用4位随机字符串+时间戳基本可满足需求 注: UUID.randomUUID()在高并发情况下性能不佳.
		return RandomStringUtils.randomAlphanumeric(LOCK_VALUE_RANDOM_LEN) + System.currentTimeMillis();
	}


//	Example：
//	@DistributedLockable(
//			argNames = {"anyObject.id", "anyObject.name", "param1"},
//			expireTime = 20, unit = TimeUnit.SECONDS,
//			onFailure = RuntimeException.class
//	)
//	public Long distributedLockableOnFaiFailure(AnyObject anyObject, String param1, Object param2, Long timeout) {
//		try {
//			TimeUnit.SECONDS.sleep(timeout);
//			logger.info("distributed-lockable: " + System.nanoTime());
//		} catch (InterruptedException e) {
//		}
//		return System.nanoTime();
//	}
}
