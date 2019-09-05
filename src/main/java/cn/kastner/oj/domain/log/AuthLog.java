package cn.kastner.oj.domain.log;

import cn.kastner.oj.domain.User;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "auth_log")
public class AuthLog {

  @Id
  @Column(length = 40)
  private String id;

  @Fetch(FetchMode.JOIN)
  @ManyToOne(fetch = FetchType.EAGER)
  private User user;

  private LocalDateTime timestamp;

  public AuthLog() {
  }

  public AuthLog(User user) {
    this.user = user;
    this.id = UUID.randomUUID().toString();
    this.timestamp = LocalDateTime.now();
  }
}
