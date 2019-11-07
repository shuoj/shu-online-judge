package cn.kastner.oj.domain.pojos;

import lombok.Data;

@Data
public class JudgeServerStatus {

  private String judger_version;

  private String hostname;

  private Integer running_task_number;

  private Integer cpu_core;

  private Double memory;

  private String action;

  private Integer cpu;

  private String service_url;
}
