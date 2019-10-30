package cn.kastner.oj.util;

import cn.kastner.oj.domain.*;
import cn.kastner.oj.domain.log.AuthLog;
import cn.kastner.oj.domain.security.SecurityQuestion;
import cn.kastner.oj.dto.*;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface DTOMapper {

  @Mapping(target = "name", source = "user.name")
  @Mapping(target = "username", source = "user.username")
  @Mapping(target = "userId", source = "user.id")
  AuthLogDTO entityToDTO(AuthLog authLog);

  List<AuthLogDTO> toAuthLogDTOs(List<AuthLog> authLogs);

  @Mapping(target = "authorName", source = "author.username")
  @Mapping(target = "authorId", source = "author.id")
  @Mapping(target = "problemId", source = "problem.id")
  @Mapping(target = "problemTitle", source = "problem.title")
  @Mapping(target = "contestId", source = "contest.id")
  SubmissionDTO entityToDTO(Submission submission);

  @InheritInverseConfiguration
  @Mapping(target = "createDate", ignore = true)
  @Mapping(target = "id", ignore = true)
  Submission dtoToEntity(SubmissionDTO submissionDTO);

  List<SubmissionDTO> toSubmissionDTOs(List<Submission> submissions);

  @Mapping(target = "authorName", source = "author.username")
  @Mapping(target = "authorId", source = "author.id")
  ProblemDTO entityToDTO(Problem problem);

  List<ProblemDTO> toProblemDTOs(List<Problem> problems);

  @InheritInverseConfiguration
  @Mapping(target = "averageAcceptTime", ignore = true)
  @Mapping(target = "acceptCount", ignore = true)
  @Mapping(target = "submitCount", ignore = true)
  @Mapping(target = "acceptRate", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  @Mapping(target = "lastUsedDate", ignore = true)
  @Mapping(target = "modifiedDate", ignore = true)
  @Mapping(target = "id", ignore = true)
  Problem dtoToEntity(ProblemDTO problemDTO);

  TagDTO entityToDTO(Tag tag);

  @InheritInverseConfiguration
  @Mapping(target = "id", ignore = true)
  Tag dtoToEntity(TagDTO tagDTO);

  List<TagDTO> toTagDTOs(List<Tag> tags);

  @Mapping(target = "authorName", source = "author.username")
  @Mapping(target = "authorId", source = "author.id")
  @Mapping(target = "password", ignore = true)
  ContestDTO entityToDTO(Contest contest);

  @InheritInverseConfiguration
  @Mapping(target = "enable", ignore = true)
  @Mapping(target = "visible", ignore = true)
  @Mapping(target = "createDate", ignore = true)
  @Mapping(target = "id", ignore = true)
  Contest dtoToEntity(ContestDTO contestDTO);

  List<ContestDTO> toContestDTOs(List<Contest> contests);

  @Mapping(target = "authorName", source = "author.username")
  @Mapping(target = "authorId", source = "author.id")
  AnnouncementDTO entityToDTO(Announcement announcement);

  @InheritInverseConfiguration
  @Mapping(target = "id", ignore = true)
  Announcement dtoToEntity(AnnouncementDTO announcementDTO);

  GroupDTO entityToDTO(Group group);

  @InheritInverseConfiguration
  @Mapping(target = "id", ignore = true)
  Group dtoToEntity(GroupDTO groupDTO);

  @Mapping(target = "contestId", source = "contest.id")
  @Mapping(target = "contestName", source = "contest.name")
  RankingDTO entityToDTO(Ranking ranking, @Context Boolean frozen);

  @InheritInverseConfiguration
  @Mapping(target = "id", ignore = true)
  Ranking dtoToEntity(RankingDTO rankingDTO);

  default RankingUserDTO entityToDTO(RankingUser rankingUser, @Context Boolean frozen) {
    if (rankingUser == null) {
      return null;
    }

    RankingUserDTO rankingUserDTO = new RankingUserDTO();
    rankingUserDTO.setRanked(rankingUser.getRanked());
    rankingUserDTO.setId(rankingUser.getId());
    rankingUserDTO.setUserId(rankingUser.getUser().getId());
    rankingUserDTO.setUserName(rankingUser.getUser().getUsername());
    rankingUserDTO.setRank(rankingUser.getRankingNumber());
    if (frozen) {
      rankingUserDTO.setAcceptCount(rankingUser.getAcceptCountBefore());
      rankingUserDTO.setSubmitCount(rankingUser.getSubmitCountBefore());
      rankingUserDTO.setTotalTime(entityToDTO(rankingUser.getTotalTimeBefore()));
      List<TimeCostDTO> timeCostDTOList = new ArrayList<>();
      for (TimeCost timeCost : rankingUser.getTimeListBefore()) {
        timeCostDTOList.add(entityToDTO(timeCost));
      }
      rankingUserDTO.setTimeList(timeCostDTOList);
      return rankingUserDTO;
    } else {
      rankingUserDTO.setAcceptCount(rankingUser.getAcceptCountAfter());
      rankingUserDTO.setSubmitCount(rankingUser.getSubmitCountAfter());
      rankingUserDTO.setTotalTime(entityToDTO(rankingUser.getTotalTimeAfter()));
      List<TimeCostDTO> timeCostDTOList = new ArrayList<>();
      for (TimeCost timeCost : rankingUser.getTimeListAfter()) {
        timeCostDTOList.add(entityToDTO(timeCost));
      }
      rankingUserDTO.setTimeList(timeCostDTOList);
      return rankingUserDTO;
    }
  }

  @Mapping(target = "contestId", source = "contestProblem.contest.id")
  @Mapping(target = "problemId", source = "contestProblem.problem.id")
  @Mapping(target = "userId", source = "rankingUser.user.id")
  @Mapping(target = "userName", source = "rankingUser.user.username")
  TimeCostDTO entityToDTO(TimeCost timeCost);

  @Mapping(target = "authorName", source = "author.username")
  @Mapping(target = "contestId", source = "contest.id")
  ClarificationDTO entityToDTO(Clarification clarification);

  @InheritInverseConfiguration
  @Mapping(target = "id", ignore = true)
  Clarification dtoToEntity(ClarificationDTO clarificationDTO);

  List<ClarificationDTO> toClarificationDTOs(List<Clarification> clarificationList);

  @Mapping(target = "securityQuestion.id", source = "id")
  @Mapping(target = "id", ignore = true)
  UserSecurityQuestion dtoToEntity(SecurityQuestionDTO securityQuestionDTO);

  SecurityQuestionDTO entityToDTO(SecurityQuestion securityQuestion);

  List<UserSecurityQuestion> toUserSecurityQuestion(
      List<SecurityQuestionDTO> securityQuestionDTOList);

  List<SecurityQuestionDTO> toSecurityQuestionDTOs(List<SecurityQuestion> securityQuestionList);
}
