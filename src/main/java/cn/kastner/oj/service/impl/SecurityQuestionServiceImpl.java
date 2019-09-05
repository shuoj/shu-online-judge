package cn.kastner.oj.service.impl;

import cn.kastner.oj.domain.User;
import cn.kastner.oj.domain.UserSecurityQuestion;
import cn.kastner.oj.domain.security.SecurityQuestion;
import cn.kastner.oj.domain.security.UserContext;
import cn.kastner.oj.dto.SecurityQuestionDTO;
import cn.kastner.oj.exception.NoSuchItemException;
import cn.kastner.oj.exception.SecurityQuestionException;
import cn.kastner.oj.repository.SecurityQuestionRepository;
import cn.kastner.oj.repository.UserRepository;
import cn.kastner.oj.repository.UserSecurityQuestionRepository;
import cn.kastner.oj.service.SecurityQuestionService;
import cn.kastner.oj.util.CommonUtil;
import cn.kastner.oj.util.DTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class SecurityQuestionServiceImpl implements SecurityQuestionService {

  private final SecurityQuestionRepository securityQuestionRepository;

  private final UserSecurityQuestionRepository userSecurityQuestionRepository;

  private final UserRepository userRepository;

  private final DTOMapper mapper;

  @Autowired
  public SecurityQuestionServiceImpl(
      SecurityQuestionRepository securityQuestionRepository,
      UserSecurityQuestionRepository userSecurityQuestionRepository,
      UserRepository userRepository,
      DTOMapper mapper) {
    this.securityQuestionRepository = securityQuestionRepository;
    this.userSecurityQuestionRepository = userSecurityQuestionRepository;
    this.userRepository = userRepository;
    this.mapper = mapper;
  }

  @Override
  public SecurityQuestionDTO getOne(String username) throws NoSuchItemException {
    User user = userRepository.findByUsername(username);
    if (user == null) {
      throw new NoSuchItemException("没有这个用户");
    }
    List<UserSecurityQuestion> userSecurityQuestionList =
        userSecurityQuestionRepository.findByUser(user);
    if (userSecurityQuestionList.isEmpty()) {
      throw new NoSuchItemException("未设置密保问题");
    }
    Random r = new Random();
    UserSecurityQuestion userSecurityQuestion = userSecurityQuestionList.get(r.nextInt(2));
    return mapper.entityToDTO(userSecurityQuestion.getSecurityQuestion());
  }

  @Override
  public List<SecurityQuestionDTO> getAll() {
    return mapper.toSecurityQuestionDTOs(securityQuestionRepository.findAll());
  }

  @Override
  public Boolean deleteAll() throws NoSuchItemException {
    User user = UserContext.getCurrentUser();
    List<UserSecurityQuestion> userSecurityQuestionList =
        userSecurityQuestionRepository.findByUser(user);
    if (userSecurityQuestionList.isEmpty()) {
      throw new NoSuchItemException("没有安全问题！");
    }
    userSecurityQuestionRepository.deleteAll(userSecurityQuestionList);
    return true;
  }

  @Override
  public Boolean saveAnswer(List<SecurityQuestionDTO> securityQuestionDTOList)
      throws SecurityQuestionException {
    User user = UserContext.getCurrentUser();

    if (securityQuestionDTOList.size() < 3) {
      throw new SecurityQuestionException(SecurityQuestionException.NOT_ENOUGH_QUESTION);
    }

    List<UserSecurityQuestion> securityQuestionList =
        userSecurityQuestionRepository.findByUser(user);
    if (!securityQuestionList.isEmpty()) {
      throw new SecurityQuestionException(SecurityQuestionException.HAVE_SET_QUESTION);
    }
    List<UserSecurityQuestion> userSecurityQuestionList =
        mapper.toUserSecurityQuestion(securityQuestionDTOList);
    for (UserSecurityQuestion question : userSecurityQuestionList) {
      String id = question.getSecurityQuestion().getId();
      SecurityQuestion securityQuestion =
          securityQuestionRepository
              .findById(id)
              .orElseThrow(
                  () -> new SecurityQuestionException(SecurityQuestionException.NO_SUCH_QUESTION));
      try {
        question.setAnswer(CommonUtil.md5(question.getAnswer()));
      } catch (NoSuchAlgorithmException e) {
        throw new SecurityQuestionException(SecurityQuestionException.ENCRYPT_EXCEPTION);
      }
      question.setUser(user);
      question.setSecurityQuestion(securityQuestion);
    }
    userSecurityQuestionRepository.saveAll(userSecurityQuestionList);
    return true;
  }

  @Override
  public Boolean checkAnswer(SecurityQuestionDTO securityQuestionDTO, String username)
      throws NoSuchItemException, NoSuchAlgorithmException {
    User user = userRepository.findByUsername(username);
    if (user == null) {
      throw new NoSuchItemException("没有 '" + username + "' 这个用户");
    }
    Optional<SecurityQuestion> questionOptional =
        securityQuestionRepository.findById(securityQuestionDTO.getId());
    if (!questionOptional.isPresent()) {
      throw new NoSuchItemException("没有这个安全问题！");
    }
    SecurityQuestion question = questionOptional.get();
    UserSecurityQuestion userSecurityQuestion =
        userSecurityQuestionRepository.findByUserAndSecurityQuestion(user, question);
    return CommonUtil.md5(securityQuestionDTO.getAnswer()).equals(userSecurityQuestion.getAnswer());
  }
}
