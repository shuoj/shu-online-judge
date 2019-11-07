package cn.kastner.oj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OjApplication {

  public static void main(String[] args) {
    SpringApplication.run(OjApplication.class, args);
  }
}
