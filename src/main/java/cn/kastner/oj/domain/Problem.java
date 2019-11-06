package cn.kastner.oj.domain;

import cn.kastner.oj.domain.enums.Difficulty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Table(name = "problem")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Problem {

  @Id
  @Column(length = 40)
  private String id = UUID.randomUUID().toString();

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
  private Boolean visible = true;

  @Fetch(FetchMode.SELECT)
  @ManyToMany(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.MERGE, CascadeType.PERSIST})
  @JoinTable(
      name = "problem_tag",
      joinColumns = {@JoinColumn(name = "problem_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "tag_id", referencedColumnName = "id")})
  private Set<Tag> tagList = new HashSet<>();

  @Column(columnDefinition = "TEXT")
  private String inputDesc;

  @Column(columnDefinition = "TEXT")
  private String outputDesc;

  private String sampleIO;

  @OneToMany(mappedBy = "problem")
  @JsonIgnore
  private List<ContestProblem> contestProblemList;

  @OneToMany(mappedBy = "problem")
  @JsonIgnore
  private List<Submission> submissionList;

  private Boolean specialJudged = false;

  @Column(columnDefinition = "TEXT")
  private String testData;

  @Column(columnDefinition = "TEXT")
  private String hint = "";

  @Column(length = 50)
  private String source = "";

  private Integer averageAcceptTime = 0;
  private Integer acceptCount = 0;
  private Integer submitCount = 0;
  private Double acceptRate = 0.0;
  private LocalDateTime createDate = LocalDateTime.now();
  private LocalDateTime lastUsedDate = LocalDateTime.now();
  private LocalDateTime modifiedDate = LocalDateTime.now();
  private Integer testCaseCount = 0;

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
