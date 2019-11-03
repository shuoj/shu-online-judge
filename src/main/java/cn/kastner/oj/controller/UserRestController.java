package cn.kastner.oj.controller;

import cn.kastner.oj.domain.User;
import cn.kastner.oj.dto.ListDTO;
import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.dto.UserGenerationParam;
import cn.kastner.oj.exception.AppException;
import cn.kastner.oj.exception.UserException;
import cn.kastner.oj.exception.ValidateException;
import cn.kastner.oj.query.UserQuery;
import cn.kastner.oj.security.JwtUser;
import cn.kastner.oj.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping(value = "/api/v1/users")
public class UserRestController {
  private final UserService userService;

  @Value("${jwt.header}")
  private String tokenHeader;

  @Autowired
  public UserRestController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public PageDTO<JwtUser> getUser(
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "10") Integer size,
      UserQuery userQuery) {
    return userService.getAllUsers(page, size, userQuery);
  }

  @GetMapping(value = "/{id}")
  public JwtUser getOne(@PathVariable String id) throws UserException {
    return userService.getOne(id);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public JwtUser create(@RequestBody User user) throws UserException {
    return userService.create(user);
  }

  @PutMapping(value = "/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public JwtUser update(@RequestBody User user, @PathVariable String id) throws UserException {
    user.setId(id);
    return userService.update(user);
  }

  @DeleteMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public void delete(@RequestBody ListDTO<String> idList) throws UserException {
//    userService.delete(idList.getList());
  }

  @PostMapping(value = "/generate")
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public PageDTO<JwtUser> generateUsers(
      @Validated @RequestBody UserGenerationParam param, BindingResult bindingResult)
      throws AppException {
    if (bindingResult.hasErrors()) {
      throw new ValidateException(bindingResult.getFieldError().getDefaultMessage());
    }
    if (null != param.getFileToken()) {
      return userService.generateUser(param.getGroupId(), new File(param.getFileToken()));
    } else if (null != param.getQuantity()) {
      return userService.generateUser(param.getGroupId(), param.getQuantity());
    } else {
      throw new ValidateException("必须提供生成用户的文件或数量");
    }
  }

  @GetMapping(value = "/ranking")
  public PageDTO<JwtUser> getUserRanking(@RequestParam Integer page, @RequestParam Integer size) {
    return userService.getUserRanking(page, size);
  }
}
