package com.github.xinlc.lock4j.autoconfigure.configuration;

import com.github.xinlc.lock4j.autoconfigure.properties.Lock4jProperties;
import com.github.xinlc.lock4j.core.aspect.BaseDistributedLockableAspect;
import com.github.xinlc.lock4j.core.lock.LockExecutor;
import com.github.xinlc.lock4j.core.lock.LockTemplate;
import com.github.xinlc.lock4j.core.redis.RedisTemplateLockExecutor;
import com.github.xinlc.lock4j.core.aspect.DistributedLockableAspect;
import com.github.xinlc.lock4j.core.redis.RedisDistributedLockService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 分布式锁自动配置器
 *
 * @author Leo Xin
 * @since 1.0.0
 */
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(Lock4jProperties.class)
public class Lock4jAutoConfiguration {

	private final Lock4jProperties properties;

	public Lock4jAutoConfiguration(Lock4jProperties properties) {
		this.properties = properties;
	}

	@Bean
	@ConditionalOnMissingBean(LockExecutor.class)
	@ConditionalOnBean(RedisTemplate.class)
	public LockExecutor lockExecutor(RedisTemplate<Object, Object> redisTemplate) {
		RedisTemplateLockExecutor redisTemplateLockExecutor = new RedisTemplateLockExecutor();
		redisTemplateLockExecutor.setRedisTemplate(redisTemplate);
		return redisTemplateLockExecutor;
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(RedisTemplate.class)
	public RedisDistributedLockService redisDistributedLockService(RedisTemplate<Object, Object> redisTemplate) {
		return new RedisDistributedLockService(redisTemplate);
	}

	@Bean
	@ConditionalOnMissingBean
	public LockTemplate lockTemplate(LockExecutor lockExecutor) {
		return new LockTemplate(lockExecutor);
	}

	@Bean
	@ConditionalOnMissingBean(BaseDistributedLockableAspect.class)
	@ConditionalOnProperty(name = "lock4j.enable-lock-for-controller", havingValue = "true", matchIfMissing = true)
	public DistributedLockableAspect distributedLockableAspect(LockTemplate lockTemplate) {
		DistributedLockableAspect distributedLockableAspect = new DistributedLockableAspect(lockTemplate);
		distributedLockableAspect.setOrder(properties.getAspectOrder());
		return distributedLockableAspect;
	}

}
