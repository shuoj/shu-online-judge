package cn.kastner.oj.exception;

import org.springframework.http.HttpStatus;

public class SubmissionException extends AppException {

  public static final String NO_SUCH_SUBMISSION = "没有这个提交";

  public static final String NOT_AUTHOR = "您不是这个提交的作者，权限不足";

  public static final String RANKING_FROZEN = "不能查看封榜之后的提交";

  public SubmissionException(String message) {
    super(message);
    switch (message) {
      case NO_SUCH_SUBMISSION:
        this.code = -2;
        this.status = HttpStatus.NOT_FOUND;
        break;
      case NOT_AUTHOR:
        this.code = -3;
        this.status = HttpStatus.FORBIDDEN;
        break;
      default:
        this.code = -1;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }

  public SubmissionException(AppException cause) {
    super(cause.getMessage());
    code = cause.getCode();
    status = cause.getStatus();
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
