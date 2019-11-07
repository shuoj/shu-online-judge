package cn.kastner.oj.domain;

import lombok.Data;

import java.util.UUID;

@Data
public class Ranking {

  private String id = UUID.randomUUID().toString();
}
