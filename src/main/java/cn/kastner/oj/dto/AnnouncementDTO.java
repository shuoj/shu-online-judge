package cn.kastner.oj.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class AnnouncementDTO {

  private String id;

  private String authorId;

  private String authorName;

  @NotBlank(message = "公告内容不能为空")
  private String content;

  @NotBlank(message = "公告标题不能为空")
  private String title;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime modifiedDate;
}
