package cn.kastner.oj.domain;

import lombok.Data;

@Data
public class JudgeResult {
  private Integer cpuTime;
  private Result result;
  private Integer memory;
  private Integer realTime;
  private String message;
}
