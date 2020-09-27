package com.github.xinlc.lock4j.autoconfigure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 分布式锁配置
 *
 * @author Leo Xin
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "lock4j")
public class Lock4jProperties {

	/**
	 * 是否开启 Controller 层 API 调用分布式锁
	 */
	private Boolean enableLockForController = true;

	/**
	 * Controller Aspect 执行顺序
	 */
	private Integer aspectOrder = 0;

	public Boolean getEnableLockForController() {
		return enableLockForController;
	}

	public void setEnableLockForController(Boolean enableLockForController) {
		this.enableLockForController = enableLockForController;
	}

	public Integer getAspectOrder() {
		return aspectOrder;
	}

	public void setAspectOrder(Integer aspectOrder) {
		this.aspectOrder = aspectOrder;
	}
}
