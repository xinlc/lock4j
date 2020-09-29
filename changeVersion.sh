# mvn versions:set -DnewVersion=1.0.1
mvn versions:set -DnewVersion=1.0.1-SNAPSHOT
# mvn versions:set -DnewVersion=1.0.1-RELEASE
# mvn -N versions:update-child-modules
# mvn versions:revert
mvn versions:commit

