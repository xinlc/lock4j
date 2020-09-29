# lock4j

Distributed lock implementation using Redis and Zookeeper.(基于 Redis 和 Zookeeper 分布式锁实现)

## Quick Start

Below is a simple demo. Talk is cheap. Show me the code.

### 1. Add Dependency

If your're using Maven, just add the following dependency in `pom.xml`.

```xml
<!-- replace here with the latest version -->
<dependency>
    <groupId>com.github.xinlc</groupId>
    <artifactId>lock4j-spring-boot-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```

If not, you can download JAR in [Maven Center Repository](https://mvnrepository.com/artifact/com.github.xinlc/lock4j-core).

### 2. Define Controller

```java
@DistributedLockable(
        prefix = "hello",
        argNames = {"name"},
        expireTime = 10,
        unit = TimeUnit.SECONDS,
        onFailure = HelloLockException.class
)
@GetMapping
public void hello(@RequestParam("name") String name) {
    log.info("hello {}", name);
}
```

> More [examples](https://github.com/xinlc/lock4j/tree/master/samples).

## TODO

- [ ] Zookeeper implementation.
