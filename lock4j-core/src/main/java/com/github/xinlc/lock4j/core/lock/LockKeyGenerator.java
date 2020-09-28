package com.github.xinlc.lock4j.core.lock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.xinlc.lock4j.core.utils.StringUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * 分布式锁 key 生成器
 * <p>
 * KeyGenerator 为自定义的 key 生成策略, 参数 key 为 SpEL表达式
 * 如果 prefix 不为空 则使用 prefix + argName + arg 作为 key
 * 否则使用包名 + 方法名 + argName + arg 作为 key 如：
 * com.lock4j.controller.OrderInfoController:getOrderList:id=5
 *
 * @author Leo Xin
 * @since 1.0.0
 */
public interface LockKeyGenerator {

	/**
	 * 表达式解析器，用来解析 argNames 的参数值
	 */
	ExpressionParser PARSER = new SpelExpressionParser();
	ThreadLocal<EvaluationContext> THREAD_LOCAL = ThreadLocal.withInitial(StandardEvaluationContext::new);

	/**
	 * key 前缀
	 */
	String LOCK4J_PREFIX = "lock4j:";

	/**
	 * 默认生成器
	 *
	 * @param joinPoint
	 * @param prefix
	 * @param argNames
	 * @param argsAssociated
	 * @return
	 * @throws JsonProcessingException
	 */
	default StringBuilder generate(ProceedingJoinPoint joinPoint, String prefix, String[] argNames, boolean argsAssociated) throws JsonProcessingException {

		// 获取方法前签名，参数
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Object[] args = joinPoint.getArgs();
		StringBuilder builder = new StringBuilder();

		// 如果没指定前缀则使用包名+方法名作为前缀
		if (StringUtil.isBlank(prefix)) {
			builder.append(LOCK4J_PREFIX)
					.append(joinPoint.getTarget().getClass().getName())
					.append(":")
					.append(signature.getName());
		} else {
			builder.append(prefix);
		}

		// 方法参数加入key中
		String[] parameterNames = signature.getParameterNames();
		if (!argsAssociated || parameterNames.length <= 0) {
			return builder;
		}

		String[] names;
		Object[] values;

		// argsNames 为空时默认为全部参数
		if (null == argNames || argNames.length <= 0) {
			names = parameterNames;
			values = args;
		} else {
			// 对应参数名和值
			Map<String, Object> argMap = new HashMap<>(parameterNames.length);
			for (int index = 0; index < parameterNames.length; index++) {
				argMap.put(parameterNames[index], args[index]);
			}

			names = new String[argNames.length];
			values = new Object[argNames.length];

			for (int index = 0; index < argNames.length; index++) {
				// 分割 anyObject.id 为 anyObject和id
				String[] expression = StringUtil.split(argNames[index], '.');
				names[index] = expression[expression.length - 1];
				String argName = expression[0];
				Object arg = argMap.get(argName);

				// 不是对象参数
				if (null == arg || expression.length == 1) {
					values[index] = arg;
					continue;
				}

				// 获取参数对应的对象值
				EvaluationContext context = THREAD_LOCAL.get();
				context.setVariable(argName, arg);
				values[index] = PARSER.parseExpression("#" + argNames[index]).getValue(context);
			}

			THREAD_LOCAL.remove();
		}

		// 拼接参数 key
		return builder.append(":")
				.append(StringUtil.simpleJoinToBuilder(names, values, "=", "|"));
	}
}
