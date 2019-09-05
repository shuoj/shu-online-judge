package cn.kastner.oj.controller;

import cn.kastner.oj.dto.ProblemDTO;
import cn.kastner.oj.dto.RecommendOptionDTO;
import cn.kastner.oj.exception.ValidateException;
import cn.kastner.oj.service.ProblemService;
import cn.kastner.oj.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1")
public class RecommendController {

  private final RecommendService recommendService;

  private final ProblemService problemService;

  @Autowired
  public RecommendController(RecommendService recommendService, ProblemService problemService) {
    this.recommendService = recommendService;
    this.problemService = problemService;
  }

  @PostMapping(value = "/problems/recommend")
  public List<ProblemDTO> recommendProblems(@Validated @RequestBody RecommendOptionDTO recommendOptionDTO, BindingResult bindingResult) throws ValidateException {
    if (bindingResult.hasErrors()) {
      throw new ValidateException(bindingResult.getFieldError().getDefaultMessage());
    } else {
      return recommendService.recommend(recommendOptionDTO);
//      return problemService.findProblemNoCriteria(0, recommendOptionDTO.getCount());
    }
  }
}
