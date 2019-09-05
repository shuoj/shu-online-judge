package cn.kastner.oj.service.impl;

import cn.kastner.oj.domain.Group;
import cn.kastner.oj.domain.User;
import cn.kastner.oj.dto.GroupDTO;
import cn.kastner.oj.exception.GroupException;
import cn.kastner.oj.exception.UserException;
import cn.kastner.oj.repository.GroupRepository;
import cn.kastner.oj.repository.UserRepository;
import cn.kastner.oj.security.JwtUser;
import cn.kastner.oj.security.JwtUserFactory;
import cn.kastner.oj.service.GroupService;
import cn.kastner.oj.util.DTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class GroupServiceImpl implements GroupService {

  private final GroupRepository groupRepository;

  private final DTOMapper mapper;

  private final UserRepository userRepository;

  @Autowired
  public GroupServiceImpl(
      GroupRepository groupRepository, DTOMapper mapper, UserRepository userRepository) {
    this.groupRepository = groupRepository;
    this.mapper = mapper;
    this.userRepository = userRepository;
  }

  @Override
  public GroupDTO findById(String id) throws GroupException {
    Group group =
        groupRepository
            .findById(id)
            .orElseThrow(() -> new GroupException(GroupException.NO_SUCH_GROUP));
    GroupDTO groupDTO = mapper.entityToDTO(group);
    List<JwtUser> jwtUserList = findAllMembers(id);
    groupDTO.setJwtUserList(jwtUserList);
    return groupDTO;
  }

  @Override
  public List<GroupDTO> findAllGroups() throws GroupException {
    List<Group> groupList = groupRepository.findAll();
    if (groupList != null && !groupList.isEmpty()) {
      List<GroupDTO> groupDTOList = new ArrayList<>();
      for (Group group : groupList) {
        GroupDTO groupDTO = mapper.entityToDTO(group);
        List<JwtUser> jwtUserList = findAllMembers(group.getId());
        groupDTO.setJwtUserList(jwtUserList);

        groupDTOList.add(groupDTO);
      }
      return groupDTOList;
    }
    return new ArrayList<>();
  }

  @Override
  public GroupDTO create(GroupDTO groupDTO) throws GroupException {
    Optional<Group> groupOptional = groupRepository.findByName(groupDTO.getName());
    if (groupOptional.isPresent()) {
      throw new GroupException(GroupException.HAVE_SUCH_GROUP);
    }
    Group group = mapper.dtoToEntity(groupDTO);
    group.setCreateDate(LocalDateTime.now());
    return mapper.entityToDTO(groupRepository.save(group));
  }

  @Override
  public GroupDTO update(GroupDTO groupDTO) throws GroupException {
    Group group =
        groupRepository
            .findById(groupDTO.getId())
            .orElseThrow(() -> new GroupException(GroupException.NO_SUCH_GROUP));
    Optional<Group> existGroupOptional = groupRepository.findByName(groupDTO.getName());
    if (existGroupOptional.isPresent()
        && !existGroupOptional.get().getId().equals(groupDTO.getId())) {
      throw new GroupException(GroupException.HAVE_SUCH_GROUP);
    } else {
      Set<User> userSet = group.getUserSet();
      Group tempGroup = mapper.dtoToEntity(groupDTO);
      tempGroup.setUserSet(userSet);
      return mapper.entityToDTO(groupRepository.save(tempGroup));
    }
  }

  @Override
  public GroupDTO delete(String id) throws GroupException {
    Group group =
        groupRepository
            .findById(id)
            .orElseThrow(() -> new GroupException(GroupException.NO_SUCH_GROUP));
    GroupDTO groupDTO = mapper.entityToDTO(group);
    groupRepository.delete(group);
    return groupDTO;
  }

  private List<JwtUser> findAllMembers(String id) throws GroupException {
    Group group =
        groupRepository
            .findById(id)
            .orElseThrow(() -> new GroupException(GroupException.NO_SUCH_GROUP));
    Set<User> users = group.getUserSet();
    return JwtUserFactory.createList(users);
  }

  @Override
  public List<JwtUser> addMembers(String id, List<String> usersId)
      throws UserException, GroupException {
    Group group =
        groupRepository
            .findById(id)
            .orElseThrow(() -> new GroupException(GroupException.NO_SUCH_GROUP));

    Set<User> userSet = group.getUserSet();
    for (String userId : usersId) {
      User user =
          userRepository
              .findById(userId)
              .orElseThrow(() -> new UserException(UserException.NO_SUCH_USER));
      userSet.add(user);
    }
    group.setUserSet(userSet);
    groupRepository.save(group);
    return JwtUserFactory.createList(userSet);
  }

  @Override
  public List<JwtUser> deleteMembers(String id, List<String> usersId)
      throws GroupException, UserException {
    Group group =
        groupRepository
            .findById(id)
            .orElseThrow(() -> new GroupException(GroupException.NO_SUCH_GROUP));
    Set<User> userSet = group.getUserSet();

    for (String userId : usersId) {
      User user =
          userRepository
              .findById(userId)
              .orElseThrow(() -> new UserException(UserException.NO_SUCH_USER));
      userSet.remove(user);
    }
    group.setUserSet(userSet);
    groupRepository.save(group);
    return JwtUserFactory.createList(userSet);
  }
}
