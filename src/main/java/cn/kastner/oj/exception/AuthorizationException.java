package cn.kastner.oj.exception;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends AppException {

  public AuthorizationException(String message) {
    super(message);
    switch (message) {
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
