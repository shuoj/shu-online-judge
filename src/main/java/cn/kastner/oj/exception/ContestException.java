package cn.kastner.oj.exception;

import org.springframework.http.HttpStatus;

public class ContestException extends AppException {

  public static final String CONTEST_NOT_GOING = "比赛没有进行"; // code -2

  public static final String NO_SUCH_CONTEST = "没有这个比赛"; // code -3

  public static final String CANNOT_JOIN = "比赛不可加入"; // code -4

  public static final String NOT_PASS_CONTEST_USER = "需要输入密码"; // code -5

  public static final String NOT_PUBLIC_CONTEST_USER = "需要加入比赛"; // code -6

  public static final String NO_PASS_PROVIDED = "私密赛（可加入）没有提供加入密码"; // code -7

  public static final String HAVE_SAME_NAME_CONTEST = "已有同名的比赛"; // code -8

  public static final String CANNOT_SHARING = "比赛设置为不可分享";

  public ContestException(String message) {
    super(message);
    switch (message) {
      case NO_SUCH_CONTEST:
        this.code = -2;
        this.status = HttpStatus.NOT_FOUND;
        break;
      case CONTEST_NOT_GOING:
        this.code = -3;
        this.status = HttpStatus.BAD_REQUEST;
        break;
      case CANNOT_JOIN:
        this.code = -4;
        this.status = HttpStatus.BAD_REQUEST;
        break;
      case NOT_PASS_CONTEST_USER:
        this.code = -5;
        this.status = HttpStatus.BAD_REQUEST;
        break;
      case NOT_PUBLIC_CONTEST_USER:
        this.code = -6;
        this.status = HttpStatus.BAD_REQUEST;
        break;
      case NO_PASS_PROVIDED:
        this.code = -7;
        this.status = HttpStatus.BAD_REQUEST;
        break;
      case HAVE_SAME_NAME_CONTEST:
        this.code = -8;
        this.status = HttpStatus.BAD_REQUEST;
        break;
      case CANNOT_SHARING:
        this.code = -9;
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
