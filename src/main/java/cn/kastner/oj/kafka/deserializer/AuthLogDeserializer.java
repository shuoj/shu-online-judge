package cn.kastner.oj.kafka.deserializer;

import cn.kastner.oj.dto.AuthLogDTO;
import com.alibaba.fastjson.JSON;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class AuthLogDeserializer implements Deserializer<AuthLogDTO> {
  @Override
  public void configure(Map<String, ?> map, boolean b) {
  }

  @Override
  public AuthLogDTO deserialize(String s, byte[] bytes) {
    return JSON.parseObject(bytes, AuthLogDTO.class);
  }

  @Override
  public void close() {
  }
}
