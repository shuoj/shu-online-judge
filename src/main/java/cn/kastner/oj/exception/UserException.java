package cn.kastner.oj.exception;

import org.springframework.http.HttpStatus;

public class UserException extends AppException {
  public static final String DUPLICATED_USERNAME = "用户名重复";
  public static final String DUPLICATED_EMAIL = "邮箱重复";
  public static final String NO_SUCH_USER = "没有这个用户！";

  public UserException(String message) {
    super(message);
    switch (message) {
      case DUPLICATED_USERNAME:
        code = -2;
        status = HttpStatus.BAD_REQUEST;
        break;
      case DUPLICATED_EMAIL:
        code = -3;
        status = HttpStatus.BAD_REQUEST;
        break;
      case NO_SUCH_USER:
        code = -4;
        status = HttpStatus.NOT_FOUND;
        break;
      default:
        this.code = -1;
        status = HttpStatus.INTERNAL_SERVER_ERROR;
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
