package cn.kastner.oj.domain;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "idx_sequence")
public class IndexSequence {

  @Id
  @Column(length = 20)
  private String name;
  private Long nextIdx;
}
