package cn.kastner.oj.util;

import cn.kastner.oj.domain.*;
import cn.kastner.oj.domain.enums.ContestStatus;
import cn.kastner.oj.domain.enums.ContestType;
import cn.kastner.oj.domain.log.AuthLog;
import cn.kastner.oj.domain.security.SecurityQuestion;
import cn.kastner.oj.domain.security.UserContext;
import cn.kastner.oj.dto.*;
import org.mapstruct.*;

import java.util.*;

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

  @AfterMapping
  default void beforeMappingSubmission(@MappingTarget SubmissionDTO target, Submission source) {
    User user = UserContext.getCurrentUser();
    Contest contest = source.getContest();
    if (contest.getContestType().equals(ContestType.OI) && !contest.getStatus().equals(ContestStatus.ENDED) && !user.isAdminOrStuff()) {
      target.setResult(null);
      target.setDuration(null);
      target.setMemory(null);
      target.setResultDetail(null);
    }
  }

  @InheritInverseConfiguration
  @Mapping(target = "createDate", ignore = true)
  @Mapping(target = "id", ignore = true)
  Submission dtoToEntity(SubmissionDTO submissionDTO);

  List<SubmissionDTO> toSubmissionDTOs(List<Submission> submissions);

  @Mapping(target = "authorName", source = "author.username")
  @Mapping(target = "authorId", source = "author.id")
  ProblemDTO entityToDTO(Problem problem);

  @Mapping(target = "authorName", source = "problem.author.name")
  @Mapping(target = "title", source = "problem.title")
  @Mapping(target = "description", source = "problem.description")
  @Mapping(target = "timeLimit", source = "problem.timeLimit")
  @Mapping(target = "ramLimit", source = "problem.ramLimit")
  @Mapping(target = "difficulty", source = "problem.difficulty")
  @Mapping(target = "tagList", source = "problem.tagList")
  @Mapping(target = "inputDesc", source = "problem.inputDesc")
  @Mapping(target = "outputDesc", source = "problem.outputDesc")
  @Mapping(target = "sampleIO", source = "problem.sampleIO")
  @Mapping(target = "hint", source = "problem.hint")
  @Mapping(target = "source", source = "problem.source")
  @Mapping(target = "createDate", source = "problem.createDate")
  @Mapping(target = "lastUsedDate", source = "problem.lastUsedDate")
  @Mapping(target = "modifiedDate", source = "problem.modifiedDate")
  @Mapping(target = "id", source = "problem.id")
  ProblemDTO entityToDTO(ContestProblem contestProblem);

  List<ProblemDTO> toContestProblemDTOs(Collection<ContestProblem> contestProblems);

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

  Set<TagDTO> toTagDTOs(Set<Tag> tags);

  List<TagDTO> toTagDTOs(List<Tag> tags);

  @InheritInverseConfiguration
  Set<Tag> toTags(Set<TagDTO> tagDTOs);

  @Mapping(target = "authorName", source = "author.username")
  @Mapping(target = "authorId", source = "author.id")
  @Mapping(target = "password", ignore = true)
  ContestDTO entityToDTO(Contest contest);

  @InheritInverseConfiguration
  @Mapping(target = "enable", ignore = true)
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

  @Mapping(target = "authorName", source = "author.username")
  @Mapping(target = "authorId", source = "author.id")
  GroupDTO entityToDTO(Group group);

  @InheritInverseConfiguration
  @Mapping(target = "id", ignore = true)
  Group dtoToEntity(GroupDTO groupDTO);

  List<GroupDTO> toGroupDTOs(List<Group> groupList);

  @Mapping(target = "contestId", source = "id")
  @Mapping(target = "contestName", source = "name")
  RankingDTO contestToRankingDTO(Contest contest);

  @Mapping(target = "userName", source = "user.username")
  @Mapping(target = "userId", source = "user.id")
  RankingUserDTO entityToDTO(RankingUser rankingUser);

  List<RankingUserDTO> toRankingUserDTOs(List<RankingUser> rankingUserList);

  TimeCostDTO entityToDTO(TimeCost timeCost);

  default Map<String, TimeCostDTO> toLabelTimeCostDTOs(List<TimeCost> timeCostList) {
    Map<String, TimeCostDTO> labelTimeCostDTO = new HashMap<>();
    for (TimeCost timeCost : timeCostList) {
      labelTimeCostDTO.put(timeCost.getContestProblem().getLabel(), entityToDTO(timeCost));
    }
    return labelTimeCostDTO;
  }

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
