package cn.kastner.oj.controller;

import cn.kastner.oj.dto.TagDTO;
import cn.kastner.oj.repository.TagRepository;
import cn.kastner.oj.util.CommonUtil;
import cn.kastner.oj.util.DTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1")
public class TagRestController {

  private final TagRepository tagRepository;

  private final DTOMapper mapper;

  @Autowired
  public TagRestController(TagRepository tagRepository, DTOMapper mapper) {
    this.tagRepository = tagRepository;
    this.mapper = mapper;
  }

  @GetMapping(value = "/tags")
  private List<TagDTO> getTags(String name) {
    if (!CommonUtil.isNull(name)) {
      return mapper.toTagDTOs(tagRepository.findByNameContaining(name));
    }
    return mapper.toTagDTOs(tagRepository.findAll());
  }
}
