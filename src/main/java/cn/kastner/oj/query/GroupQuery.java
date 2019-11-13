package cn.kastner.oj.query;

import lombok.Data;

@Data
public class GroupQuery {
  private String name;
  private Long idx;
  private Boolean currentUser;
  private String authorId;
}
