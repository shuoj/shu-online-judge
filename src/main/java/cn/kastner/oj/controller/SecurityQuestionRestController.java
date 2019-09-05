package cn.kastner.oj.controller;

import cn.kastner.oj.domain.security.SecurityQuestion;
import cn.kastner.oj.dto.SecurityQuestionDTO;
import cn.kastner.oj.exception.NoSuchItemException;
import cn.kastner.oj.exception.SecurityQuestionException;
import cn.kastner.oj.repository.SecurityQuestionRepository;
import cn.kastner.oj.service.SecurityQuestionService;
import cn.kastner.oj.util.NetResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/questions")
public class SecurityQuestionRestController {

  private final SecurityQuestionService securityQuestionService;

  private final SecurityQuestionRepository securityQuestionRepository;

  @Autowired
  public SecurityQuestionRestController(
      SecurityQuestionService securityQuestionService,
      SecurityQuestionRepository securityQuestionRepository) {
    this.securityQuestionService = securityQuestionService;
    this.securityQuestionRepository = securityQuestionRepository;
  }

  @GetMapping(value = "/random")
  public SecurityQuestionDTO getOne(@RequestParam String username) throws NoSuchItemException {
    return securityQuestionService.getOne(username);
  }

  @GetMapping
  public List<SecurityQuestionDTO> getAll() {
    return securityQuestionService.getAll();
  }

  @PostMapping
  public NetResult saveAnswer(@RequestBody List<SecurityQuestionDTO> securityQuestionDTOList)
      throws SecurityQuestionException {
    NetResult netResult = new NetResult();
    netResult.code = 0;
    netResult.data = securityQuestionService.saveAnswer(securityQuestionDTOList);
    netResult.message = "保存成功";
    return netResult;
  }

  @DeleteMapping
  public NetResult deleteAll() throws NoSuchItemException {
    NetResult netResult = new NetResult();
    netResult.data = securityQuestionService.deleteAll();
    netResult.code = 0;
    netResult.message = "删除成功";
    return netResult;
  }

  @PostMapping(value = "/add")
  public NetResult addQuestions() {
    String[] questions = {
        "你少年时代最好的朋友叫什么名字？",
        "你的第一个宠物叫什么名字？",
        "你学会做的第一道菜是什么？",
        "你第一次去电影院看的是哪一部电影？",
        "你第一次坐飞机是去哪里？",
        "你上小学时最喜欢的老师姓什么？"
    };
    List<SecurityQuestion> securityQuestionList = new ArrayList<>();
    for (int i = 0; i < questions.length; i++) {
      SecurityQuestion question = new SecurityQuestion();
      question.setQuestion(questions[i]);
      securityQuestionList.add(question);
    }
    securityQuestionRepository.saveAll(securityQuestionList);
    NetResult netResult = new NetResult();
    netResult.code = 0;
    netResult.message = "添加成功！";
    return netResult;
  }
}
