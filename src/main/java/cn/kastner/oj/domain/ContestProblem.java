package cn.kastner.oj.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
@Table(name = "contest_problem")
public class ContestProblem {

  @Id
  @Column(length = 40)
  private String id;

  @ManyToOne
  @JoinColumn(name = "contest_id")
  private Contest contest;

  @ManyToOne
  @JoinColumn(name = "problem_id")
  private Problem problem;

  private Integer acceptCountBefore;

  private Integer submitCountBefore;

  private Double acceptRateBefore;

  private Integer acceptCountAfter;

  private Integer submitCountAfter;

  private Double acceptRateAfter;

  @OneToOne(cascade = CascadeType.ALL)
  private TimeCost timeListAfter;

  @OneToOne
  private Submission firstSubmission;

  public ContestProblem() {
    this.id = UUID.randomUUID().toString();
    this.acceptCountBefore = 0;
    this.submitCountBefore = 0;
    this.acceptRateBefore = 0.0;
    this.acceptCountAfter = 0;
    this.submitCountAfter = 0;
    this.acceptRateAfter = 0.0;
  }

  public String getContestId() {
    return this.contest.getId();
  }

  public String getProblemId() {
    return this.problem.getId();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (!(o instanceof ContestProblem)) {
      return false;
    }

    ContestProblem contestProblem = (ContestProblem) o;
    return contestProblem.getContestId().equals(this.getContestId())
        && contestProblem.getProblemId().equals(this.getProblemId());
  }

  @Override
  public int hashCode() {
    int code = 20;
    code = code * 30 + id.hashCode();
    return code;
  }

  public void addAcceptCountBefore(Integer acceptCount) {
    this.acceptCountBefore += acceptCount;
  }

  public void addSubmitCountBefore(Integer submitCount) {
    this.submitCountBefore += submitCount;
  }

  public void addAcceptCountAfter(Integer acceptCount) {
    this.acceptCountAfter += acceptCount;
  }

  public void addSubmitCountAfter(Integer submitCount) {
    this.submitCountAfter += submitCount;
  }

  public void computeAcceptRateBefore() {
    if (submitCountBefore != 0) {
      acceptRateBefore = (double) acceptCountBefore / submitCountBefore;
    }
  }

  public void computeAcceptRateAfter() {
    if (submitCountAfter != 0) {
      acceptRateAfter = (double) acceptCountAfter / submitCountAfter;
    }
  }
}
