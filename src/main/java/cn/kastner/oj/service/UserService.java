package cn.kastner.oj.service;

import cn.kastner.oj.domain.User;
import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.exception.ContestException;
import cn.kastner.oj.exception.FileException;
import cn.kastner.oj.exception.NoSuchItemException;
import cn.kastner.oj.exception.UserException;
import cn.kastner.oj.query.UserQuery;
import cn.kastner.oj.security.JwtUser;

import java.io.File;
import java.util.List;

public interface UserService {

  PageDTO<JwtUser> getUserRanking(Integer page, Integer size);

  PageDTO<JwtUser> getAllUsers(Integer page, Integer size, UserQuery userQuery);

  JwtUser getOne(String id) throws UserException;

  JwtUser create(User user) throws UserException;

  JwtUser update(User user) throws UserException;

  List<JwtUser> delete(List<String> id) throws NoSuchItemException;

  PageDTO<JwtUser> generateContestUser(String id, File excel)
      throws FileException, ContestException;

  PageDTO<JwtUser> generateUser(File excel) throws FileException;
}
