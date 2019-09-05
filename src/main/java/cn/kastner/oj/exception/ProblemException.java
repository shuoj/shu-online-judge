package cn.kastner.oj.exception;

import org.springframework.http.HttpStatus;

public class ProblemException extends AppException {

  public static final String NO_SUCH_PROBLEM = "没有这个题目"; // code -2

  public static final String TEST_DATA_PATH_INVALID = "测试数据路径无效"; // code -3

  public static final String HAVE_SUCH_TITLE_PROBLEM = "已经有相同标题的题目了"; // code -4

  public static final String DID_NOT_PASS = "该题目未通过";

  public static final String PROBLEM_REFERENCED = "题目被引用不能删除";

  public ProblemException(String message) {
    super(message);
    switch (message) {
      case NO_SUCH_PROBLEM:
        this.code = -2;
        this.status = HttpStatus.NOT_FOUND;
        break;
      case TEST_DATA_PATH_INVALID:
        this.code = -3;
        this.status = HttpStatus.BAD_REQUEST;
        break;
      case HAVE_SUCH_TITLE_PROBLEM:
        this.code = -4;
        this.status = HttpStatus.BAD_REQUEST;
        break;
      case DID_NOT_PASS:
        this.code = -5;
        this.status = HttpStatus.BAD_REQUEST;
        break;
      case PROBLEM_REFERENCED:
        this.code = -6;
        this.status = HttpStatus.UNPROCESSABLE_ENTITY;
        break;
      default:
        this.code = -1;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        break;
    }
  }

  public ProblemException(AppException cause) {
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
