package cn.kastner.oj.domain;

import cn.kastner.oj.domain.security.SecurityQuestion;
import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
@Table(name = "user_security_question")
public class UserSecurityQuestion {

  @Id
  @Column(length = 40)
  private String id;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne
  @JoinColumn(name = "security_question_id")
  private SecurityQuestion securityQuestion;

  private String answer;

  public UserSecurityQuestion() {
    this.id = UUID.randomUUID().toString();
  }
}
