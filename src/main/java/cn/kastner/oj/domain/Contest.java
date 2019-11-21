package cn.kastner.oj.domain;

import cn.kastner.oj.domain.enums.ContestStatus;
import cn.kastner.oj.domain.enums.ContestType;
import cn.kastner.oj.domain.enums.JudgeType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
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
  private String id = UUID.randomUUID().toString();

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
  private ContestType contestType = ContestType.PUBLIC;

  @Enumerated(EnumType.STRING)
  private JudgeType judgeType = JudgeType.IMMEDIATELY;

  private Boolean realTimeRank = true;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private LocalDateTime createDate;

  @Fetch(FetchMode.SUBSELECT)
  @OneToMany(mappedBy = "contest")
  @JsonIgnore
  @NotFound(action = NotFoundAction.IGNORE)
  private List<Submission> submissionList;

  @OneToMany(mappedBy = "contest")
  @JsonIgnore
  private Set<ContestProblem> contestProblemSet = new HashSet<>();

  @OneToMany(mappedBy = "contest")
  @JsonIgnore
  private List<Clarification> clarificationList;

  @OneToMany(
      mappedBy = "contest",
      fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  @Transient
  private Set<RankingUser> rankingUserList = new HashSet<>();

  @Fetch(FetchMode.SUBSELECT)
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "contest_excluded_user",
      joinColumns = {@JoinColumn(name = "contest_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
  @JsonIgnore
  private Set<User> userListExcluded = new HashSet<>();

  private Boolean frozen = false;
  private Boolean enable = false;
  private Boolean visible = false;

  @Enumerated(EnumType.STRING)
  private ContestStatus status = ContestStatus.NOT_STARTED;

  private Boolean couldShare = true;

  public void setPassword(String password) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    this.password = encoder.encode(password);
  }

  public boolean isRankingFrozen() {
    return LocalDateTime.now().isAfter(this.startDate.plusHours(4));
  }

}
