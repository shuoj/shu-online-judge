package cn.kastner.oj.dto;

import lombok.Data;

import java.util.List;

@Data
public class RankingUserDTO {

  private String id;

  private String userId;

  private String userName;

  private Integer acceptCount;

  private Integer submitCount;

  private TimeCostDTO totalTime;

  private List<TimeCostDTO> timeList;

  private Boolean ranked;

  private Long rank;
}
