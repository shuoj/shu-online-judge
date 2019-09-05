package cn.kastner.oj.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SampleIODTO {

  private String id;

  @NotBlank(message = "样例输入不能为空")
  private String input;

  @NotBlank(message = "样例输出不能为空")
  private String output;
}
