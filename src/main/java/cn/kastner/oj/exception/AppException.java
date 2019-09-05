package cn.kastner.oj.exception;

import org.springframework.http.HttpStatus;

public class AppException extends Exception {

  Integer code;

  HttpStatus status;

  public AppException(String message) {
    super(message);
    this.code = -1;
    this.status = HttpStatus.INTERNAL_SERVER_ERROR;
  }

  public AppException(String message, Integer code, HttpStatus status) {
    super(message);
    this.code = code;
    this.status = status;
  }

  public Integer getCode() {
    return code;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
