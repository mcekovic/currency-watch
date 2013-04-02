@set MAVEN_OPTS=-Xmx1024M -XX:MaxPermSize=128M
mvn clean install sonar:sonar -P sonar,integration-tests -Dsonar.jacoco.itReportPath=target/jacoco-it.exec
@pause