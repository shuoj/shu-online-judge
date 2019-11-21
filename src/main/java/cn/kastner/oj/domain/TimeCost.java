package cn.kastner.oj.domain;

import lombok.Data;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "time_cost")
@Data
public class TimeCost {

  @Id
  @Column(length = 40)
  private String id = UUID.randomUUID().toString();

  @ManyToOne(fetch = FetchType.LAZY)
  @NotFound(action = NotFoundAction.IGNORE)
  private ContestProblem contestProblem;

  private Long totalTime = 0L;

  private Integer errorCount = 0;

  private Boolean submitted = false;

  private Boolean passed = false;

  private Boolean firstPassed = false;

  private Double score = 0.0;

  @ManyToOne(fetch = FetchType.LAZY)
  @NotFound(action = NotFoundAction.IGNORE)
  private RankingUser rankingUser;

  public TimeCost() {
  }

  public TimeCost(ContestProblem contestProblem, RankingUser rankingUser) {
    this.contestProblem = contestProblem;
    this.rankingUser = rankingUser;
  }

  public void addTotalTime(Long milliseconds) {
    this.totalTime += milliseconds;
  }

  public void increaseErrorCount() {
    this.errorCount++;
  }

  public void addScore(Double score) {
    this.score += score;
  }
}
