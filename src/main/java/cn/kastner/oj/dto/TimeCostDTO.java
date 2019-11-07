package cn.kastner.oj.dto;

import lombok.Data;

@Data
public class TimeCostDTO {

  private Long totalTime;

  private Integer errorCount;

  private Boolean submitted;

  private Boolean passed;

  private Boolean firstPassed;

  private Double score;
}
