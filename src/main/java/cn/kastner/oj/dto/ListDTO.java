package cn.kastner.oj.dto;

import lombok.Data;

import java.util.List;

@Data
public class ListDTO<T> {

  private List<T> list;
}
