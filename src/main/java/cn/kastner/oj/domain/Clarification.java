package cn.kastner.oj.domain;

import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "clarification")
public class Clarification {

  @Id
  @Column(length = 40)
  private String id;

  @Fetch(FetchMode.JOIN)
  @ManyToOne(fetch = FetchType.EAGER)
  private User author;

  @Fetch(FetchMode.JOIN)
  @ManyToOne(fetch = FetchType.EAGER)
  private Contest contest;

  @Column(columnDefinition = "TEXT")
  private String question;

  @Column(columnDefinition = "TEXT")
  private String answer;

  private LocalDateTime createDate;

  private Boolean isPublic;

  private Boolean isRead;

  public Clarification() {
    this.id = UUID.randomUUID().toString();
  }
}
