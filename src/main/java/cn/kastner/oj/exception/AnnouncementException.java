package cn.kastner.oj.exception;

import org.springframework.http.HttpStatus;

public class AnnouncementException extends AppException {

  public static final String NO_SUCH_ANNOUNCEMENT = "没有这个公告";

  public static final String HAVE_SUCH_ANNOUNCEMENT = "已有相同名字的公告";

  public AnnouncementException(String message) {
    super(message);
    switch (message) {
      case NO_SUCH_ANNOUNCEMENT:
        this.code = -2;
        this.status = HttpStatus.NOT_FOUND;
        break;
      case HAVE_SUCH_ANNOUNCEMENT:
        this.code = -3;
        this.status = HttpStatus.BAD_REQUEST;
        break;
      default:
        code = -1;
        status = HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }
}
