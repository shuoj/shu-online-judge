package cn.kastner.oj.dto;

import cn.kastner.oj.domain.security.Authority;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Collection;

@Data
public class UserDTO {
  private String id;

  @NotBlank(message = "用户名不能为空")
  private String username;

  @NotBlank(message = "密码不能为空")
  private String password;

  @Email(message = "邮箱格式不正确")
  private String email;

  @NotBlank(message = "名字不能为空")
  private String firstname;

  @NotBlank(message = "姓氏不能为空")
  private String lastname;

  @NotBlank(message = "学校不能为空 ")
  private String school;

  private String signature;
  private Boolean enabled;
  private Collection<Authority> authorities;
}
