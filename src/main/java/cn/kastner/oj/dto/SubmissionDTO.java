package cn.kastner.oj.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SubmissionDTO implements Serializable {

  private static final Long serialVersionUID = -1L;

  private String id;

  private Long idx;

  private String authorId;

  private String authorName;

  private String problemId;

  private String problemTitle;

  private String contestId;

  @NotNull(message = "源代码不能为空")
  private String code;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime createDate;

  @NotNull(message = "语言类型不能为空")
  private String language;

  private Integer duration;

  private String result;

  private Boolean shared;
}
