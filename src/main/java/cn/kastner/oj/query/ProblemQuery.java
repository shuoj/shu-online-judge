package cn.kastner.oj.query;

import cn.kastner.oj.domain.Difficulty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProblemQuery {

  private String authorName;

  private String title;

  private String tags;

  private Difficulty difficulty;

  private Double acceptRate;

  private LocalDateTime createDate;

  private Boolean visible;
}
