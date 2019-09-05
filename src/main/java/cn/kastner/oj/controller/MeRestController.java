package cn.kastner.oj.controller;

import cn.kastner.oj.domain.User;
import cn.kastner.oj.domain.security.UserContext;
import cn.kastner.oj.dto.ChangePasswordDTO;
import cn.kastner.oj.exception.AppException;
import cn.kastner.oj.exception.HaveSuchItemException;
import cn.kastner.oj.security.JwtUser;
import cn.kastner.oj.security.JwtUserFactory;
import cn.kastner.oj.service.MeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/me")
public class MeRestController {

  private final MeService meService;

  @Autowired
  public MeRestController(MeService meService) {
    this.meService = meService;
  }

  @GetMapping
  public JwtUser getAuthenticatedUser() {
    return JwtUserFactory.create(UserContext.getCurrentUser());
  }

  @PostMapping
  public JwtUser update(User user) throws HaveSuchItemException {
    return JwtUserFactory.create(meService.update(user));
  }

  @PostMapping(value = "/changePassword")
  public JwtUser changePassword(@RequestBody ChangePasswordDTO changePasswordDTO)
      throws AppException {
    return JwtUserFactory.create(meService.changePassword(changePasswordDTO));
  }
}
