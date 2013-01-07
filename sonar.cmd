mvn clean install sonar:sonar -P sonar,integration-tests -Dsonar.jacoco.itReportPath=target/jacoco-it.exec
@pause