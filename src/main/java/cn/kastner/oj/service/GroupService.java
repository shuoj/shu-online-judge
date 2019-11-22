package cn.kastner.oj.service;

import cn.kastner.oj.dto.GroupDTO;
import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.exception.AuthorizationException;
import cn.kastner.oj.exception.GroupException;
import cn.kastner.oj.exception.UserException;
import cn.kastner.oj.query.GroupQuery;
import cn.kastner.oj.security.JwtUser;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public interface GroupService {
  GroupDTO findById(String id) throws GroupException;

  List<GroupDTO> findAllGroups() throws GroupException;

  PageDTO<GroupDTO> findCriteria(GroupQuery groupQuery, Integer page, Integer size);

  GroupDTO create(GroupDTO groupDTO) throws GroupException;

  GroupDTO update(GroupDTO groupDTO) throws GroupException, AuthorizationException;

  GroupDTO delete(String id) throws GroupException, AuthorizationException;

  List<JwtUser> addMembers(String id, List<String> usersId) throws UserException, GroupException, AuthorizationException;

  List<JwtUser> deleteMembers(String id, List<String> userId) throws GroupException, UserException, AuthorizationException;

  Workbook resetMembersPassword(String id) throws GroupException;
}
