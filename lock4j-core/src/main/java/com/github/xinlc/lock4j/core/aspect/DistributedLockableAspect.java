package com.github.xinlc.lock4j.core.aspect;

import com.github.xinlc.lock4j.core.lock.LockTemplate;
import com.github.xinlc.lock4j.core.annotation.DistributedLockable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

/**
 * 分布式锁 AOP
 *
 * @author Leo Xin
 * @since 1.0.0
 */
@Aspect
public class DistributedLockableAspect extends BaseDistributedLockableAspect implements Ordered {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private int order;

	public DistributedLockableAspect(LockTemplate lockTemplate) {
		super(lockTemplate);
	}

	/**
	 * {@link DistributedLockable}
	 */
//	@Pointcut("(@within(org.springframework.stereotype.Controller)" +
//			"|| @within(org.springframework.web.bind.annotation.RestController))" +
//			"&& execution(public * com.log4j..*.controller..*.*(..)) " +
//			"&& @annotation(com.github.xinlc.lock4j.core.annotation.DistributedLockable)")

	@Pointcut("@annotation(com.github.xinlc.lock4j.core.annotation.DistributedLockable)")
	@Override
	public void distributedLockable() {
	}

	/**
	 * @param joinPoint
	 * @param lockable
	 * @return
	 * @throws Throwable
	 */
	@Around(value = "distributedLockable() && @annotation(lockable)")
	@Override
	public Object around(ProceedingJoinPoint joinPoint, DistributedLockable lockable) throws Throwable {

		long start = System.nanoTime();

		Object result = super.around(joinPoint, lockable);

		long end = System.nanoTime();
		logger.debug("distributed lockable cost: {} ns", end - start);

		return result;
	}

	@Override
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
}
