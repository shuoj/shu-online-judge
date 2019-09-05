package cn.kastner.oj.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@Data
public class VirtualProblemDTO extends ProblemDTO {

  private String id;

  @NotBlank(message = "源页面不能为空")
  private String sourcePage;
}
