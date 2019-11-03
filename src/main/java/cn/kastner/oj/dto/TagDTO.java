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

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (!(o instanceof TagDTO)) {
      return false;
    }

    TagDTO tagDTO = (TagDTO) o;
    return tagDTO.name.equals(name);
  }

  @Override
  public int hashCode() {
    int code = 20;
    code = code * 30 + name.hashCode();
    return code;
  }
}
