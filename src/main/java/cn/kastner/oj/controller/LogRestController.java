package cn.kastner.oj.controller;

import cn.kastner.oj.dto.AuthLogDTO;
import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.query.AuthLogQuery;
import cn.kastner.oj.service.LogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/logs")
public class LogRestController {

  private final LogService logService;

  public LogRestController(LogService logService) {
    this.logService = logService;
  }

  @GetMapping(value = "/auth")
  public PageDTO<AuthLogDTO> getAuthLogs(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer size, AuthLogQuery query) {
    return logService.getAuthLogs(query, page, size);
  }
}
