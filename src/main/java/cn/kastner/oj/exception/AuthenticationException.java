package cn.kastner.oj.exception;

import io.sentry.event.Breadcrumb;
import org.springframework.http.HttpStatus;

public class AuthenticationException extends AppException {

  public static final String USER_DISABLED = "用户被禁用！";

  public static final String BAD_CREDENTIALS = "用户名或密码错误！";

  public AuthenticationException(String message) {
    super(message);
    this.category = "auth";
    switch (message) {
      case USER_DISABLED:
        this.code = -2;
        this.status = HttpStatus.UNAUTHORIZED;
        this.level = Breadcrumb.Level.DEBUG;
        break;
      case BAD_CREDENTIALS:
        this.code = -3;
        this.status = HttpStatus.UNAUTHORIZED;
        break;
      default:
        this.code = -1;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        break;
    }
  }

  @Override
  public Integer getCode() {
    return code;
  }

  @Override
  public HttpStatus getStatus() {
    return status;
  }
}
