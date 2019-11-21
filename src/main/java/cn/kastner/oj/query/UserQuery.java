package cn.kastner.oj.query;

import lombok.Data;

import java.util.List;

@Data
public class UserQuery {

  private String id;

  private String username;

  private String name;

  private String studentNumber;

  private Boolean temporary;

  private String school;

  private List<String> role;
}
