package cn.kastner.oj.aspect;

import cn.kastner.oj.dto.AuthLogDTO;
import cn.kastner.oj.kafka.Producer;
import cn.kastner.oj.util.CommonUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuthAspect {

  private final String topic;
  private final Producer producer;

  @Autowired
  public AuthAspect(Producer producer, @Value("${kafka.topic.auth}") String topic) {
    this.producer = producer;
    this.topic = topic;
  }

  @AfterReturning(
      pointcut =
          "execution(* cn.kastner.oj.service.AuthenticationService.login(..))",
      returning = "returning")
  private void authCounter(JoinPoint jp, String returning) {
    if (!CommonUtil.isNull(returning)) {
      producer.send(topic, "login", new AuthLogDTO(jp.getArgs()[0].toString()));
    }
  }
}
