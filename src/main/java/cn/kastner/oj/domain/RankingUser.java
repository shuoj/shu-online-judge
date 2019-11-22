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

@Entity
@Table(name = "ranking_user")
@Data
public class RankingUser {

  @Id
  @Column(length = 40)
  private String id = UUID.randomUUID().toString();

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne
  @JoinColumn(name = "contest_id")
  @NotFound(action = NotFoundAction.IGNORE)
  private Contest contest;

  private Integer passedCount = 0;

  private Integer acceptCount = 0;

  private Integer submitCount = 0;

  private Integer errorCount = 0;

  private Double score = 0.0;

  private Long time = 0L;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "rankingUser")
  @Fetch(FetchMode.SUBSELECT)
  @OrderBy("id DESC ")
  @JsonIgnore
  private List<TimeCost> timeList = new ArrayList<>();

  private Boolean ranked = true;

  private Integer rankingNumber;

  private String teacherId;

  private String groupId;

  public void increasePassedCount() {
    this.passedCount++;
  }

  public void increaseAcceptCount() {
    this.acceptCount++;
  }

  public void decreaseAcceptCount() {
    this.acceptCount--;
  }

  public void decreaseSubmitCount() {
    this.submitCount--;
  }

  public void increaseSubmitCount() {
    this.submitCount++;
  }

  public void increaseErrorCount() {
    this.errorCount++;
  }

  public void addTime(Long milliseconds) {
    this.time += milliseconds;
  }

  public void minusTime(Long milliseconds) {
    this.time -= milliseconds;
  }


  public void addScore(Double score) {
    this.score += score;
  }

  public void minusScore(Double score) {
    this.score -= score;
  }
}

