package cn.kastner.oj.query;

import cn.kastner.oj.domain.ContestStatus;
import cn.kastner.oj.domain.ContestType;
import lombok.Data;

@Data
public class ContestQuery {

  String name;

  ContestStatus status;

  ContestType type;
}
