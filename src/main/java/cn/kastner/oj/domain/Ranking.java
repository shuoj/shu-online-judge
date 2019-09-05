package cn.kastner.oj.domain;

import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "ranking")
public class Ranking {

  @Id
  @Column(length = 40)
  private String id;

  @OneToOne
  private Contest contest;

  @OneToMany(
      mappedBy = "ranking",
      fetch = FetchType.LAZY,
      cascade = {CascadeType.ALL})
  @Fetch(FetchMode.SUBSELECT)
  private List<RankingUser> rankingUserList;

  @Fetch(FetchMode.SUBSELECT)
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "ranking_user",
      joinColumns = {@JoinColumn(name = "ranking_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
  private List<User> userListExcluded;

  public Ranking() {
    this.id = UUID.randomUUID().toString();
    this.rankingUserList = new ArrayList<>();
    this.userListExcluded = new ArrayList<>();
  }
}
