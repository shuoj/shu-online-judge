package cn.kastner.oj.service;

import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.dto.UserDTO;
import cn.kastner.oj.exception.FileException;
import cn.kastner.oj.exception.GroupException;
import cn.kastner.oj.exception.UserException;
import cn.kastner.oj.query.UserQuery;
import cn.kastner.oj.security.JwtUser;

import java.io.File;
import java.util.List;

public interface UserService {

  PageDTO<JwtUser> getUserRanking(Integer page, Integer size);

  PageDTO<JwtUser> getAllUsers(Integer page, Integer size, UserQuery userQuery);

  JwtUser getOne(String id) throws UserException;

  JwtUser create(UserDTO userDTO) throws UserException;

  JwtUser update(UserDTO userDTO) throws UserException;

  void delete(List<String> id) throws UserException;

  PageDTO<JwtUser> generateUser(String groupId, Long quantity) throws GroupException;

  PageDTO<JwtUser> generateUser(String groupId, File excel) throws GroupException, FileException;

}
