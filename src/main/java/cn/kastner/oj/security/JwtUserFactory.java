package cn.kastner.oj.security;

import cn.kastner.oj.domain.User;
import cn.kastner.oj.domain.security.Authority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public final class JwtUserFactory {

  private JwtUserFactory() { // ?
  }

  public static JwtUser create(User user) {
    return new JwtUser(
        user.getId(),
        user.getName(),
        user.getUsername(),
        user.getPassword(),
        user.getStudentNumber(),
        user.getFirstname(),
        user.getLastname(),
        user.getEmail(),
        user.getSchool(),
        user.getSignature(),
        user.getAcCount(),
        user.getSubmitCount(),
        user.getAcRate(),
        user.getEnabled(),
        user.getLastPasswordResetDate(),
        mapToGrantedAuthorities(user.getAuthorities()));
  }

  public static List<JwtUser> createList(Collection<User> userList) {
    List<JwtUser> jwtUserList = new ArrayList<>();
    Iterator<User> userIterator = userList.iterator();
    while (userIterator.hasNext()) {
      User user = userIterator.next();
      jwtUserList.add(
              JwtUserFactory.create(user)
      );
    }
    return jwtUserList;
  }

  private static List<GrantedAuthority> mapToGrantedAuthorities(List<Authority> authorities) {
    return authorities.stream()
        .map(authority -> new SimpleGrantedAuthority(authority.getName().name()))
        .collect(Collectors.toList());
  }
}
