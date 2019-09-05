package cn.kastner.oj.controller;

import cn.kastner.oj.exception.*;
import cn.kastner.oj.util.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(
      value = {
          NoSuchItemException.class,
          ValidateException.class,
          HaveSuchItemException.class,
          NoSuchItemException.class,
          AuthorizationException.class,
          ItemReferencedException.class,
          RequestException.class
      })
  @ResponseBody
  public ErrorResponse generalErrorHandler(HttpServletRequest req, Exception e) {
    ErrorResponse r = new ErrorResponse();
    r.setMessage(e.getMessage());
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
