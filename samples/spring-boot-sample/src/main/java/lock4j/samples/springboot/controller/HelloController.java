package lock4j.samples.springboot.controller;

import com.github.xinlc.lock4j.core.annotation.DistributedLockable;
import lock4j.samples.springboot.exception.HelloLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author Leo Xin
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("hello")
public class HelloController {

	@DistributedLockable(
			prefix = "hello",
			argNames = {"name"},
//			argNames = {"name", "obj.xxx"},
			expireTime = 10,
			unit = TimeUnit.SECONDS,
			onFailure = HelloLockException.class
	)
	@GetMapping
	public void hello(@RequestParam("name") String name) throws InterruptedException {

		log.info("hello {}", name);
		TimeUnit.SECONDS.sleep(5L);
	}
}
