package cn.kastner.oj.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class Producer {

  private KafkaTemplate<Object, Object> kafkaTemplate;

  @Autowired
  public Producer(KafkaTemplate<Object, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void send(String topic, Object key, Object data) {
    try {
      kafkaTemplate.send(topic, key, data);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
