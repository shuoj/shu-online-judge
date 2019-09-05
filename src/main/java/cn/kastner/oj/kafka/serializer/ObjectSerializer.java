package cn.kastner.oj.kafka.serializer;

import com.alibaba.fastjson.JSON;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class ObjectSerializer implements Serializer<Object> {

  @Override
  public void configure(Map<String, ?> map, boolean b) {
  }

  @Override
  public byte[] serialize(String s, Object o) {
    return JSON.toJSONBytes(o);
  }

  @Override
  public void close() {
  }
}
