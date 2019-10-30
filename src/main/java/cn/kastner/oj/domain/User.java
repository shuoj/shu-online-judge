package cn.kastner.oj.domain;

import cn.kastner.oj.domain.log.AuthLog;
import cn.kastner.oj.domain.security.Authority;
import cn.kastner.oj.domain.security.AuthorityName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user")
@Data
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class User {

  @Id
  @Column(length = 40)
  private String id;

  @Column(unique = true, length = 50)
  @NotBlank(message = "用户名不能为空")
  @Size(min = 4, max = 50)
  private String username;

  @Column(length = 100)
  @NotNull(message = "密码不能为空")
  private String password;

  private String studentNumber;

  @Column(length = 50)
  private String firstname;

  @Column(length = 50)
  private String lastname;

  @Column(length = 50)
  @NotNull(message = "名字不能为空")
  private String name;

  @Column(unique = true, length = 50)
  @NotNull(message = "邮箱不能为空")
  @Email(message = "邮箱格式错误")
  private String email;

  @Column(length = 50)
  @NotNull(message = "学校不能为空")
  private String school;

  @Column
  private String signature;

  @Column(length = 50)
  private Long acCount;

  @Column(length = 50)
  private Long submitCount;

  @Column(length = 50)
  private Double acRate;

  @NotNull
  private Boolean enabled;

  @Temporal(TemporalType.TIMESTAMP)
  @NotNull
  private Date lastPasswordResetDate;

  @ManyToMany(fetch = FetchType.EAGER)
  @Fetch(FetchMode.JOIN)
  @JoinTable(
      name = "user_authority",
      joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "authority_id", referencedColumnName = "id")})
  private List<Authority> authorities;

  @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  private List<Problem> problemList;

  @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  private List<VirtualProblem> virtualProblemList;

  @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  private List<Contest> contestList;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  private List<RankingUser> rankingUserList;

  @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  private List<Announcement> announcementList;

  @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  private List<Group> createGroupList;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  private List<AuthLog> authLogList;

  @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  private List<Clarification> clarificationList;

  @ManyToMany(mappedBy = "userSet", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  private List<Group> groupList;

  @ManyToMany(mappedBy = "userSet", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  private List<Contest> contestListJoined;

  @ManyToMany(mappedBy = "userListExcluded", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  private List<Ranking> rankingList;

  @OneToMany(mappedBy = "user")
  @JsonIgnore
  private List<UserSecurityQuestion> userSecurityQuestionList;

  private Long referUserId;
  private Boolean temporary;

  public User(
      @NotNull @Size(min = 4, max = 50) String username,
      @NotNull String password,
      @NotNull String email,
      String firstname,
      String lastname,
      String school,
      List<Authority> authorities) {
    this.id = UUID.randomUUID().toString();
    this.username = username;
    this.password = password;
    this.email = email;
    this.firstname = firstname;
    this.lastname = lastname;
    this.school = school;
    this.acCount = (long) 0;
    this.submitCount = (long) 0;
    this.acRate = 0.0;
    this.enabled = true;
    this.lastPasswordResetDate = new Date();
    this.authorities = authorities;
  }

  public User(
      @NotNull @Size(min = 4, max = 50) String username,
      @NotNull String password,
      @NotNull String email,
      String school,
      List<Authority> authorities) {
    this.id = UUID.randomUUID().toString();
    this.username = username;
    this.password = password;
    this.email = email;
    this.school = school;
    this.acCount = (long) 0;
    this.submitCount = (long) 0;
    this.acRate = 0.0;
    this.enabled = true;
    this.lastPasswordResetDate = new Date();
    this.authorities = authorities;
  }

  public User(
      @NotNull @Size(min = 4, max = 50) String username,
      @NotNull String password,
      String firstname,
      String lastname,
      String studentNumber,
      @NotNull String email,
      String school,
      List<Authority> authorities) {
    this.id = UUID.randomUUID().toString();
    this.username = username;
    this.password = password;
    this.firstname = firstname;
    this.lastname = lastname;
    this.name = firstname + lastname;
    this.studentNumber = studentNumber;
    this.email = email;
    this.school = school;
    this.acCount = (long) 0;
    this.submitCount = (long) 0;
    this.acRate = 0.0;
    this.enabled = true;
    this.lastPasswordResetDate = new Date();
    this.authorities = authorities;
  }

  public User() {
    this.id = UUID.randomUUID().toString();
    this.acCount = (long) 0;
    this.submitCount = (long) 0;
    this.acRate = 0.0;
    this.enabled = true;
    this.lastPasswordResetDate = new Date();
  }

  public boolean isAdmin() {
    for (Authority authority : authorities) {
      if (AuthorityName.ROLE_ADMIN.equals(authority.getName())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (!(o instanceof User)) {
      return false;
    }

    User user = (User) o;
    return user.id.equals(id);
  }

  @Override
  public int hashCode() {
    int code = 20;
    code = code * 30 + name.hashCode();
    return code;
  }
}
