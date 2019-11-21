package cn.kastner.oj.service.impl;

import cn.kastner.oj.domain.User;
import cn.kastner.oj.domain.log.AuthLog;
import cn.kastner.oj.domain.security.Authority;
import cn.kastner.oj.domain.security.AuthorityName;
import cn.kastner.oj.domain.security.UserContext;
import cn.kastner.oj.dto.AuthLogDTO;
import cn.kastner.oj.dto.UserDTO;
import cn.kastner.oj.exception.AuthenticationException;
import cn.kastner.oj.exception.UserException;
import cn.kastner.oj.repository.AuthLogRepository;
import cn.kastner.oj.repository.AuthorityRepository;
import cn.kastner.oj.repository.UserRepository;
import cn.kastner.oj.security.JwtUser;
import cn.kastner.oj.service.AuthenticationService;
import cn.kastner.oj.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

  private AuthenticationManager authenticationManager;

  private UserDetailsService userDetailsService;

  private JwtTokenUtil jwtTokenUtil;

  private UserRepository userRepository;

  private AuthorityRepository authorityRepository;

  private AuthLogRepository authLogRepository;

  @Value("${jwt.tokenHead}")
  private String tokenHead;

  @Autowired
  public AuthenticationServiceImpl(
      AuthenticationManager authenticationManager,
      @Qualifier("jwtUserDetailsService") UserDetailsService userDetailsService,
      JwtTokenUtil jwtTokenUtil,
      UserRepository userRepository,
      AuthorityRepository authorityRepository,
      AuthLogRepository authLogRepository) {
    this.authenticationManager = authenticationManager;
    this.userDetailsService = userDetailsService;
    this.jwtTokenUtil = jwtTokenUtil;
    this.userRepository = userRepository;
    this.authorityRepository = authorityRepository;
    this.authLogRepository = authLogRepository;
  }

  @Override
  public User register(UserDTO userDTO) throws UserException {
    final String username = userDTO.getUsername();
    final String email = userDTO.getEmail();

    if (userRepository.existsByUsername(username)) {
      throw new UserException(UserException.DUPLICATED_USERNAME);
    }

    if (userRepository.existsByEmail(email)) {
      throw new UserException(UserException.DUPLICATED_EMAIL);
    }

    List<Authority> authorities = new ArrayList<>();
    authorities.add(authorityRepository.findByName(AuthorityName.ROLE_USER));

    User user =
        new User(
            username,
            userDTO.getPassword(),
            email,
            userDTO.getFirstname(),
            userDTO.getLastname(),
            userDTO.getSchool(),
            authorities);
    return userRepository.save(user);
  }

  @Override
  public User forgotPassword(String rawPassword) {
    User user = UserContext.getCurrentUser();
    user.setPassword(rawPassword);
    user.setLastPasswordResetDate(new Date());
    return userRepository.save(user);
  }

  @Override
  public String login(String username, String password) throws AuthenticationException {
    UsernamePasswordAuthenticationToken upToken =
        new UsernamePasswordAuthenticationToken(username, password);
    Authentication authentication;
    try {
      authentication = authenticationManager.authenticate(upToken);
    } catch (DisabledException e) {
      throw new AuthenticationException(AuthenticationException.USER_DISABLED);
    } catch (BadCredentialsException e) {
      throw new AuthenticationException(AuthenticationException.BAD_CREDENTIALS);
    }
    SecurityContextHolder.getContext().setAuthentication(authentication);
    final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    return jwtTokenUtil.generateToken(userDetails);
  }

  @Override
  public String refresh(String oldToken) {
    final String token = oldToken.substring(tokenHead.length());
    String username = jwtTokenUtil.getUsernameFromToken(token);
    JwtUser user = (JwtUser) userDetailsService.loadUserByUsername(username);
    if (jwtTokenUtil.canTokenBeRefreshed(token, user.getLastPasswordResetDate())) {
      return jwtTokenUtil.refreshToken(token);
    }
    return null;
  }

  @Override
  public void counter(AuthLogDTO authLogDTO) {
    User user = userRepository.findByUsername(authLogDTO.getUserId());
    AuthLog authLog = new AuthLog(user);
    authLog.setTimestamp(authLogDTO.getTimestamp());
    authLogRepository.save(authLog);
  }
}
