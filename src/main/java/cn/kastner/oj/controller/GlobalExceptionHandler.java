package cn.kastner.oj.controller;

import cn.kastner.oj.exception.AppException;
import cn.kastner.oj.util.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(value = {Exception.class})
  @ResponseBody
  public ErrorResponse generalErrorHandler(HttpServletRequest req, Exception e) {
    logger.error("Uncaught Exception", e);
    ErrorResponse r = new ErrorResponse();
    r.setMessage("系统错误");
    r.setCode(ErrorResponse.ERROR);
    r.setUrl(req.getRequestURL().toString());
    return r;
  }

  @ExceptionHandler(value = {AppException.class})
  public ResponseEntity<ErrorResponse> authorizationHandler(
      HttpServletRequest req, AppException e) {
    ErrorResponse r = new ErrorResponse();
    r.setCode(e.getCode());
    r.setMessage(e.getMessage());
    r.setUrl(req.getRequestURL().toString());
    return ResponseEntity.status(e.getStatus()).body(r);
  }
}
