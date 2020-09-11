# Getting Started

### Activate maven wrapper
Set java version

export JAVA_HOME=java14/jdk-14.0.2

Set maven opts for setting http proxy

export MAVEN_OPTS="-Dhttp.proxyHost=proxy.private.fio.cz -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.private.fio.cz -Dhttps.proxyPort=8080"

download the wrapper and build and install application

./mvnw clean install


### Start application
./mvnw spring-boot:run

or

 mvn spring-boot:run

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.3.3.RELEASE/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.3.3.RELEASE/maven-plugin/reference/html/#build-image)

