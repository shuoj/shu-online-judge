package cn.kastner.oj.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "contest_problem")
public class ContestProblem {

  @Id
  @Column(length = 40)
  private String id = UUID.randomUUID().toString();

  @ManyToOne
  @JoinColumn(name = "contest_id")
  private Contest contest;

  @ManyToOne
  @JoinColumn(name = "problem_id")
  private Problem problem;

  private String label;

  private Integer acceptCount = 0;

  private Integer submitCount = 0;

  private Double acceptRate = 0.0;

  private Integer score = 0;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "contestProblem")
  @Fetch(FetchMode.SUBSELECT)
  @OrderBy("id DESC ")
  @JsonIgnore
  @Transient
  private List<TimeCost> timeList = new ArrayList<>();

  @OneToOne(fetch = FetchType.LAZY)
  @NotFound(action = NotFoundAction.IGNORE)
  private Submission firstSubmission;

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

  public void increaseAcceptCount() {
    this.acceptCount++;
  }

  public void decreaseAcceptCount() {
    this.acceptCount--;
  }

  public void increaseSubmitCount() {
    this.submitCount++;
  }

  public void computeAcceptRate() {
    if (submitCount != 0) {
      acceptRate = (double) acceptCount / submitCount;
    }
  }
}
