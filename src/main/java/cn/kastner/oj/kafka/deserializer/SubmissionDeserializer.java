package cn.kastner.oj.kafka.deserializer;

import cn.kastner.oj.dto.SubmissionDTO;
import com.alibaba.fastjson.JSON;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class SubmissionDeserializer implements Deserializer<SubmissionDTO> {
  @Override
  public void configure(Map<String, ?> map, boolean b) {

  }

  @Override
  public SubmissionDTO deserialize(String s, byte[] bytes) {
    return JSON.parseObject(bytes, SubmissionDTO.class);
  }

  @Override
  public void close() {

  }
}
