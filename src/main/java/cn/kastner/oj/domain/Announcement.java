package cn.kastner.oj.domain;

import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "announcement")
public class Announcement {

  @Id
  @Column(length = 40)
  private String id;

  @Fetch(FetchMode.JOIN)
  @ManyToOne(fetch = FetchType.EAGER)
  private User author;

  @Column(length = 50)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  private LocalDateTime modifiedDate;

  public Announcement() {
    this.id = UUID.randomUUID().toString();
    this.modifiedDate = LocalDateTime.now();
  }
}
