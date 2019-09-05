package cn.kastner.oj.service.impl;

import cn.kastner.oj.domain.User;
import cn.kastner.oj.domain.security.UserContext;
import cn.kastner.oj.dto.ChangePasswordDTO;
import cn.kastner.oj.exception.HaveSuchItemException;
import cn.kastner.oj.exception.ValidateException;
import cn.kastner.oj.repository.UserRepository;
import cn.kastner.oj.service.MeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class MeServiceImpl implements MeService {

  private final UserRepository userRepository;

  @Autowired
  public MeServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public User update(User user) throws HaveSuchItemException {
    String username = user.getUsername();
    Optional<User> userOptional = userRepository.findUserByUsername(username);
    if (userOptional.isPresent() && userOptional.get().getId().equals(user.getId())) {
      throw new HaveSuchItemException("用户名重复！");
    }

    String email = user.getEmail();
    Optional<User> userOptional1 = userRepository.findByEmail(email);
    if (userOptional1.isPresent() && userOptional1.get().getEmail().equals(user.getEmail())) {
      throw new HaveSuchItemException("邮箱重复！");
    }
    User currentUser = UserContext.getCurrentUser();
    currentUser.setUsername(username);
    currentUser.setEmail(email);
    currentUser.setSchool(user.getSchool());
    currentUser.setSignature(user.getSignature());
    currentUser.setStudentNumber(user.getStudentNumber());
    currentUser.setFirstname(user.getFirstname());
    currentUser.setLastname(user.getLastname());
    return userRepository.save(currentUser);
  }

  @Override
  public User changePassword(ChangePasswordDTO changePasswordDTO) throws ValidateException {
    User user = UserContext.getCurrentUser();
    String oldPassword = changePasswordDTO.getOldPassword();
    String newPassword = changePasswordDTO.getNewPassword();

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    String encodedOldPassword = encoder.encode(oldPassword);
    if (!encodedOldPassword.equals(user.getPassword())) {
      throw new ValidateException(ValidateException.WRONG_OLD_PASSWORD);
    }

    user.setPassword(encoder.encode(newPassword));
    user.setLastPasswordResetDate(new Date());
    return userRepository.save(user);
  }
}
