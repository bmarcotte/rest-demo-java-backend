# NOTE: This dockerfile requires Docker 17.05 or higher,
#       as it implements a multi-stage build.
# For more info, see:
#   https://docs.docker.com/engine/userguide/eng-image/multistage-build/

FROM maven:3.5.2-jdk-8 AS blogapi_builder
RUN [ "mkdir", "-p", "/usr/src/app" ]
WORKDIR /usr/src/app
COPY pom.xml /usr/src/app/
COPY src     /usr/src/app/src
RUN [ "mvn", "clean", "install" ]

FROM tomcat:8.5.27-jre8 AS blogapi_deploy
WORKDIR "${CATALINA_HOME}"
RUN rm -rf ${CATALINA_HOME}/webapps/ROOT
COPY --from=blogapi_builder /usr/src/app/target/ROOT.war ${CATALINA_HOME}/webapps/
