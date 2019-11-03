package cn.kastner.oj.domain;

import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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
  private String id;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne
  @JoinColumn(name = "ranking_id")
  private Ranking ranking;

  private Integer acceptCountBefore;

  private Integer submitCountBefore;

  @OneToOne(cascade = {CascadeType.ALL})
  private TimeCost totalTimeBefore;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Fetch(FetchMode.SUBSELECT)
  @JoinTable(name = "time_cost_before")
  @OrderBy("id DESC ")
  private List<TimeCost> timeListBefore;

  private Integer acceptCountAfter;

  private Integer submitCountAfter;

  @OneToOne(cascade = {CascadeType.ALL})
  private TimeCost totalTimeAfter;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Fetch(FetchMode.SUBSELECT)
  @JoinTable(name = "time_cost_after")
  @OrderBy("id DESC ")
  private List<TimeCost> timeListAfter;

  private Boolean ranked;
  private Long rankingNumber;

  public RankingUser() {
    this.id = UUID.randomUUID().toString();
    this.acceptCountBefore = 0;
    this.submitCountBefore = 0;
    this.totalTimeBefore = new TimeCost();
    this.timeListBefore = new ArrayList<>();
    this.acceptCountAfter = 0;
    this.submitCountAfter = 0;
    this.totalTimeAfter = new TimeCost();
    this.timeListAfter = new ArrayList<>();
    this.ranked = true;
  }
}
