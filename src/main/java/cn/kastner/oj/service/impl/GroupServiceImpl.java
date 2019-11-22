package cn.kastner.oj.service.impl;

import cn.kastner.oj.constant.EntityName;
import cn.kastner.oj.domain.Group;
import cn.kastner.oj.domain.IndexSequence;
import cn.kastner.oj.domain.User;
import cn.kastner.oj.domain.security.UserContext;
import cn.kastner.oj.dto.GroupDTO;
import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.exception.AuthorizationException;
import cn.kastner.oj.exception.GroupException;
import cn.kastner.oj.exception.UserException;
import cn.kastner.oj.query.GroupQuery;
import cn.kastner.oj.repository.GroupRepository;
import cn.kastner.oj.repository.IndexSequenceRepository;
import cn.kastner.oj.repository.UserRepository;
import cn.kastner.oj.security.JwtUser;
import cn.kastner.oj.security.JwtUserFactory;
import cn.kastner.oj.service.GroupService;
import cn.kastner.oj.util.CommonUtil;
import cn.kastner.oj.util.DTOMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
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

  private final IndexSequenceRepository indexSequenceRepository;

  @Autowired
  public GroupServiceImpl(
      GroupRepository groupRepository,
      DTOMapper mapper,
      UserRepository userRepository,
      IndexSequenceRepository indexSequenceRepository) {
    this.groupRepository = groupRepository;
    this.mapper = mapper;
    this.userRepository = userRepository;
    this.indexSequenceRepository = indexSequenceRepository;
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
  public PageDTO<GroupDTO> findCriteria(GroupQuery groupQuery, Integer page, Integer size) {
    User user = UserContext.getCurrentUser();
    Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, "idx");
    Specification<Group> ps =
        (root, criteriaQuery, criteriaBuilder) -> {
          List<Predicate> predicateList = new ArrayList<>();

          String name = groupQuery.getName();
          if (!CommonUtil.isNull(name)) {
            predicateList.add(
                criteriaBuilder.like(root.get("name").as(String.class), "%" + name + "%"));
          }

          Long idx = groupQuery.getIdx();
          if (null != idx) {
            predicateList.add(criteriaBuilder.equal(root.get("idx").as(Long.class), idx));
          }

          if (null != groupQuery.getCurrentUser() && groupQuery.getCurrentUser()) {
            predicateList.add(criteriaBuilder.equal(root.get("author").as(User.class), user));
          } else if (null != groupQuery.getAuthorId()) {
            Optional<User> authorOptional = userRepository.findById(groupQuery.getAuthorId());
            if (!authorOptional.isPresent()) {
              return null;
            }
            predicateList.add(
                criteriaBuilder.equal(root.get("author").as(User.class), authorOptional.get()));
          }

          Predicate[] p = new Predicate[predicateList.size()];
          return criteriaBuilder.and(predicateList.toArray(p));
        };

    if (null == ps) {
      return new PageDTO<>(page, 0, 0L, new ArrayList<>());
    }
    List<Group> groupList = groupRepository.findAll(ps, pageable).getContent();
    long count = groupRepository.count(ps);

    return new PageDTO<>(page, size, count, mapper.toGroupDTOs(groupList));
  }

  @Override
  public GroupDTO create(GroupDTO in) throws GroupException {
    Optional<Group> groupOptional = groupRepository.findByName(in.getName());
    if (groupOptional.isPresent()) {
      throw new GroupException(GroupException.HAVE_SUCH_GROUP);
    }
    Group group = mapper.dtoToEntity(in);
    User user = UserContext.getCurrentUser();
    group.setAuthor(user);
    group.setCreateDate(LocalDateTime.now());
    IndexSequence sequence = indexSequenceRepository.findByName(EntityName.GROUP);
    group.setIdx(sequence.getNextIdx());
    GroupDTO dto = mapper.entityToDTO(groupRepository.save(group));
    sequence.setNextIdx(group.getIdx() + 1);
    indexSequenceRepository.save(sequence);
    return dto;
  }

  @Override
  public GroupDTO update(GroupDTO groupDTO) throws GroupException, AuthorizationException {
    Group group =
        groupRepository
            .findById(groupDTO.getId())
            .orElseThrow(() -> new GroupException(GroupException.NO_SUCH_GROUP));

    authorize(group);

    Optional<Group> existGroupOptional = groupRepository.findByName(groupDTO.getName());
    if (existGroupOptional.isPresent()
        && !existGroupOptional.get().getId().equals(groupDTO.getId())) {
      throw new GroupException(GroupException.HAVE_SUCH_GROUP);
    }
    if (null != groupDTO.getName()) {
      group.setName(groupDTO.getName());
    }
    return mapper.entityToDTO(groupRepository.save(group));
  }

  @Override
  public GroupDTO delete(String id) throws GroupException, AuthorizationException {
    Group group =
        groupRepository
            .findById(id)
            .orElseThrow(() -> new GroupException(GroupException.NO_SUCH_GROUP));

    authorize(group);

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
      throws UserException, GroupException, AuthorizationException {
    Group group =
        groupRepository
            .findById(id)
            .orElseThrow(() -> new GroupException(GroupException.NO_SUCH_GROUP));

    authorize(group);

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
      throws GroupException, UserException, AuthorizationException {
    Group group =
        groupRepository
            .findById(id)
            .orElseThrow(() -> new GroupException(GroupException.NO_SUCH_GROUP));
    Set<User> userSet = group.getUserSet();

    authorize(group);

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

  @Override
  public Workbook resetMembersPassword(String id) throws GroupException {
    Group group =
        groupRepository
            .findById(id)
            .orElseThrow(() -> new GroupException(GroupException.NO_SUCH_GROUP));
    Set<User> userSet = group.getUserSet();
    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet("用户密码");
    int rowNum = 0;
    Row header = sheet.createRow(rowNum++);
    header.createCell(0).setCellValue("Username");
    header.createCell(1).setCellValue("学号");
    header.createCell(2).setCellValue("姓名");
    header.createCell(3).setCellValue("密码");
    for (User user : userSet) {
      String randomPassword = CommonUtil.generateStr(8);
      user.setPassword(randomPassword);
      Row row = sheet.createRow(rowNum++);
      row.createCell(0).setCellValue(user.getUsername());
      row.createCell(1).setCellValue(user.getStudentNumber());
      row.createCell(2).setCellValue(user.getName());
      row.createCell(3).setCellValue(randomPassword);
    }
    return workbook;
  }

  private void authorize(Group group) throws AuthorizationException {
    User user = UserContext.getCurrentUser();
    if (!user.isAdmin() && !user.equals(group.getAuthor())) {
      throw new AuthorizationException(AuthorizationException.NOT_GROUP_OWNER);
    }
  }
}
