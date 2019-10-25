package cn.kastner.oj.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class SentryConfiguration {
  @Bean
  public HandlerExceptionResolver sentryExceptionResolver() {
    return new io.sentry.spring.SentryExceptionResolver();
  }
}
