package cn.kastner.oj.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserGenerationParam {
  @NotNull(message = "群组 id 不能为空")
  private String groupId;
  private Long quantity;
  private String fileToken;
}
