package cn.kastner.oj.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tag")
@Data
public class Tag {

  @Id
  @Column(length = 40)
  private String id;

  @Column(unique = true, length = 50)
  private String name;

  private Long problemCount;

  @ManyToMany(mappedBy = "tagList", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  private List<Problem> problemList;

  public Tag() {
    this.id = UUID.randomUUID().toString();
    this.problemCount = 0L;
    this.problemList = new ArrayList<>();
  }
}
