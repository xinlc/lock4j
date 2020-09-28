package com.github.xinlc.lock4j.core.lock;

/**
 * 分布式锁 AOP 处理器
 *
 * @author Leo Xin
 * @since 1.0.0
 */
@FunctionalInterface
public interface LockHandler<T> {

	/**
	 * aop proceed
	 *
	 * @return
	 * @throws Throwable
	 */
	T handle() throws Throwable;
}
