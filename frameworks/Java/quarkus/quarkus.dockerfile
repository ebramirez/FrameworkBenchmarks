FROM maven:3.6.1-jdk-11-slim as maven
WORKDIR /quarkus
RUN mkdir -p /root/.m2/repository/io
RUN mkdir -p /root/.m2/repository/org
COPY m2-quarkus /root/.m2/repository/io/quarkus
COPY m2-vertx /root/.m2/repository/io/vertx
COPY m2-hibernate /root/.m2/repository/org/hibernate
COPY pom.xml pom.xml
RUN mvn dependency:go-offline -q
COPY src src
RUN mvn package -q

FROM openjdk:11.0.3-jdk-slim
WORKDIR /quarkus
COPY --from=maven /quarkus/target/lib lib
COPY --from=maven /quarkus/target/benchmark-1.0-SNAPSHOT-runner.jar app.jar
CMD ["java", "-server", "-XX:+UseNUMA", "-XX:+UseParallelGC", "-Djava.lang.Integer.IntegerCache.high=10000", "-Dvertx.disableHttpHeadersValidation=true", "-Dvertx.disableMetrics=true", "-Dvertx.disableH2c=true", "-Dvertx.disableWebsockets=true", "-Dvertx.flashPolicyHandler=false", "-Dvertx.threadChecks=false", "-Dvertx.disableContextTimings=true", "-Dvertx.disableTCCL=true", "-Dhibernate.allow_update_outside_transaction=true", "-jar", "app.jar"]
