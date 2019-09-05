package cn.kastner.oj.dto;

import lombok.Data;

@Data
public class ChangePasswordDTO {

  private String oldPassword;

  private String newPassword;
}
