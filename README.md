
# Online Judge For Shanghai University
![](https://travis-ci.org/shuoj/shu-online-judge.svg?branch=master)

![Docker Cloud Automated build](https://img.shields.io/docker/cloud/automated/kastnerorz/shu-online-judge)
![Docker Cloud Build Status](https://img.shields.io/docker/cloud/build/kastnerorz/shu-online-judge)
![Docker Pulls](https://img.shields.io/docker/pulls/kastnerorz/shu-online-judge)

## API Reference
[Wiki](https://github.com/shuoj/shu-online-judge/wiki)

## Deploy

Please see [shuoj/deploy](https://github.com/shuoj/deploy).

## Development

### Example Properties Files

#### application.properties

```properties
jwt.header=Authorization
jwt.secret=
jwt.expiration=604800
jwt.tokenHead="Bearer "
jwt.route.authentication.path=/api/v1/auth
jwt.route.authentication.refresh=/api/v1/refresh
jwt.route.authentication.register=/api/v1/register
judger.token=
judger.url=http://localhost:12358
upload.path=
spring.datasource.url=http://localhost:3306
spring.datasource.username=root
spring.datasource.password=mysqlpass
spring.jpa.open-in-view=true
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
# Specify the DBMS
spring.jpa.database=MYSQL
spring.jpa.show-sql=true
# Hibernate ddl auto (create, create-drop, update)
spring.jpa.hibernate.ddl-auto=update
# Naming strategy
spring.jpa.hibernate.naming-strategy=org.hibernate.cfg.ImprovedNamingStrategy
kafka.producer.bootstrapServers=localhost:9092
kafka.producer.retries=3
kafka.producer.batchSize=16384
kafka.producer.lingerMs=1
kafka.producer.bufferMemory=33554432
kafka.consumer.bootstrapServers=localhost:9092
kafka.consumer.groupId=0
kafka.consumer.enableAutoCommit=false
kafka.consumer.autoCommitIntervalMs=1000
kafka.consumer.sessionTimeoutMs=30000
kafka.consumer.maxPollRecords=100
kafka.consumer.autoOffsetReset=earliest
kafka.topic.submission=
kafka.topic.auth=
spring.redis.database=0
spring.redis.host=localhost
spring.redis.port=6379 
spring.redis.password=passredis
spring.redis.timeout=300
spring.data.redis.repositories.enabled=false
server.port=8081
spring.mvc.logResolvedException=false
```

#### sentry.properties

```properties
stacktrace.app.packages=cn.kastner.oj
```