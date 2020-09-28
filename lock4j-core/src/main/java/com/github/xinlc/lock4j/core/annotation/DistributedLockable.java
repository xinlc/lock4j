package com.github.xinlc.lock4j.core.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 * <p>
 * 实现思路：
 * 1. 使用 KeyGenerator 来生成 redis key, value 为随机串
 * 2. 使用 RedisLockClient 来尝试获取锁, 并使用 try-with-resource 完成自动释放锁
 * 3. RedisDistributedLock 释放锁，Lua 保证原子操作
 *
 * @author Leo Xin
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DistributedLockable {

	/**
	 * key 过期时间，防止死锁，默认 3000 毫秒
	 */
	long expireTime() default 3000L;

	/**
	 * 时间单位, 默认毫秒
	 */
	TimeUnit unit() default TimeUnit.MILLISECONDS;

	/**
	 * 获取锁重试次数
	 */
	int retries() default 0;

	/**
	 * 重试间隔时间
	 */
	long waitingTime() default 0L;

	/**
	 * key 前缀
	 * 如果不指定，则默认用包名+方法名，详见 KeyGenerator
	 */
	String prefix() default "";

	/**
	 * 组成 key 的参数，SpEL表达式
	 */
	String[] argNames() default {};

	/**
	 * 是否要用参数生成 key
	 * 如：id=1
	 */
	boolean argsAssociated() default true;

	/**
	 * 完成时是否自动解锁
	 */
	boolean autoUnlock() default true;

	/**
	 * 获取锁失败时抛出的异常
	 */
	Class<? extends RuntimeException> onFailure() default NoException.class;

	/**
	 * no exception
	 */
	final class NoException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}
}
