package cn.kastner.oj.exception;

import org.springframework.http.HttpStatus;

public class RecommendException extends AppException {

  public static final String BAD_OPTION = "推荐参数错误";

  public RecommendException(String message) {
    super(message);
    switch (message) {
      case BAD_OPTION:
        this.code = -2;
        this.status = HttpStatus.BAD_REQUEST;
        break;
      default:
        this.code = -1;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }
}
