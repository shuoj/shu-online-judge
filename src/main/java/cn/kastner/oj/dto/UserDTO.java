package cn.kastner.oj.dto;

import cn.kastner.oj.domain.security.Authority;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Collection;

@Data
public class UserDTO {
  @NotBlank
  private String username;
  @NotBlank
  private String password;
  @Email
  private String email;
  @NotBlank
  private String firstname;
  @NotBlank
  private String lastname;
  @NotBlank
  private String school;
  @NotEmpty
  private Collection<Authority> authorities;
}
