package cn.kastner.oj.domain;

import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "submission")
public class Submission {

  @Id
  @Column(length = 40)
  private String id;

  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long idx;

  @Fetch(FetchMode.JOIN)
  @ManyToOne(fetch = FetchType.EAGER)
  private User author;

  @Fetch(FetchMode.JOIN)
  @ManyToOne(fetch = FetchType.EAGER)
  private Problem problem;

  @Column(columnDefinition = "TEXT")
  private String code;

  @Fetch(FetchMode.JOIN)
  @ManyToOne(fetch = FetchType.EAGER)
  private Contest contest;

  private Boolean isPractice;

  private LocalDateTime createDate;

  @Enumerated(value = EnumType.STRING)
  private Language language;

  private Integer memory;

  private Integer length;

  private Integer duration;

  private Boolean isBeforeFrozen;

  private Boolean shared;

  @Enumerated(EnumType.STRING)
  private Result result;

  @Column(columnDefinition = "TEXT")
  private String resultDetail;

  public Submission() {
    this.id = UUID.randomUUID().toString();
    this.createDate = LocalDateTime.now();
    this.shared = false;
  }

  public String getAuthorId() {
    return author.getId();
  }

  public String getContestId() {
    return contest.getId();
  }

  public String getProblemId() {
    return problem.getId();
  }
}
