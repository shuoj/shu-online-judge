package cn.kastner.oj.exception;

import org.springframework.http.HttpStatus;

public class SecurityQuestionException extends AppException {

  public static final String NO_SUCH_QUESTION = "没有这个问题";
  public static final String HAVE_SET_QUESTION = "已经设置过密保问题";
  public static final String NOT_ENOUGH_QUESTION = "密保问题数量不足";
  public static final String ENCRYPT_EXCEPTION = "内部加密错误";

  public SecurityQuestionException(String message) {
    super(message);
    switch (message) {
      case NO_SUCH_QUESTION:
        this.code = -2;
        this.status = HttpStatus.BAD_REQUEST;
        break;
      case HAVE_SET_QUESTION:
        this.code = -3;
        this.status = HttpStatus.BAD_REQUEST;
        break;
      case NOT_ENOUGH_QUESTION:
        this.code = -4;
        this.status = HttpStatus.BAD_REQUEST;
        break;
      case ENCRYPT_EXCEPTION:
        this.code = -5;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
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
