package cn.kastner.oj.kafka.deserializer;

import com.alibaba.fastjson.JSON;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class ObjectDeserializer implements Deserializer<Object> {

  @Override
  public void configure(Map<String, ?> map, boolean b) {
  }

  @Override
  public Object deserialize(String s, byte[] bytes) {
    return JSON.parseObject(bytes, Object.class);
  }

  @Override
  public void close() {
  }
}
