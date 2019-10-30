package cn.kastner.oj.dto;

import cn.kastner.oj.domain.SampleIO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProblemDTO {

  private String id;

  private Long idx;

  private String authorId;

  private String authorName;

  @NotBlank(message = "题目标题不能为空")
  private String title;

  @NotBlank(message = "题目描述不能为空")
  private String description;

  @NotNull(message = "时间显示不能为空")
  private Integer timeLimit;

  @NotNull(message = "内存限制不能为空")
  private Integer ramLimit;

  @NotNull(message = "难度不能为空")
  private String difficulty;

  private Double degreeOfDifficulty;

  private Boolean visible;

  private List<TagDTO> tagList;

  @NotBlank(message = "输入描述不能为空")
  private String inputDesc;

  @NotBlank(message = "输出描述不能为空")
  private String outputDesc;

  @NotNull(message = "样例不能为空")
  private List<SampleIO> sampleIOList;

  private String sampleIO;

  private Boolean specialJudged;

  @NotBlank(message = "测试数据不能为空")
  private String testData;

  private String hint;

  private String source;

  private Integer averageAcceptTime;

  private Integer acceptCount;

  private Integer submitCount;

  private Double acceptRate;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime createDate;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime lastUsedDate;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime modifiedDate;
}
