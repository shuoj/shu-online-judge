package cn.kastner.oj.exception;

import org.springframework.http.HttpStatus;

public class JudgeException extends AppException {

  public static final String POST_ERROR = "发送到判题机失败";

  public JudgeException(String message) {
    super(message);
    switch (message) {
      case POST_ERROR:
        code = -2;
        status = HttpStatus.INTERNAL_SERVER_ERROR;
        break;
      default:
        code = -1;
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
