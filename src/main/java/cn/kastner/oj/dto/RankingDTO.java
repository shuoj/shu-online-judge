package cn.kastner.oj.dto;

import lombok.Data;

import java.util.List;

@Data
public class RankingDTO {

  private String id;

  private String contestId;

  private String contestName;

  private List<RankingUserDTO> rankingUserList;
}
