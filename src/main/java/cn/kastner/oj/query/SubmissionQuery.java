package cn.kastner.oj.query;

import cn.kastner.oj.domain.enums.Language;
import lombok.Data;

@Data
public class SubmissionQuery {

  private String username;

  private String problemId;

  private Language language;

  private Boolean isPractice;
}
