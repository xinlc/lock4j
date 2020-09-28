package com.github.xinlc.lock4j.core.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 分布式锁抽象类
 * <p>
 * 实现 AutoCloseable 接口, 可使用 try-with-resource 方便地完成自动解锁
 *
 * @author Leo Xin
 * @since 1.0.0
 */
public abstract class BaseDistributedLock implements AutoCloseable {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 是否锁
	 *
	 * @return 成功失败
	 */
	public abstract boolean releaseLock();

	/**
	 * @see AutoCloseable#close()
	 */
	@Override
	public void close() throws Exception {

		logger.debug("distributed lock close , {}", this.toString());

		this.releaseLock();
	}
}
