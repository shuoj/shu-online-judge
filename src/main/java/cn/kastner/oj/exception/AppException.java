package cn.kastner.oj.exception;

import io.sentry.event.Breadcrumb;
import org.springframework.http.HttpStatus;

public class AppException extends Exception {

  Integer code;

  HttpStatus status;

  Breadcrumb.Level level;

  String category;

  public AppException(String message) {
    super(message);
    this.code = -1;
    this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    this.level = Breadcrumb.Level.INFO;
    this.category = "default";
  }

  public AppException(String message, Integer code, HttpStatus status) {
    super(message);
    this.code = code;
    this.status = status;
    this.level = Breadcrumb.Level.INFO;
    this.category = "default";
  }

  public Integer getCode() {
    return code;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public Breadcrumb.Level getLevel() {
    return level;
  }

  public String getCategory() {
    return category;
  }
}
