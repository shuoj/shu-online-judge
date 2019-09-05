package cn.kastner.oj.service.impl;

import cn.kastner.oj.domain.Problem;
import cn.kastner.oj.dto.ProblemDTO;
import cn.kastner.oj.dto.RecommendOptionDTO;
import cn.kastner.oj.repository.ProblemRepository;
import cn.kastner.oj.repository.UserTagStatRepository;
import cn.kastner.oj.repository.result.TagScore;
import cn.kastner.oj.service.RecommendService;
import cn.kastner.oj.util.DTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImpl implements RecommendService {

  private final UserTagStatRepository userTagStatRepository;
  private final ProblemRepository problemRepository;
  private final DTOMapper mapper;

  @Autowired
  public RecommendServiceImpl(
      UserTagStatRepository userTagStatRepository,
      ProblemRepository problemRepository,
      DTOMapper mapper) {
    this.userTagStatRepository = userTagStatRepository;
    this.problemRepository = problemRepository;
    this.mapper = mapper;
  }

  @Override
  public List<ProblemDTO> recommend(RecommendOptionDTO recommendOptionDTO) {
    Integer difficultDegree = recommendOptionDTO.getDifficultDegree();
    Integer problemCount = recommendOptionDTO.getCount();
    Integer submitInterval = recommendOptionDTO.getSubmitInterval();
    List<String> userIdList = recommendOptionDTO.getUserIdList();
    List<TagScore> tagScoreResult =
        userTagStatRepository.findSumOfScoreGroupByUserId(userIdList, 10);
    List<String> tagIdList =
        tagScoreResult.stream().map(TagScore::getTagId).collect(Collectors.toList());

    TagScore[] tagScores = new TagScore[tagScoreResult.size()];
    tagScores = tagScoreResult.toArray(tagScores);

    TagScore min = tagScores[0];
    TagScore max = tagScores[tagScores.length - 1];
    Integer diff = max.getScore() - min.getScore();
    Integer aIndex = findIndex(diff * 0.25, tagScores);
    Integer bIndex = findIndex(diff * 0.5, tagScores);
    Integer cIndex = findIndex(diff * 0.75, tagScores);

    Integer aRatio = computeProblemRatio(1, difficultDegree);
    Integer bRatio = computeProblemRatio(2, difficultDegree);
    Integer cRatio = computeProblemRatio(3, difficultDegree);
    Integer dRatio = computeProblemRatio(4, difficultDegree);
    Integer ratioSum = aRatio + bRatio + cRatio + dRatio;

    Integer aCount = Math.floorDiv(aRatio * problemCount, ratioSum);
    Integer bCount = Math.floorDiv(bRatio * problemCount, ratioSum);
    Integer cCount = Math.floorDiv(cRatio * problemCount, ratioSum);
    Integer dCount = Math.floorDiv(dRatio * problemCount, ratioSum);

    Set<Problem> problemSet = new HashSet<>();
    problemSet.addAll(pickProblems(0, aIndex, tagIdList, userIdList, aCount, submitInterval));
    problemSet.addAll(pickProblems(aIndex, bIndex, tagIdList, userIdList, bCount, submitInterval));
    problemSet.addAll(pickProblems(bIndex, cIndex, tagIdList, userIdList, cCount, submitInterval));
    problemSet.addAll(
        pickProblems(cIndex, tagIdList.size() - 1, tagIdList, userIdList, dCount, submitInterval));
    while (problemSet.size() < problemCount) {
      problemSet.addAll(
          problemRepository.findRandomByTagAndUserAndLastSubmitDateAndLimit(
              tagIdList, userIdList, submitInterval, problemCount - problemSet.size()));
    }
    return mapper.toProblemDTOs(new ArrayList<>(problemSet));
  }

  private Integer findIndex(Double number, TagScore[] tagScores) {
    if (tagScores.length == 1) {
      return 0;
    }

    for (int i = 0, len = tagScores.length; i < len; i++) {
      if (tagScores[i].getScore() > number) {
        if (i == 0) {
          return 0;
        } else {
          return i - 1;
        }
      }
    }
    return tagScores.length - 1;
  }

  private Integer computeProblemRatio(Integer index, Integer difficultDegree) {
    if (index % 2 == 0) {
      return Math.floorDiv(10, difficultDegree * (index + 3)) + 1;
    } else {
      return Math.floorDiv(10, difficultDegree * (index + 3));
    }
  }

  private List<Problem> pickProblems(
      Integer upBound,
      Integer downBound,
      List<String> tagIdList,
      List<String> userIdList,
      Integer count,
      Integer submitInterval) {
    Long problemCount =
        problemRepository.countByTagAndUserAndLastSubmitDate(tagIdList, userIdList, submitInterval);
    if (problemCount < count) {
      return problemRepository.findAllByTagAndUserAndLastSubmitDate(
          tagIdList.subList(upBound, downBound), userIdList, submitInterval);
    }
    return problemRepository.findRandomByTagAndUserAndLastSubmitDateAndLimit(
        tagIdList.subList(upBound, downBound), userIdList, submitInterval, count);
  }
}
