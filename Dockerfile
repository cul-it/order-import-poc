FROM maven:3.6.3-openjdk-15-slim as maven

COPY ./pom.xml ./pom.xml
RUN mvn dependency:go-offline -B

COPY ./src ./src
RUN mvn package -DskipTests=true

FROM jetty:10

WORKDIR $JETTY_BASE

COPY --from=maven target/order-import-poc-*.war ./webapps/order-import-poc.war
