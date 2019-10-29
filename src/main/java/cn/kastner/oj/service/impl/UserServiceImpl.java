package cn.kastner.oj.service.impl;

import cn.kastner.oj.domain.Contest;
import cn.kastner.oj.domain.Group;
import cn.kastner.oj.domain.User;
import cn.kastner.oj.domain.security.Authority;
import cn.kastner.oj.domain.security.AuthorityName;
import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.exception.*;
import cn.kastner.oj.query.UserQuery;
import cn.kastner.oj.repository.AuthorityRepository;
import cn.kastner.oj.repository.ContestRepository;
import cn.kastner.oj.repository.GroupRepository;
import cn.kastner.oj.repository.UserRepository;
import cn.kastner.oj.security.JwtUser;
import cn.kastner.oj.security.JwtUserFactory;
import cn.kastner.oj.service.UserService;
import cn.kastner.oj.util.CommonUtil;
import cn.kastner.oj.util.ExcelUtil;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  private final AuthorityRepository authorityRepository;

  private final ContestRepository contestRepository;

  private final GroupRepository groupRepository;

  @Autowired
  public UserServiceImpl(
      UserRepository userRepository,
      AuthorityRepository authorityRepository,
      ContestRepository contestRepository, GroupRepository groupRepository) {
    this.userRepository = userRepository;
    this.authorityRepository = authorityRepository;
    this.contestRepository = contestRepository;
    this.groupRepository = groupRepository;
  }

  @Override
  public PageDTO<JwtUser> getUserRanking(Integer page, Integer size) {
    Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "acCount");
    List<User> userList = userRepository.findAll(pageable).getContent();
    List<JwtUser> jwtUserList = new ArrayList<>();
    for (User user : userList) {
      jwtUserList.add(JwtUserFactory.create(user));
    }
    return new PageDTO<>(page, size, (long) jwtUserList.size(), jwtUserList);
  }

  @Override
  public PageDTO<JwtUser> getAllUsers(Integer page, Integer size, UserQuery userQuery) {
    Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "acCount");

    Specification<User> us =
        ((root, criteriaQuery, criteriaBuilder) -> {
          List<Predicate> predicateList = new ArrayList<>();

          String id = userQuery.getId();
          if (null != id) {
            predicateList.add(criteriaBuilder.equal(root.get("id"), id));
          }

          String username = userQuery.getUsername();
          if (!CommonUtil.isNull(username)) {
            predicateList.add(criteriaBuilder.like(root.get("username"), "%" + username + "%"));
          }

          String name = userQuery.getName();
          if (!CommonUtil.isNull(name)) {
            predicateList.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
          }

          String studentNumber = userQuery.getStudentNumber();
          if (!CommonUtil.isNull(studentNumber)) {
            predicateList.add(
                criteriaBuilder.like(root.get("studentNumber"), "%" + studentNumber + "%"));
          }

          String school = userQuery.getSchool();
          if (!CommonUtil.isNull(school)) {
            predicateList.add(criteriaBuilder.like(root.get("school"), "%" + school + "%"));
          }

          Boolean temporary = userQuery.getTemporary();
          if (null != temporary) {
            predicateList.add(criteriaBuilder.equal(root.get("temporary"), temporary));
          }

          Predicate[] predicates = new Predicate[predicateList.size()];
          return criteriaBuilder.and(predicateList.toArray(predicates));
        });

    List<User> userList = userRepository.findAll(us, pageable).getContent();
    long count = userRepository.count(us);
    List<JwtUser> jwtUserList = new ArrayList<>();
    for (User user : userList) {
      jwtUserList.add(JwtUserFactory.create(user));
    }
    return new PageDTO<>(page, jwtUserList.size(), count, jwtUserList);
  }

  @Override
  public JwtUser getOne(String id) throws UserException {
    Optional<User> userOptional = userRepository.findById(id);
    if (!userOptional.isPresent()) {
      throw new UserException(UserException.NO_SUCH_USER);
    }
    return JwtUserFactory.create(userOptional.get());
  }

  @Override
  public JwtUser create(User user) throws UserException {
    user.setId(null);

    final String username = user.getUsername();
    Optional<User> userOptional = userRepository.findUserByUsername(username);
    if (userOptional.isPresent()) {
      throw new UserException(UserException.USERNAME_REPEAT);
    }

    final String email = user.getEmail();
    Optional<User> userOptional1 = userRepository.findByEmail(email);
    if (userOptional1.isPresent()) {
      throw new UserException(UserException.EMAIL_REPEAT);
    }

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    final String rawPassword = user.getPassword();
    user.setPassword(encoder.encode(rawPassword));

    List<Authority> authorities = new ArrayList<>();
    for (Authority authority : user.getAuthorities()) {
      authorities.add(authorityRepository.findByName(authority.getName()));
    }
    user.setAuthorities(authorities);
    return JwtUserFactory.create(userRepository.save(user));
  }

  @Override
  public JwtUser update(User user) throws UserException {

    String username = user.getUsername();
    Optional<User> userOptional = userRepository.findUserByUsername(username);
    if (userOptional.isPresent() && !userOptional.get().getId().equals(user.getId())) {
      throw new UserException(UserException.USERNAME_REPEAT);
    }

    String email = user.getEmail();
    Optional<User> userOptional1 = userRepository.findByEmail(email);
    if (userOptional1.isPresent() && !userOptional1.get().getEmail().equals(user.getEmail())) {
      throw new UserException(UserException.EMAIL_REPEAT);
    }

    List<Authority> authorities = new ArrayList<>();
    for (Authority authority : user.getAuthorities()) {
      authorities.add(authorityRepository.findByName(authority.getName()));
    }
    user.setAuthorities(authorities);

    return JwtUserFactory.create(userRepository.save(user));
  }

  @Override
  public void delete(List<String> idList) throws NoSuchItemException {
    List<User> selectedUserList = new ArrayList<>();
    for (String id : idList) {
      Optional<User> userOptional = userRepository.findById(id);
      if (!userOptional.isPresent()) {
        throw new NoSuchItemException("没有此用户！");
      }
      selectedUserList.add(userOptional.get());
    }

    userRepository.deleteAll(selectedUserList);
  }

  @Override
  public PageDTO<JwtUser> generateUser(String groupId, Long quantity) throws GroupException {
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new GroupException(GroupException.NO_SUCH_GROUP));

    List<User> userList = new ArrayList<>();
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    List<Authority> authorities = new ArrayList<>();
    authorities.add(authorityRepository.findByName(AuthorityName.ROLE_USER));

    for (int i = 0; i < quantity; i++) {
      String username = "g" + generateNum(group.getIdx().intValue()) + "#" + generateNum(i);
      String password = encoder.encode(CommonUtil.generateStr(6));
      String firstname = "临时";
      String lastname = "用户";
      String email = username + "@temp.com";
      User user = new User(username, password, firstname, lastname, "", email, "临时大学", authorities);
      user.setTemporary(true);
      userList.add(user);
    }

    userList = userRepository.saveAll(userList);
    group.setUserSet(new HashSet<>(userList));
    groupRepository.save(group);

    List<JwtUser> jwtUserList = JwtUserFactory.createList(userRepository.saveAll(userList));
    return new PageDTO<>(0, jwtUserList.size(), (long) jwtUserList.size(), jwtUserList);
  }

  @Override
  public PageDTO<JwtUser> generateUser(String groupId, File excel) throws GroupException, FileException {
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new GroupException(GroupException.NO_SUCH_GROUP));

    ExcelUtil.validExcel(excel);

    List<User> userList = new ArrayList<>();
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    List<Authority> authorities = new ArrayList<>();
    authorities.add(authorityRepository.findByName(AuthorityName.ROLE_USER));

    try (InputStream is = new FileInputStream(excel)) {
      Workbook wb = ExcelUtil.getWorkbook(is, excel);
      Sheet sheet = wb.getSheetAt(0);

      int index = 0;
      for (Row row : sheet) {
        row.getCell(0).setCellType(CellType.STRING);
        row.getCell(1).setCellType(CellType.STRING);
        if ("".equals(row.getCell(0).getStringCellValue())) {
          break;
        }

        if (index == 0) {
          if (!"学号".equals(row.getCell(0).getStringCellValue())
              || !"姓名".equals(row.getCell(1).getStringCellValue())) {
            throw new FileException(FileException.EXCEL_FORMAT_ERROR);
          }
          index++;
          continue;
        }

        String username = "g" + generateNum(group.getIdx().intValue()) + "#" + generateNum(index);
        String password = encoder.encode(CommonUtil.generateStr(6));
        String studentNumber = row.getCell(0).getStringCellValue();
        String firstname = row.getCell(1).getStringCellValue();
        String email = username + "@acmoj.shu.edu.cn";
        User user =
            new User(username, password, firstname, "", studentNumber, email, "上海大学", authorities);
        user.setTemporary(true);
        userList.add(user);
        index++;
      }
    } catch (IOException e) {
      throw new FileException(e.getMessage());
    }

    userList = userRepository.saveAll(userList);
    group.setUserSet(new HashSet<>(userList));
    groupRepository.save(group);

    List<JwtUser> jwtUserList = JwtUserFactory.createList(userRepository.saveAll(userList));
    return new PageDTO<>(0, jwtUserList.size(), (long) jwtUserList.size(), jwtUserList);
  }

  private String generateNum(int integer) {
    if (integer < 10) {
      return "000" + integer;
    } else if (integer < 100) {
      return "00" + integer;
    } else if (integer < 1000) {
      return "0" + integer;
    }
    return String.valueOf(integer);
  }
}
