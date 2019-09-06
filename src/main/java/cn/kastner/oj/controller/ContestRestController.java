package cn.kastner.oj.controller;

import cn.kastner.oj.domain.ContestOption;
import cn.kastner.oj.dto.ContestDTO;
import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.dto.ProblemDTO;
import cn.kastner.oj.dto.RankingDTO;
import cn.kastner.oj.exception.AppException;
import cn.kastner.oj.exception.ValidateException;
import cn.kastner.oj.query.ContestQuery;
import cn.kastner.oj.security.JwtUser;
import cn.kastner.oj.service.ContestService;
import cn.kastner.oj.util.NetResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/contests")
public class ContestRestController {

  private final ContestService contestService;

  private final NetResult netResult;

  @Autowired
  public ContestRestController(ContestService contestService, NetResult netResult) {
    this.contestService = contestService;
    this.netResult = netResult;
  }

  /**
   * 获取指定比赛
   *
   * @param id 比赛 id
   */
  @GetMapping(value = "/{id}")
  public ContestDTO getContest(@PathVariable String id) throws AppException {
    return contestService.findById(id);
  }

  /**
   * 获取比赛列表
   *
   * @param contestQuery name
   * @param page         页数
   * @param size         数量
   */
  @GetMapping
  public PageDTO<ContestDTO> getContests(
      ContestQuery contestQuery,
      @RequestParam(value = "page", defaultValue = "0") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size) {
    return contestService.findCriteria(page, size, contestQuery);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ContestDTO createContest(
      @Validated @RequestBody ContestDTO contestDTO, BindingResult bindingResult)
      throws AppException {
    if (bindingResult.hasErrors()) {
      throw new ValidateException(bindingResult.getFieldError().getDefaultMessage());
    } else {
      return contestService.create(contestDTO);
    }
  }

  @PutMapping(value = "/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ContestDTO updateContest(
      @Validated @RequestBody ContestDTO contestDTO,
      BindingResult bindingResult,
      @PathVariable String id)
      throws AppException {
    if (bindingResult.hasErrors()) {
      throw new ValidateException(bindingResult.getFieldError().getDefaultMessage());
    } else {
      contestDTO.setId(id);
      return contestService.update(contestDTO);
    }
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ContestDTO partUpdateContest(@PathVariable String id, ContestDTO contestDTO)
      throws AppException {
    contestDTO.setId(id);
    return contestService.partUpdate(contestDTO);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ContestDTO deleteContest(@PathVariable String id) throws AppException {
    return contestService.delete(id);
  }

  @GetMapping("/{id}/problems")
  public List<ProblemDTO> getProblems(@PathVariable String id) throws AppException {
    return contestService.findAllProblems(id);
  }

  @PostMapping("/{id}/problems")
  @PreAuthorize("hasRole('ADMIN')")
  public List<ProblemDTO> setProblems(
      @RequestBody List<String> problemIdList, @PathVariable String id) throws AppException {
    return contestService.addProblems(problemIdList, id);
  }

  @DeleteMapping("/{id}/problems")
  @PreAuthorize("hasRole('ADMIN')")
  public List<ProblemDTO> deleteProblems(
      @RequestBody List<String> problemIdList, @PathVariable String id) throws AppException {
    return contestService.deleteProblems(problemIdList, id);
  }

  @PostMapping(value = "/{id}/groups")
  @PreAuthorize("hasRole('ADMIN')")
  public List<JwtUser> addUsersByGroups(
      @RequestBody List<String> groupIdList, @PathVariable String id) throws AppException {
    return contestService.addUsersByGroups(groupIdList, id);
  }

  @PostMapping("/{id}/join")
  public NetResult joinContest(@PathVariable String id, String password) throws AppException {
    Boolean result = contestService.joinContest(id, password);
    if (result) {
      netResult.message = "加入成功";
    } else {
      netResult.message = "密码错误";
    }
    netResult.code = 200;
    netResult.data = result;
    return netResult;
  }

  @GetMapping("/{id}/users")
  public List<JwtUser> getUsers(@PathVariable String id) throws AppException {
    return contestService.getUsers(id);
  }

  @PostMapping("/{id}/users")
  @PreAuthorize("hasRole('ADMIN')")
  public List<JwtUser> addUsers(@RequestBody List<String> userIdList, @PathVariable String id)
      throws AppException {
    return contestService.addUsers(userIdList, id);
  }

  @DeleteMapping("/{id}/users")
  @PreAuthorize("hasRole('ADMIN')")
  public List<JwtUser> deleteUsers(@RequestBody List<String> userIdList, @PathVariable String id)
      throws AppException {
    return contestService.deleteUsers(userIdList, id);
  }

  @GetMapping("/{id}/ranking")
  public RankingDTO getRanking(@PathVariable String id) throws AppException {
    return contestService.getRanking(id);
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize("hasRole('ADMIN')")
  public ContestDTO setContestStatus(@PathVariable String id, @RequestParam ContestOption option)
      throws AppException {
    return contestService.setContestStatus(id, option);
  }
}