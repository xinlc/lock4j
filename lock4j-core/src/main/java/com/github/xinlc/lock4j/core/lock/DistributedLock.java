package com.github.xinlc.lock4j.core.lock;

/**
 * 分布式锁
 *
 * @author leo
 * @since 1.0.0
 */
public class DistributedLock extends BaseDistributedLock {

	/**
	 * 分布式锁执行器
	 */
	private final LockExecutor lockExecutor;

	/**
	 * 分布式锁信息
	 */
	private LockInfo lockInfo;

	public LockInfo getLockInfo() {
		return lockInfo;
	}

	public void setLockInfo(LockInfo lockInfo) {
		this.lockInfo = lockInfo;
	}

	public DistributedLock(LockExecutor lockExecutor, LockInfo lockInfo) {
		this.lockExecutor = lockExecutor;
		this.lockInfo = lockInfo;
	}

	@Override
	public boolean releaseLock() {
		return lockExecutor.releaseLock(lockInfo);
	}
}
