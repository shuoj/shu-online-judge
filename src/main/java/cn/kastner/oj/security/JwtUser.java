package cn.kastner.oj.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;

public class JwtUser implements UserDetails {

  private final String id;

  private final String name;

  private final String username;

  private final String password;

  private final String studentNumber;

  private final String firstname;

  private final String lastname;

  private final String email;

  private final String school;

  private final String signature;

  private final Long acCount;

  private final Long submitCount;

  private final Double acRate;

  private final Boolean enabled;

  private final Date lastPasswordResetDate;

  private final Collection<? extends GrantedAuthority> authorities;

  public JwtUser(
      String id,
      String name,
      String username,
      String password,
      String studentNumber,
      String firstname,
      String lastname,
      String email,
      String school,
      String signature,
      Long acCount,
      Long submitCount,
      Double acRate,
      Boolean enabled,
      Date lastPasswordResetDate,
      Collection<? extends GrantedAuthority> authorities) {
    this.id = id;
    this.name = name;
    this.username = username;
    this.password = password;
    this.studentNumber = studentNumber;
    this.firstname = firstname;
    this.lastname = lastname;
    this.email = email;
    this.school = school;
    this.signature = signature;
    this.acCount = acCount;
    this.submitCount = submitCount;
    this.acRate = acRate;
    this.enabled = enabled;
    this.lastPasswordResetDate = lastPasswordResetDate;
    this.authorities = authorities;
  }

  public String getId() {
    return id;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @JsonIgnore
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @JsonIgnore
  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @JsonIgnore
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @JsonIgnore
  @Override
  public String getPassword() {
    return password;
  }

  public String getFirstname() {
    return firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public String getEmail() {
    return email;
  }

  public String getSchool() {
    return school;
  }

  public String getSignature() {
    return signature;
  }

  public Long getAcCount() {
    return acCount;
  }

  public Long getSubmitCount() {
    return submitCount;
  }

  public Double getAcRate() {
    return acRate;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @JsonIgnore
  public Date getLastPasswordResetDate() {
    return lastPasswordResetDate;
  }

  public String getStudentNumber() {
    return studentNumber;
  }

  public String getName() {
    return name;
  }
}
