
# release all
mvn clean deploy -Dmaven.test.skip=true -P release

# exclude projects
# mvn clean deploy -Dmaven.test.skip=true -P release -pl "!lock4j-core,!lock4j-spring-boot-autoconfigure,!lock4j-spring-boot-starter"

# release projects
# mvn clean deploy -Dmaven.test.skip=true -P release -projects lock4j-core,lock4j-spring-boot-autoconfigure,lock4j-spring-boot-starter
