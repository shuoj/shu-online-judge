package cn.kastner.oj.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageDTO<T> {

  private Integer currentPage;

  private Integer currentSize;

  private Long total;

  private List<T> list;

  public PageDTO(Integer currentPage, Integer currentSize, Long total, List<T> list) {
    this.currentPage = currentPage;
    this.currentSize = currentSize;
    this.total = total;
    this.list = list;
  }
}
