package cn.kastner.oj.exception;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends AppException {

  public static final String NOT_GROUP_OWNER = "不是群组所有者";

  public AuthorizationException(String message) {
    super(message);
    switch (message) {
      case NOT_GROUP_OWNER:
        this.code = -2;
        this.status = HttpStatus.FORBIDDEN;
        break;
      default:
        this.code = -1;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
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
