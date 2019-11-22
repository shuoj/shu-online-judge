package cn.kastner.oj.controller;

import cn.kastner.oj.dto.GroupDTO;
import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.exception.AppException;
import cn.kastner.oj.exception.GroupException;
import cn.kastner.oj.exception.ValidateException;
import cn.kastner.oj.query.GroupQuery;
import cn.kastner.oj.security.JwtUser;
import cn.kastner.oj.service.GroupService;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
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
   */
  @GetMapping(value = "/{id}")
  public GroupDTO getGroup(@PathVariable String id) throws GroupException {
    return groupService.findById(id);
  }

  /** 条件查询群组 */
  @GetMapping
  public PageDTO<GroupDTO> findCriteria(GroupQuery query, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer size) {
    return groupService.findCriteria(query, page, size);
  }

  /** 创建群组 */
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public GroupDTO createGroup(
      @Validated @RequestBody GroupDTO groupDTO, BindingResult bindingResult) throws AppException {
    if (bindingResult.hasErrors()) {
      throw new ValidateException(bindingResult.getFieldError().getDefaultMessage());
    } else {
      return groupService.create(groupDTO);
    }
  }

  /** 更新群组信息 */
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

  /** 删除指定群组 */
  @DeleteMapping(value = "/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public GroupDTO deleteGroup(@PathVariable String id) throws AppException {
    return groupService.delete(id);
  }

  /** 添加指定群组成员（由管理员批量添加） */
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

  /** 删除指定群组内的成员（由管理员批量删除） */
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

  @PostMapping(value = "/{id}/members/resetPassword")
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public void resetMemberPassword(@PathVariable String id, HttpServletResponse response) throws GroupException {
    response.setHeader("content-type", "application/octet-stream");
    response.setContentType("application/octet-stream");
    response.setHeader("Content-Disposition", "attachment");

    Workbook workbook = groupService.resetMembersPassword(id);
    try (OutputStream os = response.getOutputStream()) {
      workbook.write(os);
    } catch (IOException e) {
      throw new GroupException(GroupException.RESET_ERROR);
    }
  }
}
