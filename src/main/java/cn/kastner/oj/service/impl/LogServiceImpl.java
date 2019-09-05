package cn.kastner.oj.service.impl;

import cn.kastner.oj.domain.User;
import cn.kastner.oj.domain.log.AuthLog;
import cn.kastner.oj.dto.AuthLogDTO;
import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.query.AuthLogQuery;
import cn.kastner.oj.repository.AuthLogRepository;
import cn.kastner.oj.service.LogService;
import cn.kastner.oj.util.CommonUtil;
import cn.kastner.oj.util.DTOMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LogServiceImpl implements LogService {

  private final AuthLogRepository authLogRepository;
  private final DTOMapper mapper;

  public LogServiceImpl(AuthLogRepository authLogRepository, DTOMapper mapper) {
    this.authLogRepository = authLogRepository;
    this.mapper = mapper;
  }


  @Override
  public PageDTO<AuthLogDTO> getAuthLogs(AuthLogQuery query, Integer page, Integer size) {
    Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "timestamp");
    Specification<AuthLog> specification =
        (root, criteriaQuery, criteriaBuilder) -> {
          List<Predicate> predicateList = new ArrayList<>();

          String username = query.getUsername();
          if (!CommonUtil.isNull(username)) {
            Join<AuthLog, User> authLogUserJoin = root.join("user", JoinType.LEFT);
            predicateList.add(
                criteriaBuilder.like(
                    authLogUserJoin.get("username").as(String.class), "%" + username + "%"));
          }

          String name = query.getName();
          if (!CommonUtil.isNull(name)) {
            Join<AuthLog, User> authLogUserJoin = root.join("user", JoinType.LEFT);
            predicateList.add(
                criteriaBuilder.like(
                    authLogUserJoin.get("name").as(String.class), "%" + name + "%"));
          }

          Predicate[] p = new Predicate[predicateList.size()];
          return criteriaBuilder.and(predicateList.toArray(p));
        };

    List<AuthLog> authLogList = authLogRepository.findAll(specification, pageable).getContent();
    long count = authLogRepository.count(specification);
    return new PageDTO<>(page, size, count, mapper.toAuthLogDTOs(authLogList));
  }
}
