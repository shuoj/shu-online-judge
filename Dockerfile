FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG JAR_FILE
ARG APP_VERSION
ENV APP_VERSION $APP_VERSION
ADD ${JAR_FILE} app.jar
RUN echo "$APP_VERSION"
RUN echo "Asia/Shanghai" > /etc/timezone
ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -Dsentry.release=$APP_VERSION -jar /app.jar