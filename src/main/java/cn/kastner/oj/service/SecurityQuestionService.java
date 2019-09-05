package cn.kastner.oj.service;

import cn.kastner.oj.dto.SecurityQuestionDTO;
import cn.kastner.oj.exception.NoSuchItemException;
import cn.kastner.oj.exception.SecurityQuestionException;

import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface SecurityQuestionService {

  SecurityQuestionDTO getOne(String username) throws NoSuchItemException;

  List<SecurityQuestionDTO> getAll();

  Boolean deleteAll() throws NoSuchItemException;

  Boolean saveAnswer(List<SecurityQuestionDTO> securityQuestionDTOList)
      throws SecurityQuestionException;

  Boolean checkAnswer(SecurityQuestionDTO securityQuestionDTO, String username)
      throws NoSuchItemException, NoSuchAlgorithmException;
}
