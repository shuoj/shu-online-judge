package cn.kastner.oj.exception;

import org.springframework.http.HttpStatus;

public class AnnouncementException extends AppException {

  public AnnouncementException(String message) {
    super(message);
    switch (message) {
      default:
        code = -1;
        status = HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }
}
