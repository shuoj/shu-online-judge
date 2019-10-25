package cn.kastner.oj.controller;

import cn.kastner.oj.exception.*;
import cn.kastner.oj.util.ErrorResponse;
import io.sentry.Sentry;
import io.sentry.event.Breadcrumb;
import io.sentry.event.BreadcrumbBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

  @Value("${app.version}")
  private String version;

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
    Sentry.getContext().addTag("app-version", version);
    Sentry.getContext().addExtra("message", e.getMessage());
    ErrorResponse r = new ErrorResponse();
    r.setCode(e.getCode());
    r.setMessage(e.getMessage());
    r.setUrl(req.getRequestURL().toString());
    return ResponseEntity.status(e.getStatus()).body(r);
  }
}
