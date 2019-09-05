package cn.kastner.oj.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "sampleio")
@Data
public class SampleIO {

  @Id
  @Column(length = 40)
  private String id;

  @Column(columnDefinition = "TEXT")
  private String input;

  @Column(columnDefinition = "TEXT")
  private String output;

  @ManyToOne
  private Problem problem;

  public SampleIO() {
    this.id = UUID.randomUUID().toString();
  }
}
