package cn.kastner.oj.controller;

import cn.kastner.oj.domain.pojos.JudgeServerStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1")
public class JudgeServerRestController {

  private final RedisTemplate redisTemplate;

  @Autowired
  public JudgeServerRestController(RedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @PostMapping(value = "/judge_server_heartbeat")
  public Map<String, String> handleHeartbeat(@RequestBody JudgeServerStatus judgeServerStatus) {
    redisTemplate.opsForValue().set("judge-server:status", judgeServerStatus);
    Map<String, String> map = new HashMap<>();
    map.put("data", "success");
    map.put("error", null);
    return map;
  }

  @GetMapping(value = "/judge-server/status")
  public List<JudgeServerStatus> getStatus() {
    List<JudgeServerStatus> judgeServerStatusList = new ArrayList<>();
    judgeServerStatusList.add((JudgeServerStatus) redisTemplate.opsForValue().get("judge-server:status"));
    return judgeServerStatusList;
  }
}
