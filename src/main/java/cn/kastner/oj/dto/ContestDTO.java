package cn.kastner.oj.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class ContestDTO {

  private String id;

  private Long idx;

  private String authorId;

  private String authorName;

  private String password;

  @NotBlank(message = "比赛名字不能为空")
  private String name;

  @NotBlank(message = "比赛描述不能为空")
  private String description;

  private String openType;

  private String contestType;

  private String status;

  private String judgeType;

  @NotNull(message = "比赛开始时间不能为空")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime startDate;

  @NotNull(message = "比赛结束时间不能为空")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime endDate;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime createDate;

  private Boolean frozen;

  private Long frozenOffset;

  private Boolean enable;

  private Boolean visible;

  private Boolean sharable;
}
