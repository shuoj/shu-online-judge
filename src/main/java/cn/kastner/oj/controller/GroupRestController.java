package cn.kastner.oj.controller;

import cn.kastner.oj.dto.GroupDTO;
import cn.kastner.oj.exception.*;
import cn.kastner.oj.repository.UserRepository;
import cn.kastner.oj.security.JwtUser;
import cn.kastner.oj.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/groups")
public class GroupRestController {

  private final GroupService groupService;


  @Autowired
  public GroupRestController(GroupService groupService) {
    this.groupService = groupService;
  }

  /**
   * 获取指定群组
   *
   * @param id
   * @throws NoSuchItemException return groupDTO
   */
  @GetMapping(value = "/{id}")
  public GroupDTO getGroup(@PathVariable String id) throws GroupException {
    return groupService.findById(id);
  }

  /**
   * 获取所有群组
   *
   * @return
   * @throws NoSuchItemException
   */
  @GetMapping
  public List<GroupDTO> getGroups() throws GroupException {
    return groupService.findAllGroups();
  }

  /**
   * 创建群组
   *
   * @param groupDTO
   * @param bindingResult
   * @return groupDTO
   */
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public GroupDTO createGroup(
      @Validated @RequestBody GroupDTO groupDTO, BindingResult bindingResult)
      throws AppException {
    if (bindingResult.hasErrors()) {
      throw new ValidateException(bindingResult.getFieldError().getDefaultMessage());
    } else {
      return groupService.create(groupDTO);
    }
  }

  /**
   * 更新群组信息
   *
   * @param groupDTO
   * @param bindingResult
   * @param id
   * @return
   */
  @PutMapping(value = "/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public GroupDTO updateGroup(
      @Validated @RequestBody GroupDTO groupDTO,
      BindingResult bindingResult,
      @PathVariable String id)
      throws AppException {
    if (bindingResult.hasErrors()) {
      throw new ValidateException(bindingResult.getFieldError().getDefaultMessage());
    } else {
      groupDTO.setId(id);
      groupDTO.setCreateDate(getGroup(id).getCreateDate());
      return groupService.update(groupDTO);
    }
  }

  /**
   * 删除指定群组
   *
   * @param id
   * @return
   * @throws NoSuchItemException
   */
  @DeleteMapping(value = "/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public GroupDTO deleteGroup(@PathVariable String id) throws AppException {
    return groupService.delete(id);
  }

  /**
   * 添加指定群组成员（由管理员批量添加）
   *
   * @param usersId
   * @param bindingResult
   * @param id
   * @return
   */
  @PostMapping(value = "/{id}/members")
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public List<JwtUser> addMembers(
      @Validated @RequestBody List<String> usersId,
      BindingResult bindingResult,
      @PathVariable String id)
      throws AppException {
    if (bindingResult.hasErrors()) {
      throw new ValidateException(bindingResult.getFieldError().getDefaultMessage());
    }

    return groupService.addMembers(id, usersId);
  }

  /**
   * 删除指定群组内的成员（由管理员批量删除）
   *
   * @param usersId
   * @param bindingResult
   * @param id
   * @return
   * @throws ValidateException
   * @throws NoSuchItemException
   */
  @DeleteMapping(value = "/{id}/members")
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public List<JwtUser> deleteMembers(
      @Validated @RequestBody List<String> usersId,
      BindingResult bindingResult,
      @PathVariable String id)
      throws AppException {
    if (bindingResult.hasErrors()) {
      throw new ValidateException(bindingResult.getFieldError().getDefaultMessage());
    }
    return groupService.deleteMembers(id, usersId);
  }
}
