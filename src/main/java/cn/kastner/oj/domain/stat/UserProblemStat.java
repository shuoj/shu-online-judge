package cn.kastner.oj.domain.stat;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "user_problem_stat")
public class UserProblemStat {

  @Id
  @Column(length = 40)
  private String id;

  private String userId;

  private String problemId;

  private LocalDateTime lastSubmitDate;

  private Boolean passed;

  private Integer score;

  private Integer errorTimes;

  public UserProblemStat() {
    this.id = UUID.randomUUID().toString();
    this.score = 0;
    this.errorTimes = 0;
    this.passed = false;
  }

  public UserProblemStat(String userId, String problemId) {
    this.userId = userId;
    this.problemId = problemId;
    this.id = UUID.randomUUID().toString();
    this.score = 0;
    this.errorTimes = 0;
    this.passed = false;
  }
}
