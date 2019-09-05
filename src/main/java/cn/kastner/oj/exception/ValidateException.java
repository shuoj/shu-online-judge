package cn.kastner.oj.exception;

import org.springframework.http.HttpStatus;

public class ValidateException extends AppException {

  public static final String DUPLICATED_USERNAME = "用户名重复！";

  public static final String DUPLICATED_EMAIL = "邮箱重复！";

  public static final String WRONG_OLD_PASSWORD = "原密码错误！";

  public ValidateException(String message) {
    super(message);
    this.code = -1;
    this.status = HttpStatus.BAD_REQUEST;
  }

  public ValidateException(String message, HttpStatus status) {
    super(message);
    this.code = -1;
    this.status = status;
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
