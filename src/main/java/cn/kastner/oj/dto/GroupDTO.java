package cn.kastner.oj.dto;

import cn.kastner.oj.security.JwtUser;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupDTO {

  private String id;

  private String idx;

  @NotBlank(message = "组名不能为空")
  private String name;

  private LocalDateTime createDate;

  private List<JwtUser> jwtUserList;
}
