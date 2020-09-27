package com.github.xinlc.lock4j.core.aspect;

import com.github.xinlc.lock4j.core.LockKeyGenerator;
import com.github.xinlc.lock4j.core.LockTemplate;
import com.github.xinlc.lock4j.core.annotation.DistributedLockable;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 分布式锁 AOP
 *
 * @author Leo Xin
 * @since 1.0.0
 */
public abstract class BaseDistributedLockableAspect implements LockKeyGenerator {

	private final LockTemplate lockTemplate;

	public BaseDistributedLockableAspect(LockTemplate lockTemplate) {
		this.lockTemplate = lockTemplate;
	}

	/**
	 * 切入点
	 * {@link DistributedLockable}
	 */
	public void distributedLockable() {
	}

	/**
	 * 切面
	 *
	 * @param joinPoint
	 * @param lockable
	 * @return
	 * @throws Throwable
	 */
	public Object around(ProceedingJoinPoint joinPoint, DistributedLockable lockable) throws Throwable {
		// 生成 key
		final String key = this.generate(joinPoint, lockable.prefix(), lockable.argNames(), lockable.argsAssociated()).toString();

		// 获取锁
		return lockTemplate.tryLock(
				key,
				joinPoint::proceed,
				lockable.unit().toMillis(lockable.expireTime()),
				lockable.autoUnlock(),
				lockable.retries(),
				lockable.unit().toMillis(lockable.waitingTime()),
				lockable.onFailure()
		);
	}
}
