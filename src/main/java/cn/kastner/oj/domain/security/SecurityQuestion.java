package cn.kastner.oj.domain.security;

import cn.kastner.oj.domain.UserSecurityQuestion;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "security_question")
public class SecurityQuestion {

  @Id
  @Column(length = 40)
  private String id;

  @OneToMany(mappedBy = "securityQuestion")
  @JsonIgnore
  private List<UserSecurityQuestion> userSecurityQuestionList;

  private String question;

  public SecurityQuestion() {
    this.id = UUID.randomUUID().toString();
  }
}
