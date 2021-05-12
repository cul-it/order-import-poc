FROM maven:3.6.3-openjdk-15-slim as maven

# Some env vars are needed during build to run tests
ARG USE_SYSTEM_ENV=true
ENV USE_SYSTEM_ENV $USE_SYSTEM_ENV
ARG buildDir=.
ENV buildDir $buildDir
ARG baseOkapEndpoint
ENV baseOkapEndpoint $baseOkapEndpoint
ARG okapi_username
ENV okapi_username $okapi_username
ARG okapi_password
ENV okapi_password $okapi_password
ARG tenant
ENV tenant $tenant

COPY ./pom.xml ./pom.xml
COPY ./src ./src
COPY ./marc-test-files ./marc-test-files

RUN mvn dependency:go-offline -B
RUN mvn package

FROM jetty

WORKDIR $JETTY_BASE

COPY --from=maven target/order-import-poc-*.war ./webapps/order-import-poc.war
