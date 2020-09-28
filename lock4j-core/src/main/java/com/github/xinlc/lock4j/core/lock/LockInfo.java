package com.github.xinlc.lock4j.core.lock;

/**
 * 成功获取锁的信息
 *
 * @author Leo Xin
 * @since 1.0.0
 */
public class LockInfo {

	/**
	 * 锁标志对应的key
	 */
	private String lockKey;

	/**
	 * 锁对应的值
	 */
	private String lockValue;

	/**
	 * 过期时间
	 */
	private long expireTime;

	/**
	 * 重试间隔时间
	 */
	private long waitingTime;

	/**
	 * 获取锁次数
	 */
	private int acquireCount;

	public String getLockKey() {
		return lockKey;
	}

	public void setLockKey(String lockKey) {
		this.lockKey = lockKey;
	}

	public String getLockValue() {
		return lockValue;
	}

	public void setLockValue(String lockValue) {
		this.lockValue = lockValue;
	}

	public long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	public long getWaitingTime() {
		return waitingTime;
	}

	public void setWaitingTime(long waitingTime) {
		this.waitingTime = waitingTime;
	}

	public int getAcquireCount() {
		return acquireCount;
	}

	public void setAcquireCount(int acquireCount) {
		this.acquireCount = acquireCount;
	}

	public LockInfo() {
	}

	public LockInfo(String lockKey, String lockValue, long expireTime, long waitingTime, int acquireCount) {
		this.lockKey = lockKey;
		this.lockValue = lockValue;
		this.expireTime = expireTime;
		this.waitingTime = waitingTime;
		this.acquireCount = acquireCount;
	}
}
