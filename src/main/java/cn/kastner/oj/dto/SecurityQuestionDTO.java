package cn.kastner.oj.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SecurityQuestionDTO {

  @NotNull(message = "问题 id 不能为空！")
  private String id;

  private String question;

  @NotNull(message = "问题答案不能为空！")
  @NotBlank(message = "问题答案不能为空！")
  private String answer;
}
