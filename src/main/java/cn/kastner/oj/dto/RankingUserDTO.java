package cn.kastner.oj.dto;

import lombok.Data;

import java.util.Map;

@Data
public class RankingUserDTO {

  private String id;

  private String userId;

  private String userName;

  private Integer acceptCount;

  private Integer submitCount;

  private Integer passedCount;

  private Integer errorCount;

  private Long time;

  private Double score;

  private Map<String, TimeCostDTO> timeList;

  private Boolean ranked;

  private Integer rankingNumber;

  private String teacherId;

  private String groupId;
}
