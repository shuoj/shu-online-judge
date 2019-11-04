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
  private String id;

  @OneToOne
  private ContestProblem contestProblem;

  private Long totalTime;

  private Integer errorCount;

  private Boolean submitted;

  private Boolean passed;

  private Boolean firstPassed;

  private Boolean frozen;

  @ManyToOne
  private RankingUser rankingUser;

  public TimeCost() {
    this.id = UUID.randomUUID().toString();
    this.totalTime = (long) 0;
    this.errorCount = 0;
    this.submitted = false;
    this.passed = false;
    this.firstPassed = false;
      this.frozen = false;
  }

  public void addTotalTime(Long milliseconds) {
    this.totalTime += milliseconds;
  }

  public void addErrorCount(Integer count) {
    this.errorCount += count;
  }
}
