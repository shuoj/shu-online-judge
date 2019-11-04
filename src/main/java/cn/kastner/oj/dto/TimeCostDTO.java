package cn.kastner.oj.dto;

import lombok.Data;

@Data
public class TimeCostDTO {

  private String id;

  private String contestId;

  private String problemId;

  private Long totalTime;

  private Integer errorCount;

  private String userId;

  private String userName;

  private Boolean submitted;

  private Boolean passed;

  private Boolean firstPassed;

  private Double score;
}
