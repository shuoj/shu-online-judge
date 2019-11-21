package cn.kastner.oj.controller;

import cn.kastner.oj.domain.User;
import cn.kastner.oj.dto.SecurityQuestionDTO;
import cn.kastner.oj.dto.UserDTO;
import cn.kastner.oj.exception.AppException;
import cn.kastner.oj.exception.AuthenticationException;
import cn.kastner.oj.exception.NoSuchItemException;
import cn.kastner.oj.exception.ValidateException;
import cn.kastner.oj.security.JwtAuthenticationRequest;
import cn.kastner.oj.security.JwtUser;
import cn.kastner.oj.security.JwtUserFactory;
import cn.kastner.oj.service.AuthenticationService;
import cn.kastner.oj.service.JwtAuthenticationResponse;
import cn.kastner.oj.service.SecurityQuestionService;
import cn.kastner.oj.util.JwtTokenUtil;
import cn.kastner.oj.util.NetResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@RestController
public class AuthenticationRestController {

  private final AuthenticationManager authenticationManager;
  private final JwtTokenUtil jwtTokenUtil;
  private final UserDetailsService userDetailsService;
  private final AuthenticationService authenticationService;
  private final SecurityQuestionService securityQuestionService;

  @Value("${jwt.header}")
  private String tokenHeader;

  @Autowired
  public AuthenticationRestController(
      AuthenticationManager authenticationManager,
      JwtTokenUtil jwtTokenUtil,
      @Qualifier("jwtUserDetailsService") UserDetailsService userDetailsService,
      AuthenticationService authenticationService,
      SecurityQuestionService securityQuestionService) {
    this.authenticationManager = authenticationManager;
    this.jwtTokenUtil = jwtTokenUtil;
    this.userDetailsService = userDetailsService;
    this.authenticationService = authenticationService;
    this.securityQuestionService = securityQuestionService;
  }

  /**
   * @return { "token": "..." }
   * @throws AuthenticationException 认证失败
   */
  @PostMapping(value = "/api/v1/auth")
  public ResponseEntity createAuthenticationToken(
      @RequestBody JwtAuthenticationRequest authenticationRequest) throws AuthenticationException {
    final String token =
        authenticationService.login(
            authenticationRequest.getUsername(), authenticationRequest.getPassword());
    // Return the token
    return ResponseEntity.ok(new JwtAuthenticationResponse(token));
  }

  @GetMapping(value = "/api/v1/refresh")
  public ResponseEntity refreshAndGetAuthenticationToken(HttpServletRequest request) {
    String authToken = request.getHeader(tokenHeader);
    final String token = authToken.substring(7);
    String username = jwtTokenUtil.getUsernameFromToken(token);
    JwtUser user = (JwtUser) userDetailsService.loadUserByUsername(username);

    if (jwtTokenUtil.canTokenBeRefreshed(token, user.getLastPasswordResetDate())) {
      String refreshedToken = jwtTokenUtil.refreshToken(token);
      return ResponseEntity.ok(new JwtAuthenticationResponse(refreshedToken));
    } else {
      return ResponseEntity.badRequest().body(null);
    }
  }

  @PostMapping(value = "/api/v1/register")
  public JwtUser register(@Validated @RequestBody UserDTO userDTO, BindingResult result)
      throws AppException {
    if (result.hasFieldErrors()) {
      throw new ValidateException(result.getFieldError().getDefaultMessage());
    }
    User user = authenticationService.register(userDTO);
    return JwtUserFactory.create(user);
  }

  @PostMapping(value = "/api/v1/questions/checkAnswer")
  public ResponseEntity<?> checkAnswer(
      @RequestBody SecurityQuestionDTO securityQuestionDTO, @RequestParam String username)
      throws NoSuchItemException, NoSuchAlgorithmException {
    boolean result = securityQuestionService.checkAnswer(securityQuestionDTO, username);
    if (result) {
      final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      final String token = jwtTokenUtil.generateToken(userDetails);

      // Return the token
      return ResponseEntity.ok(new JwtAuthenticationResponse(token));
    } else {
      NetResult netResult = new NetResult();
      netResult.message = "安全问题错误";
      netResult.code = -1;
      return ResponseEntity.badRequest().body(netResult);
    }
  }

  @PostMapping("/api/v1/forgotPassword")
  public JwtUser changePassword(@RequestParam String password) {
    return JwtUserFactory.create(authenticationService.forgotPassword(password));
  }

  /**
   * Authenticates the user. If something is wrong, an {@link AuthenticationException} will be
   * thrown
   */
  private void authenticate(String username, String password) throws AppException {
    Objects.requireNonNull(username);
    Objects.requireNonNull(password);

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(username, password));
    } catch (DisabledException e) {
      throw new AuthenticationException(AuthenticationException.USER_DISABLED);
    } catch (BadCredentialsException e) {
      throw new AuthenticationException(AuthenticationException.BAD_CREDENTIALS);
    }
  }
}
