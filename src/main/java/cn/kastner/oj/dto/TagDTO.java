package cn.kastner.oj.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TagDTO {

  private String id;

  private Long problemCount;

  @NotBlank(message = "Tag名称不能为空/空格")
  private String name;

  public TagDTO() {
    this.problemCount = 0L;
  }
}
