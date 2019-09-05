package cn.kastner.oj.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class VirtualProblem extends Problem {

  @Id
  private String id;

  @NotNull(message = "出题人不能为空")
  @ManyToOne
  private User author;

  @NotNull(message = "源页面不能为空")
  private String sourcePage;

  public VirtualProblem() {
    this.id = UUID.randomUUID().toString();
  }
}
