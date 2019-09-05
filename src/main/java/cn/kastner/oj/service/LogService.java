package cn.kastner.oj.service;

import cn.kastner.oj.dto.AuthLogDTO;
import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.query.AuthLogQuery;

public interface LogService {
  PageDTO<AuthLogDTO> getAuthLogs(AuthLogQuery query, Integer page, Integer size);
}
