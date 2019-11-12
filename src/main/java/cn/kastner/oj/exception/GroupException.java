package cn.kastner.oj.exception;

import org.springframework.http.HttpStatus;

public class GroupException extends AppException {

  public static final String NO_SUCH_GROUP = "没有该群组";
  public static final String HAVE_SUCH_GROUP = "该群组名已存在";
  public static final String HAS_BEEN_GENERATED = "已经批量生成过了";

  public GroupException(String message) {
    super(message);
    switch (message) {
      case NO_SUCH_GROUP:
        this.code = -2;
        this.status = HttpStatus.BAD_REQUEST;
        break;
      case HAVE_SUCH_GROUP:
        this.code = -3;
        this.status = HttpStatus.BAD_REQUEST;
        break;
      case HAS_BEEN_GENERATED:
        this.code = -4;
        this.status = HttpStatus.BAD_REQUEST;
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

