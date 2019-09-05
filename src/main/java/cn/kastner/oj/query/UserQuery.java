package cn.kastner.oj.query;

import lombok.Data;

@Data
public class UserQuery {

  private String id;

  private String username;

  private String name;

  private String studentNumber;

  private Boolean temporary;

  private String school;
}
