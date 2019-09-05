package cn.kastner.oj.controller;

import cn.kastner.oj.domain.User;
import cn.kastner.oj.dto.ListDTO;
import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.exception.NoSuchItemException;
import cn.kastner.oj.exception.UserException;
import cn.kastner.oj.query.UserQuery;
import cn.kastner.oj.security.JwtUser;
import cn.kastner.oj.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
  @PreAuthorize("hasRole('ADMIN')")
  public PageDTO<JwtUser> getUser(
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "10") Integer size,
      UserQuery userQuery) {
    return userService.getAllUsers(page, size, userQuery);
  }

  @GetMapping(value = "/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public JwtUser getOne(@PathVariable String id) throws UserException {
    return userService.getOne(id);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public JwtUser create(User user) throws UserException {
    return userService.create(user);
  }

  @PutMapping(value = "/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public JwtUser update(User user, @PathVariable String id) throws UserException {
    user.setId(id);
    return userService.update(user);
  }

  @DeleteMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<JwtUser> delete(@RequestBody ListDTO<String> idList) throws NoSuchItemException {
    return userService.delete(idList.getList());
  }

  @GetMapping(value = "/ranking")
  public PageDTO<JwtUser> getUserRanking(@RequestParam Integer page, @RequestParam Integer size) {
    return userService.getUserRanking(page, size);
  }
}
