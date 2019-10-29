package cn.kastner.oj.service;

import cn.kastner.oj.domain.User;
import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.exception.*;
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

  void delete(List<String> id) throws NoSuchItemException;

  PageDTO<JwtUser> generateUser(String groupId, Long quantity) throws GroupException;

  PageDTO<JwtUser> generateUser(String groupId, File excel) throws GroupException, FileException;

}
