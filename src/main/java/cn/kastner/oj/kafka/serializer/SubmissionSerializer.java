package cn.kastner.oj.kafka.serializer;

import cn.kastner.oj.dto.SubmissionDTO;
import com.alibaba.fastjson.JSON;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class SubmissionSerializer implements Serializer<SubmissionDTO> {
  @Override
  public void configure(Map<String, ?> map, boolean b) {

  }

  @Override
  public byte[] serialize(String s, SubmissionDTO submissionDTO) {
    return JSON.toJSONBytes(submissionDTO);
  }

  @Override
  public void close() {

  }
}
