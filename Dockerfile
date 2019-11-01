FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG JAR_FILE
ENV APP_VERSION "backend@$TRAVIS_TAG"
ADD ${JAR_FILE} app.jar
RUN echo "Asia/Shanghai" > /etc/timezone
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-Dsentry.release=${APP_VERSION}","-jar","/app.jar"]