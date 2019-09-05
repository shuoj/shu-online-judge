package cn.kastner.oj.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuthLogDTO {
  private String id;

  private String userId;

  private String username;

  private String name;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime timestamp;

  public AuthLogDTO(String userId) {
    this.userId = userId;
    this.timestamp = LocalDateTime.now();
  }

  public AuthLogDTO() {
  }
}
