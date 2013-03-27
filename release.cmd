mvn -B clean release:prepare -P integration-tests
mvn release:perform
@pause