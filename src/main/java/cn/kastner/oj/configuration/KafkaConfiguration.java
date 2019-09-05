package cn.kastner.oj.configuration;

import cn.kastner.oj.dto.AuthLogDTO;
import cn.kastner.oj.dto.SubmissionDTO;
import cn.kastner.oj.kafka.deserializer.AuthLogDeserializer;
import cn.kastner.oj.kafka.deserializer.SubmissionDeserializer;
import cn.kastner.oj.kafka.serializer.ObjectSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfiguration {
  @Value("${kafka.producer.bootstrapServers}")
  private String producerBootstrapServers; // 生产者连接Server地址

  @Value("${kafka.producer.retries}")
  private String producerRetries; // 生产者重试次数

  @Value("${kafka.producer.batchSize}")
  private String producerBatchSize;

  @Value("${kafka.producer.lingerMs}")
  private String producerLingerMs;

  @Value("${kafka.producer.bufferMemory}")
  private String producerBufferMemory;

  @Value("${kafka.consumer.bootstrapServers}")
  private String consumerBootstrapServers;

  @Value("${kafka.consumer.groupId}")
  private String consumerGroupId;

  @Value("${kafka.consumer.enableAutoCommit}")
  private String consumerEnableAutoCommit;

  @Value("${kafka.consumer.autoCommitIntervalMs}")
  private String consumerAutoCommitIntervalMs;

  @Value("${kafka.consumer.sessionTimeoutMs}")
  private String consumerSessionTimeoutMs;

  @Value("${kafka.consumer.maxPollRecords}")
  private String consumerMaxPollRecords;

  @Value("${kafka.consumer.autoOffsetReset}")
  private String consumerAutoOffsetReset;

  @Bean
  public ProducerFactory<Object, Object> producerFactory() {
    Map<String, Object> configs = new HashMap<>(); // 参数
    configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, producerBootstrapServers);
    configs.put(ProducerConfig.RETRIES_CONFIG, producerRetries);
    configs.put(ProducerConfig.BATCH_SIZE_CONFIG, producerBatchSize);
    configs.put(ProducerConfig.LINGER_MS_CONFIG, producerLingerMs);
    configs.put(ProducerConfig.BUFFER_MEMORY_CONFIG, producerBufferMemory);
    configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ObjectSerializer.class);
    return new DefaultKafkaProducerFactory<>(configs);
  }

  @Bean
  public KafkaTemplate<Object, Object> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory(), true);
  }

  @Bean
  @Primary
  public ConsumerFactory<Object, Object> submissionConsumerFactory() {
    Map<String, Object> props = consumerConfigs();
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SubmissionDeserializer.class);
    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  @Primary
  KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, SubmissionDTO>>
  kafkaListenerSubmissionContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, SubmissionDTO> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(submissionConsumerFactory());
    factory.setConcurrency(3);
    factory.getContainerProperties().setPollTimeout(3000);
    factory.getContainerProperties()
        .setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    return factory;
  }

  @Bean
  public ConsumerFactory<Object, Object> authLogConsumerFactory() {
    Map<String, Object> props = consumerConfigs();
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, AuthLogDeserializer.class);
    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, AuthLogDTO>>
  kafkaListenerAuthLogContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, AuthLogDTO> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(authLogConsumerFactory());
    factory.setConcurrency(3);
    factory.getContainerProperties().setPollTimeout(3000);
    factory.getContainerProperties()
        .setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    return factory;
  }

  Map<String, Object> consumerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerBootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, consumerEnableAutoCommit);
//    configs.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, consumerAutoCommitIntervalMs);
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, consumerSessionTimeoutMs);
    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, consumerMaxPollRecords); // 批量消费数量
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerAutoOffsetReset);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    return props;
  }
}
