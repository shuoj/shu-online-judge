package cn.kastner.oj.domain.security;

import cn.kastner.oj.domain.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "authority")
@Data
public class Authority {

  @Id
  @Column(length = 40)
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(length = 50)
  @NotNull
  @Enumerated(EnumType.STRING)
  private AuthorityName name;

  @ManyToMany(mappedBy = "authorities")
  @JsonIgnore
  private List<User> userList;
}
