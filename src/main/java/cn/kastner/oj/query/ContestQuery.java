package cn.kastner.oj.query;

import cn.kastner.oj.domain.enums.ContestStatus;
import cn.kastner.oj.domain.enums.OpenType;
import lombok.Data;

@Data
public class ContestQuery {

  String name;

  ContestStatus status;

  OpenType type;
}
