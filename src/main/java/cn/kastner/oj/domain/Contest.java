package cn.kastner.oj.domain;

import cn.kastner.oj.domain.enums.ContestStatus;
import cn.kastner.oj.domain.enums.ContestType;
import cn.kastner.oj.domain.enums.JudgeType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "contest")
public class Contest {

  @Id
  @Column(length = 40)
  private String id;

  @Column(updatable = false, unique = true, nullable = false)
  private Long idx;

  @Fetch(FetchMode.JOIN)
  @ManyToOne(fetch = FetchType.EAGER)
  private User author;

  @Column(unique = true, length = 50)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  private String password;

  @Enumerated(EnumType.STRING)
  private ContestType contestType;

  @Enumerated(EnumType.STRING)
  private JudgeType judgeType;

  @OneToOne(cascade = CascadeType.ALL)
  private Ranking ranking;

  private Boolean realTimeRank;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private LocalDateTime createDate;

  @Fetch(FetchMode.SUBSELECT)
  @OneToMany(mappedBy = "contest")
  @JsonIgnore
  private List<Submission> submissionList;

  @OneToMany(mappedBy = "contest")
  @JsonIgnore
  private Set<ContestProblem> contestProblemSet;

  @OneToMany(mappedBy = "contest")
  @JsonIgnore
  private List<Clarification> clarificationList;

  @ManyToMany(fetch = FetchType.EAGER)
  @Fetch(FetchMode.SELECT)
  @JoinTable(
      name = "contest_user",
      joinColumns = {@JoinColumn(name = "contest_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
  private Set<User> userSet;

  private Boolean frozen;
  private Boolean enable;
  private Boolean visible;

  @Enumerated(EnumType.STRING)
  private ContestStatus status;

  private Boolean couldShare;

  public Contest() {
    this.id = UUID.randomUUID().toString();
    this.contestType = ContestType.PUBLIC;
    this.judgeType = JudgeType.IMMEDIATELY;
    this.realTimeRank = true;
    this.contestProblemSet = new HashSet<>();
    this.userSet = new HashSet<>();
    this.visible = false;
    this.enable = false;
    this.status = ContestStatus.NOT_STARTED;
    this.couldShare = true;
  }

  public void setPassword(String password) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    this.password = encoder.encode(password);
  }

  public boolean isRankingFrozen() {
    return LocalDateTime.now().isAfter(this.startDate.plusHours(4));
  }

}
