package cn.kastner.oj.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class RecommendOptionDTO {
  private Integer difficultDegree;
  @NotNull(message = "题目总数不能为空")
  private Integer count;
  private Integer duration;
  private Integer submitInterval;
  private List<String> tagIdsInclude;
  private List<String> tagIdsExclude;
  @NotNull(message = "参数人员列表不能为空")
  private List<String> userIdList;

  public Integer getDifficultDegree() {
    if (difficultDegree == null) {
      return 5;
    }
    return difficultDegree;
  }

  public Integer getSubmitInterval() {
    if (submitInterval == null) {
      return 30;
    }
    return submitInterval;
  }

}
