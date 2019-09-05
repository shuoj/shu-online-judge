package cn.kastner.oj.domain.security;

import cn.kastner.oj.domain.User;

public class UserContext implements AutoCloseable {

  public static final ThreadLocal<User> current = new ThreadLocal<>();

  public UserContext(User user) {
    current.set(user);
  }

  public static User getCurrentUser() {
    return current.get();
  }

  @Override
  public void close() {
    current.remove();
  }
}
