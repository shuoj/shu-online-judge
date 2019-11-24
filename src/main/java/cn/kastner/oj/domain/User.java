package cn.kastner.oj.domain;

import cn.kastner.oj.domain.log.AuthLog;
import cn.kastner.oj.domain.security.Authority;
import cn.kastner.oj.domain.security.AuthorityName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.hash.Hashing;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
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
  private String id = UUID.randomUUID().toString();

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
  private Long acCount = 0L;

  @Column(length = 50)
  private Long submitCount = 0L;

  @Column(length = 50)
  private Double acRate = 0.0;

  @NotNull
  private Boolean enabled = true;

  @Temporal(TemporalType.TIMESTAMP)
  @NotNull
  private Date lastPasswordResetDate = new Date();

  @ManyToMany(fetch = FetchType.EAGER)
  @Fetch(FetchMode.JOIN)
  @JoinTable(
      name = "user_authority",
      joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "authority_id", referencedColumnName = "id")})
  @JsonIgnore
  private List<Authority> authorities = new ArrayList<>();

  @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  @Transient
  private List<Problem> problemList;

  @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  @Transient
  private List<VirtualProblem> virtualProblemList;

  @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  @Transient
  private List<Contest> contestList;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  @Transient
  private List<RankingUser> rankingUserList;

  @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  @Transient
  private List<Announcement> announcementList;

  @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  @Transient
  private List<Group> createGroupList;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  @Transient
  private List<AuthLog> authLogList;

  @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  @Transient
  private List<Clarification> clarificationList;

  @ManyToMany(mappedBy = "userSet", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  @Transient
  private List<Group> groupList;

  @ManyToMany(mappedBy = "userListExcluded", fetch = FetchType.LAZY)
  @Fetch(FetchMode.SUBSELECT)
  @JsonIgnore
  @Transient
  private List<Contest> contestListExcluded;

  @OneToMany(mappedBy = "user")
  @JsonIgnore
  @Transient
  private List<UserSecurityQuestion> userSecurityQuestionList;

  private Long referUserId;
  private Boolean temporary = false;

  public User(
      @NotNull @Size(min = 4, max = 50) String username,
      @NotNull String password,
      @NotNull String email,
      String firstname,
      String lastname,
      String school,
      List<Authority> authorities) {
    this.username = username;
    this.email = email;
    this.firstname = firstname;
    this.lastname = lastname;
    this.name = lastname + firstname;
    this.school = school;
    this.authorities = authorities;

    this.setPassword(password);
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
    this.username = username;
    this.firstname = firstname;
    this.lastname = lastname;
    this.name = lastname + firstname;
    this.studentNumber = studentNumber;
    this.email = email;
    this.school = school;
    this.authorities = authorities;

    this.setMd5Password(password);
  }

  public User() {
  }

  public boolean isAdmin() {
    for (Authority authority : authorities) {
      if (AuthorityName.ROLE_ADMIN.equals(authority.getName())) {
        return true;
      }
    }
    return false;
  }

  public boolean isAdminOrStuff() {
    for (Authority authority : authorities) {
      if (AuthorityName.ROLE_ADMIN.equals(authority.getName())
          || AuthorityName.ROLE_STUFF.equals(authority.getName())) {
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

  public void setPassword(String password) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    this.password = encoder.encode(password);
  }

  public void setMd5Password(String password) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    String md5Password = Hashing.md5().hashBytes(password.getBytes()).toString();
    this.password = encoder.encode(md5Password);
  }
}
