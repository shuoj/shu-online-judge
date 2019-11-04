package cn.kastner.oj.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "time_cost")
@Data
public class TimeCost {

  @Id
  @Column(length = 40)
  private String id = UUID.randomUUID().toString();

  @OneToOne
  private ContestProblem contestProblem;

  private Long totalTime = 0L;

  private Integer errorCount = 0;

  private Boolean submitted = false;

  private Boolean passed = false;

  private Boolean firstPassed = false;

  private Boolean frozen = false;

  private Double score = 0.0;

  @ManyToOne
  private RankingUser rankingUser;

  public void addTotalTime(Long milliseconds) {
    this.totalTime += milliseconds;
  }

  public void addErrorCount(Integer count) {
    this.errorCount += count;
  }

  public void addScore(Double score) {
    this.score += score;
  }
}
