package cn.kastner.oj.dto;

import cn.kastner.oj.security.JwtUser;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupDTO {

  private String id;

  private String idx;

  private String authorId;

  private String authorName;

  @NotBlank(message = "组名不能为空")
  private String name;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime createDate;

  private List<JwtUser> jwtUserList;
}
