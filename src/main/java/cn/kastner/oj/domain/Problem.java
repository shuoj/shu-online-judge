package cn.kastner.oj.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "problem")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Problem {

  @Id
  @Column(length = 40)
  private String id;

  @Column(updatable = false, unique = true, nullable = false)
  private Long idx;

  @Fetch(FetchMode.JOIN)
  @ManyToOne(fetch = FetchType.EAGER)
  private User author;

  @Column(unique = true, length = 50)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  private Integer timeLimit;
  private Integer ramLimit;

  @Enumerated(EnumType.STRING)
  private Difficulty difficulty;

  private Double degreeOfDifficulty;
  private Boolean visible;

  @Fetch(FetchMode.SELECT)
  @ManyToMany(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.MERGE, CascadeType.PERSIST})
  @JoinTable(
      name = "problem_tag",
      joinColumns = {@JoinColumn(name = "problem_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "tag_id", referencedColumnName = "id")})
  private List<Tag> tagList;

  @Column(columnDefinition = "TEXT")
  private String inputDesc;

  @Column(columnDefinition = "TEXT")
  private String outputDesc;

  @Fetch(FetchMode.SELECT)
  @OneToMany(
      mappedBy = "problem",
      fetch = FetchType.EAGER,
      cascade = {CascadeType.ALL})
  private List<SampleIO> sampleIOList;

  @OneToMany(mappedBy = "problem")
  @JsonIgnore
  private List<ContestProblem> contestProblemList;

  @OneToMany(mappedBy = "problem")
  @JsonIgnore
  private List<Submission> submissionList;

  private Boolean specialJudged;

  @Column(columnDefinition = "TEXT")
  private String testData;

  @Column(columnDefinition = "TEXT")
  private String hint;

  @Column(length = 50)
  private String source;

  private Integer averageAcceptTime;
  private Integer acceptCount;
  private Integer submitCount;
  private Double acceptRate;
  private LocalDateTime createDate;
  private LocalDateTime lastUsedDate;
  private LocalDateTime modifiedDate;

  public Problem() {
    this.id = UUID.randomUUID().toString();
    this.visible = true;
    this.tagList = new ArrayList<>();
    this.specialJudged = false;
    this.hint = "";
    this.source = "";
    this.averageAcceptTime = 0;
    this.acceptCount = 0;
    this.submitCount = 0;
    this.acceptRate = 0.0;
    this.createDate = LocalDateTime.now();
    this.lastUsedDate = LocalDateTime.now();
    this.modifiedDate = LocalDateTime.now();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (!(o instanceof Problem)) {
      return false;
    }

    Problem problem = (Problem) o;
    return problem.id.equals(id);
  }

  @Override
  public int hashCode() {
    int code = 20;
    code = code * 30 + title.hashCode();
    return code;
  }
}
