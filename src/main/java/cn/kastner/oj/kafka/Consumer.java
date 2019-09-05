package cn.kastner.oj.kafka;

import cn.kastner.oj.dto.AuthLogDTO;
import cn.kastner.oj.dto.SubmissionDTO;
import cn.kastner.oj.exception.ProblemException;
import cn.kastner.oj.exception.UserException;
import cn.kastner.oj.service.AuthenticationService;
import cn.kastner.oj.service.SubmissionService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class Consumer {

  private final SubmissionService submissionService;

  private final AuthenticationService authenticationService;

  @Autowired
  public Consumer(SubmissionService submissionService, AuthenticationService authenticationService) {
    this.submissionService = submissionService;
    this.authenticationService = authenticationService;
  }

  @KafkaListener(
      topics = {"#{'${kafka.topic.submission}'}"},
      groupId = "default",
      containerFactory = "kafkaListenerSubmissionContainerFactory"
  )
  public void submissionListener(ConsumerRecord<String, SubmissionDTO> cr, Acknowledgment ack) {
    try {
      submissionService.counter(cr.value());
      ack.acknowledge();
    } catch (ProblemException | UserException e) {
      e.printStackTrace();
    }
  }


  @KafkaListener(
      topics = {"#{'${kafka.topic.auth}'}"},
      groupId = "default",
      containerFactory = "kafkaListenerAuthLogContainerFactory"
  )
  public void authListener(ConsumerRecord<String, AuthLogDTO> cr, Acknowledgment ack) {
    authenticationService.counter(cr.value());
    ack.acknowledge();
  }
}
