package lock4j.samples.springboot.exception;


/**
 * 自定义分布式锁异常
 *
 * @author Leo Xin
 * @since 1.0.0
 */
public class HelloLockException extends RuntimeException {

	public HelloLockException() {
		super("正在处理中");
	}
}
