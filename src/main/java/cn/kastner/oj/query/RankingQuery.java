package cn.kastner.oj.query;

import lombok.Data;

@Data
public class RankingQuery {
  private String groupId;
  private String teacherId;
  private Boolean realTime;
}
